package com.doronyg.mylightscontroller.utils

import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HomeAssistantRepository
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

/**
 * Multiplatform mock data utilities.
 * Platform-specific file I/O is delegated to PlatformBridge.mockDataProvider.
 */
object MockUtils {

    /**
     * Save mock HA data using platform-specific file I/O.
     * The callback receives the platform path of the written file (or null on failure)
     * and is dispatched on the UI thread.
     */
    fun saveMockData(
        onResult: ((String?) -> Unit)? = null
    ) {
        val provider = PlatformBridge.mockDataProvider

        CoroutineScope(PlatformBridge.dispatchers.ioDispatcher()).launch {
            try {
                HomeAssistantRepository.instance.fetchAllLights(
                    refresh = true
                ) { lights ->
                    val resultPath: String? = try {
                        val jsonString = JsonUtils.json.encodeToString(lights)
                        provider.writeFile("ha_lights.json", jsonString)
                    } catch (e: Exception) {
                        Utils.logException(e, "Failed to write HA data")
                        null
                    }
                    dispatchToMain(onResult, resultPath)
                }
            } catch (e: Exception) {
                Utils.logException(e, "Failed to fetch HA data")
                dispatchToMain(onResult, null)
            }
        }
    }

    /**
     * Load mock devices from platform-specific storage.
     * Platform-specific asset/file access is delegated to the provider.
     */
    fun loadMockDevices(): List<HaDevice> {
        return try {
            val jsonString = PlatformBridge.mockDataProvider.readAssetFile("ha_lights_demo.json")
            JsonUtils.json.decodeFromString<List<HaDevice>>(jsonString)
        } catch (e: Exception) {
            Utils.logException(e, "Failed to load mock data")
            emptyList()
        }
    }

    private fun dispatchToMain(
        onResult: ((String?) -> Unit)?,
        result: String?
    ) {
        if (onResult == null) return

        CoroutineScope(PlatformBridge.dispatchers.uiDispatcher()).launch {
            onResult(result)
        }
    }
}


