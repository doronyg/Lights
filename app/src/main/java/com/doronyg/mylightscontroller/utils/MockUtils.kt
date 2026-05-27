package com.doronyg.mylightscontroller.utils

import android.content.Context
import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HomeAssistantRepository
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object MockUtils {

    private fun dispatchToMain(
        onResult: ((File?) -> Unit)?,
        result: File?
    ) {
        if (onResult == null) return

        CoroutineScope(Dispatchers.Main).launch {
            onResult(result)
        }
    }

    fun saveMockData(
        appContext: Context,
        onResult: ((File?) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                HomeAssistantRepository.instance.fetchAllLights(
                    refresh = true
                ) { lights ->

                    val resultFile: File? = try {
                        val json = GsonBuilder()
                            .setPrettyPrinting()
                            .create()
                            .toJson(lights)

                        val file = File(appContext.cacheDir, "ha_lights.json")
                        file.writeText(json)

                        Utils.log("HA export written to: ${file.absolutePath}")
                        file
                    } catch (e: Exception) {
                        Utils.logException(e, "Failed to write HA data")
                        null
                    }
                    dispatchToMain(onResult, resultFile)
                }
            } catch (e: Exception) {
                Utils.logException(e, "Failed to fetch HA data")
                dispatchToMain(onResult, null)
            }
        }
    }


    fun loadMockDevices(context: Context): List<HaDevice> {
        return try {
            val json = context.assets
                .open("ha_lights_demo.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<List<HaDevice>>() {}.type

            Gson().fromJson(json, type)
        } catch (e: Exception) {
            Utils.logException(e, "Failed to load mock data")
            emptyList()
        }
    }
}