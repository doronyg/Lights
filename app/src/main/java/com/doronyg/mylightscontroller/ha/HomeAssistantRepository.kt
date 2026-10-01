package com.doronyg.mylightscontroller.ha

import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.Settings.secondsForHaUpdateOnUi
import com.doronyg.mylightscontroller.ha.mock.HomeAssistantApiMock
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class HomeAssistantRepository private constructor() : HomeAssistantInterface {

    private val listeners = mutableListOf< WeakReference<HomeAssistantInterface.Listener>>()

    // --- Reactive state flows (for KMP compatibility) ---
    private val _devices = MutableStateFlow<List<HaDevice>>(emptyList())
    val devices: StateFlow<List<HaDevice>> = _devices

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // --- UI presence tracking (separate from data listeners) ---
    private var nextToken = 0
    private val activeUiTokens = mutableSetOf<Int>()

    // --- Polling ---
    private var pollingIterationMillis = secondsForHaUpdateOnUi.coerceAtLeast(1).times(1000L)
    private var pollingJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + PlatformBridge.dispatchers.ioDispatcher())

    private var api: HomeAssistantInterface =
        if (Settings.withMockData()) HomeAssistantApiMock
        else HomeAssistantApi

    private val apiListener = object : HomeAssistantInterface.Listener {
        override fun onUpdate() {
            notifyListeners()
        }
    }

    init {
        api.registerListener(apiListener)
    }

    // Switch API if setting changed
    private fun ensureCorrectApi() {
        val shouldUseMock = Settings.withMockData()
        val newApi =
            if (shouldUseMock) HomeAssistantApiMock
            else HomeAssistantApi

        if (api !== newApi) {
            api.deregisterListener(apiListener)
            api = newApi
            api.registerListener(apiListener)
            notifyListeners()
        }
    }

    // --- Listener management ---
    private fun cleanupListeners() {
        listeners.removeAll { it.get() == null }
    }

    override fun registerListener(listener: HomeAssistantInterface.Listener) {
        cleanupListeners()
        if (listeners.none { it.get() == listener }) {
            listeners.add(WeakReference(listener))
        }
    }

    override fun deregisterListener(listener: HomeAssistantInterface.Listener) {
        cleanupListeners()
        listeners.removeAll { it.get() == listener || it.get() == null }
    }

    private fun notifyListeners() {
        cleanupListeners()
        listeners.forEach { it.get()?.onUpdate() }
    }

    // --- Fetch devices ---
    override fun fetchAllLights(
        refresh: Boolean,
        onResult: (List<HaDevice>) -> Unit
    ) {
        ensureCorrectApi()

        api.fetchAllLights(refresh) { devices ->
            CoroutineScope(PlatformBridge.dispatchers.ioDispatcher()).launch {
                // If using real API and saving is enabled → update mock storage
                if (api === HomeAssistantApi && Settings.saveMockHaData) {
                    HomeAssistantApiMock.populateMockData(devices)
                }

                coroutineScope {
                    devices.map { device ->
                        async { device.translate() }
                    }.awaitAll()
                }
            }

            CoroutineScope(PlatformBridge.dispatchers.uiDispatcher()).launch {
                _devices.value = devices
                onResult(devices)

                if (refresh) {
                    notifyListeners()
                }

                _loading.value = false
            }
        }
    }

    // --- Control lights ---
    override fun turnOnLights(devices: List<HaDevice>, brightnessPct: Int?) {
        ensureCorrectApi()
        api.turnOnLights(devices, brightnessPct)
    }

    override fun turnOffLights(devices: List<HaDevice>) {
        ensureCorrectApi()
        api.turnOffLights(devices)
    }

    fun registerUi(): Int {
        val token = nextToken++
        activeUiTokens.add(token)

        if (activeUiTokens.size == 1) {
            startPolling()
        }

        return token
    }

    fun unregisterUi(token: Int) {
        val removed = activeUiTokens.remove(token)
        if (!removed) return // ignore invalid / double calls

        if (activeUiTokens.isEmpty()) {
            stopPolling()
        }
    }

    // --- Polling control ---
    private fun startPolling() {
        if (pollingJob != null) return

        pollingJob = scope.launch {
            try {
                while (isActive && activeUiTokens.isNotEmpty()) {
                    _loading.value = true
                    fetchAllLights(true) {}
                    delay(pollingIterationMillis)
                }
            } finally {
                // Allow restart later
                pollingJob = null
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    companion object {
        val instance: HomeAssistantRepository by lazy {
            HomeAssistantRepository()
        }
    }
}