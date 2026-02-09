package com.doronyg.mylightscontroller.ha
import com.doronyg.mylightscontroller.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.lang.ref.WeakReference

object HomeAssistantApi {

    interface Listener {
        fun onUpdate()
    }

    private val gson = Gson()
    private val typeTokenType = object : TypeToken<List<HaDevice>>() {}.type

    private var haLights : List<HaDevice> = emptyList()

    private var listeners : MutableList<WeakReference<Listener>> = ArrayList()

    private fun cleanupListeners() {
        listeners = listeners.mapNotNull { if (it.get() == null) null else it }.toMutableList()
    }

    private fun findListener(listener: Listener) : WeakReference<Listener>? {
        listeners.forEach {
            val homeAssistantApiListener = it.get()
            if (homeAssistantApiListener ==  listener) return it
        }
        return null
    }

    fun registerListener(listener: Listener) {
        cleanupListeners()
        if (findListener(listener) == null) {
            listeners.add(WeakReference(listener))
        }
    }

    fun deregisterListener(listener: Listener) {
        findListener(listener)?.let { listeners.remove(it) }
    }

    // Fetch all lights
    fun fetchAllLights(refresh: Boolean = true, onResult: (List<HaDevice>) -> Unit) {
        if (!refresh && haLights.isNotEmpty()) {
            Utils.post({ onResult(haLights) })
            return
        }

        val request = Utils.buildHaRequest("states")

        Utils.okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Utils.log("HomeAssistantApi Failed to fetch states $e")
                onResult(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Utils.log("HomeAssistantApiUnexpected response: ${it.code}")
                        onResult(emptyList())
                        return
                    }

                    val body = it.body?.string() ?: ""

                    val devices: List<HaDevice> = gson.fromJson(body, typeTokenType)

                    // only keep lights & switches
                    onResult(devices.filter { haDevice -> (haDevice.type == DeviceType.LIGHT || haDevice.type == DeviceType.SWITCH) })
                }
            }
        })
    }

    // Turn on lights
    fun turnOnLights(devices: List<HaDevice>, brightnessPct: Int? = null) {
        val (lights, switches) = devicesToTypes(devices)

        if (lights.isNotEmpty()) {
            val lightIds = lights.map { it.entityId }
            val lightCommand = HaLightCommand(lightIds, brightnessPct)
            postCommand("light/turn_on", lightCommand)
        }

        if (switches.isNotEmpty()) {
            val switchIds = switches.map { it.entityId }
            val switchCommand = HaLightCommand(switchIds) // brightness ignored
            postCommand("switch/turn_on", switchCommand)
        }
    }

    // Turn off lights
    fun turnOffLights(devices: List<HaDevice>) {
        val (lights, switches) = devicesToTypes(devices)

        if (lights.isNotEmpty()) {
            val lightIds = lights.map { it.entityId }
            val lightCommand = HaLightCommand(lightIds)
            postCommand("light/turn_off", lightCommand)
        }

        if (switches.isNotEmpty()) {
            val switchIds = switches.map { it.entityId }
            val switchCommand = HaLightCommand(switchIds)
            postCommand("switch/turn_off", switchCommand)
        }
    }

    private fun devicesToTypes(devices: List<HaDevice>): Pair<List<HaDevice>, List<HaDevice>> {
        val lights = devices.filter { it.type == DeviceType.LIGHT }
        val switches = devices.filter { it.type == DeviceType.SWITCH }
        return Pair(lights, switches)
    }

    // Generic POST helper
    private fun postCommand(endpoint: String, command: HaLightCommand) {
        val request = Utils.buildHaRequest(
            endpoint = "services/$endpoint",
            method = "POST",
            bodyJson = gson.toJson(command)
        )

        Utils.okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Utils.log("HomeAssistantApi Failed to POST $endpoint $e")
            }

            override fun onResponse(call: Call, response: Response) {
                Utils.log("HomeAssistantApi $endpoint response: ${response.code}")
                listeners.forEach { it.get()?.onUpdate() }
                response.close()
            }
        })
    }
}
