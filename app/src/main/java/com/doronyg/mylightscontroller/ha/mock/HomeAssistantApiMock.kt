package com.doronyg.mylightscontroller.ha.mock

import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HomeAssistantInterface
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.utils.JsonUtils
import com.doronyg.mylightscontroller.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString

/**
 * Mock Home Assistant backed by an in-memory device list.
 * The list is loaded lazily from preferences and written back only when it actually
 * changes and [Settings.saveMockHaData] is enabled.
 */
object HomeAssistantApiMock : HomeAssistantInterface {
    private const val MOCK_DATA_KEY = "mock_ha_devices"

    // Use platform-agnostic preferences
    private var mockDevicesJson by PlatformBridge.preferences.stringPref(MOCK_DATA_KEY, "")

    // In-memory state (null = not loaded from disk yet)
    private var devices: List<HaDevice>? = null

    private val _changes = MutableStateFlow(0L)

    /**
     * Running counter, incremented whenever the mock data is replaced externally
     * via [populateMockData]. Commands do not bump it - the repository refreshes after commands.
     */
    val changes: StateFlow<Long> = _changes.asStateFlow()

    // --- Storage helpers ---
    private fun currentDevices(): List<HaDevice> =
        devices ?: readFromDisk().also { devices = it }

    private fun readFromDisk(): List<HaDevice> {
        val jsonString = mockDevicesJson
        if (jsonString.isEmpty()) return emptyList()
        return try {
            JsonUtils.json.decodeFromString<List<HaDevice>>(jsonString)
        } catch (e: Exception) {
            Utils.logException(e, "Failed to deserialize mock devices")
            emptyList()
        }
    }

    private fun writeToDisk(devices: List<HaDevice>) {
        try {
            mockDevicesJson = JsonUtils.json.encodeToString(devices)
            Utils.log("Saved ${devices.size} mock devices")
        } catch (e: Exception) {
            Utils.logException(e, "Failed to serialize mock devices")
        }
    }

    /** Updates the in-memory state and persists it if needed. Returns true if anything changed. */
    private fun setDevices(newDevices: List<HaDevice>): Boolean {
        if (newDevices == currentDevices()) return false
        devices = newDevices
        if (Settings.saveMockHaData) writeToDisk(newDevices)
        return true
    }

    // --- HomeAssistantInterface ---
    override suspend fun fetchAllLights(): List<HaDevice> = currentDevices()

    override suspend fun turnOnLights(devices: List<HaDevice>, brightnessPct: Int?) {
        updateDevices(devices) { old ->
            old.copy(
                state = "on",
                attributes = old.attributes.copy(
                    brightness = brightnessPct?.let { it * 255 / 100 } ?: old.attributes.brightness
                )
            )
        }
    }

    override suspend fun turnOffLights(devices: List<HaDevice>) {
        updateDevices(devices) { old -> old.copy(state = "off") }
    }

    private fun updateDevices(targets: List<HaDevice>, transform: (HaDevice) -> HaDevice) {
        val ids = targets.map { it.entityId }.toSet()
        setDevices(currentDevices().map { if (it.entityId in ids) transform(it) else it })
    }

    // --- Populate mock data (demo mode, tests, or mirroring real HA data) ---
    fun populateMockData(devices: List<HaDevice>) {
        if (setDevices(devices)) {
            _changes.update { it + 1 }
        }
    }
}

