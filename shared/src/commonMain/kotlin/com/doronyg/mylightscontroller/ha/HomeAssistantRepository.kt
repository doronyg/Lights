package com.doronyg.mylightscontroller.ha

import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.ha.mock.HomeAssistantApiMock
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.utils.Utils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Single source of truth for Home Assistant devices, and the only place that decides
 * when to talk to Home Assistant (polling, follow-up refreshes after commands).
 *
 * All state is confined to the UI dispatcher. [registerUi] / [unregisterUi] must be called
 * from the UI thread; all other public functions are safe to call from any thread.
 */
class HomeAssistantRepository private constructor() {

    // --- Reactive state ---
    private val _devices = MutableStateFlow<List<HaDevice>>(emptyList())
    val devices: StateFlow<List<HaDevice>> = _devices.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // Unexpected errors in one job are logged (with stack) instead of escaping; other jobs keep running
    private val scope = CoroutineScope(
        SupervisorJob() + PlatformBridge.dispatchers.uiDispatcher() +
            CoroutineExceptionHandler { _, e -> Utils.logException(e, "HomeAssistantRepository job failed") }
    )

    // --- UI presence tracking ---
    private var nextToken = 0
    private val activeUiTokens = mutableSetOf<Int>()

    // --- Background jobs ---
    private var pollingJob: Job? = null
    private var followUpRefreshJob: Job? = null

    // Re-evaluated on every call, so switching the mock setting takes effect immediately
    private val api: HomeAssistantInterface
        get() = if (Settings.withMockData()) HomeAssistantApiMock else HomeAssistantApi

    init {
        // Mock data replaced externally (demo mode, tests) - reload if we're showing mock data
        scope.launch {
            HomeAssistantApiMock.changes.drop(1).collect {
                if (api === HomeAssistantApiMock) refreshDevices()
            }
        }
    }

    // --- Public API ---

    /**
     * Fetch devices and deliver them on the UI thread.
     * On failure the last known devices are delivered.
     * @param refresh if false and devices are already loaded, returns them without a request.
     */
    fun fetchAllLights(
        refresh: Boolean = true,
        onResult: (List<HaDevice>) -> Unit
    ) {
        scope.launch {
            val current = _devices.value
            onResult(if (!refresh && current.isNotEmpty()) current else refreshDevices())
        }
    }

    fun turnOnLights(devices: List<HaDevice>, brightnessPct: Int? = null) =
        runCommand("turnOn") { it.turnOnLights(devices, brightnessPct) }

    fun turnOffLights(devices: List<HaDevice>) =
        runCommand("turnOff") { it.turnOffLights(devices) }

    fun registerUi(): Int {
        val token = nextToken++
        activeUiTokens.add(token)
        if (activeUiTokens.size == 1) startPolling()
        return token
    }

    fun unregisterUi(token: Int) {
        if (!activeUiTokens.remove(token)) return // ignore invalid / double calls
        if (activeUiTokens.isEmpty()) stopPolling()
    }

    // --- Refresh logic ---

    /**
     * Fetch devices from the current API and publish them.
     * On failure the last known devices are kept and returned.
     */
    private suspend fun refreshDevices(): List<HaDevice> {
        val currentApi = api
        return try {
            currentApi.fetchAllLights().also { publish(currentApi, it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Utils.logException(e, "Failed to fetch devices - keeping last known state")
            _devices.value
        } finally {
            _loading.value = false
        }
    }

    private fun publish(source: HomeAssistantInterface, devices: List<HaDevice>) {
        _devices.value = devices

        // Mirror real HA data into the mock (the mock only writes to disk when data changed)
        if (source === HomeAssistantApi && Settings.saveMockHaData) {
            HomeAssistantApiMock.populateMockData(devices)
        }

        scope.launch(PlatformBridge.dispatchers.ioDispatcher()) {
            devices.map { device -> async { device.translate() } }.awaitAll()
        }
    }

    private fun runCommand(name: String, command: suspend (HomeAssistantInterface) -> Unit) {
        scope.launch {
            try {
                command(api)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Utils.logException(e, "HomeAssistant $name failed")
            }
            // Refresh even on failure so the UI reflects the real state
            scheduleFollowUpRefreshes()
        }
    }

    /**
     * HA is sometimes slow to apply state changes, so refresh a few times after a command.
     * A new command restarts the sequence.
     */
    private fun scheduleFollowUpRefreshes() {
        followUpRefreshJob?.cancel()
        followUpRefreshJob = scope.launch {
            FOLLOW_UP_REFRESH_DELAYS_MS.forEach { delayMs ->
                delay(delayMs)
                refreshDevices()
            }
        }
    }

    // --- Polling ---
    private fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = scope.launch {
            while (isActive) {
                _loading.value = true
                refreshDevices()
                delay(Settings.secondsForHaUpdateOnUi.coerceAtLeast(1) * 1000L)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    companion object {
        /** Delays between consecutive follow-up refreshes after a command. */
        private val FOLLOW_UP_REFRESH_DELAYS_MS = listOf(0L, 5_000L)

        val instance: HomeAssistantRepository by lazy {
            HomeAssistantRepository()
        }
    }
}

