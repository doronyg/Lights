package com.doronyg.mylightscontroller.ha.mock
import android.content.Context
import android.content.SharedPreferences
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HomeAssistantInterface
import com.doronyg.mylightscontroller.utils.JsonUtils
import com.doronyg.mylightscontroller.utils.Utils
import kotlinx.serialization.encodeToString
import java.lang.ref.WeakReference

object HomeAssistantApiMock : HomeAssistantInterface {
    private const val PREF_NAME = "mock_ha_data"
    private const val MOCK_DATA_KEY = "mock_ha_devices"
    private val listeners = mutableListOf<WeakReference<HomeAssistantInterface.Listener>>()
    private val prefs: SharedPreferences?
        get() = Utils.appContext?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    // --- Helper to load cached devices ---
    private fun loadDevices(): List<HaDevice> {
        val jsonString = prefs?.getString(MOCK_DATA_KEY, null)
        return if (jsonString != null) {
            try {
                JsonUtils.json.decodeFromString<List<HaDevice>>(jsonString)
            } catch (e: Exception) {
                Utils.log("Failed to deserialize mock devices: $e")
                Utils.logException(e)
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    // --- Helper to save devices persistently ---
    private fun saveDevices(devices: List<HaDevice>) {
        if (!Settings.saveMockHaData) return
        try {
            val jsonString = JsonUtils.json.encodeToString(devices)
            prefs?.edit()?.putString(MOCK_DATA_KEY, jsonString)?.apply()
        } catch (e: Exception) {
            Utils.log("Failed to serialize mock devices: $e")
            Utils.logException(e)
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
        // Always return cached devices
        val devices = loadDevices()
        Utils.post({onResult(devices)})
    }

    // --- Turn on lights ---
    override fun turnOnLights(devices: List<HaDevice>, brightnessPct: Int?) {
        val current = loadDevices().toMutableList()
        devices.forEach { device ->
            val index = current.indexOfFirst { it.entityId == device.entityId }
            if (index >= 0) {
                val old = current[index]
                current[index] = old.copy(
                    state = "on",
                    attributes = old.attributes.copy(
                        brightness = brightnessPct?.let { it * 255 / 100 } ?: old.attributes.brightness
                    )
                )
            }
        }
        saveDevices(current)
        notifyListeners()
    }

    // --- Turn off lights ---
    override fun turnOffLights(devices: List<HaDevice>) {
        val current = loadDevices().toMutableList()
        devices.forEach { device ->
            val index = current.indexOfFirst { it.entityId == device.entityId }
            if (index >= 0) {
                val old = current[index]
                current[index] = old.copy(state = "off")
            }
        }
        saveDevices(current)
        notifyListeners()
    }

    // --- Optional helper to pre-populate mock ---
    fun populateMockData(devices: List<HaDevice>) {
        saveDevices(devices)
        notifyListeners()
    }
}