package com.doronyg.mylightscontroller.ha.mock

import android.content.Context
import android.content.SharedPreferences
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HomeAssistantInterface
import com.doronyg.mylightscontroller.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.ref.WeakReference

object HomeAssistantApiMock : HomeAssistantInterface {

    private const val PREF_NAME = "mock_ha_data"
    private const val MOCK_DATA_KEY = "mock_ha_devices"

    private val gson = Gson()
    private val typeToken = object : TypeToken<List<HaDevice>>() {}.type

    private val listeners = mutableListOf<WeakReference<HomeAssistantInterface.Listener>>()

    private val prefs: SharedPreferences?
        get() = Utils.appContext?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // --- Helper to load cached devices ---
    private fun loadDevices(): List<HaDevice> {
        val json = prefs?.getString(MOCK_DATA_KEY, null)
        return if (json != null) {
            gson.fromJson<List<HaDevice>>(json, typeToken)
        } else {
            emptyList()
        }
    }

    // --- Helper to save devices persistently ---
    private fun saveDevices(devices: List<HaDevice>) {
        if (!Settings.saveMockHaData) return
        prefs?.edit()?.putString(MOCK_DATA_KEY, gson.toJson(devices))?.apply()
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