package com.doronyg.mylightscontroller.ha
import com.doronyg.mylightscontroller.ha.HomeAssistantInterface.Listener
import com.doronyg.mylightscontroller.utils.NetworkUtils
import com.doronyg.mylightscontroller.utils.Utils
import java.lang.ref.WeakReference

object HomeAssistantApi : HomeAssistantInterface {

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

    override fun registerListener(listener: Listener) {
        cleanupListeners()
        if (findListener(listener) == null) {
            listeners.add(WeakReference(listener))
        }
    }

    override fun deregisterListener(listener: Listener) {
        findListener(listener)?.let { listeners.remove(it) }
    }

    // Fetch all lights
    override fun fetchAllLights(refresh: Boolean, onResult: (List<HaDevice>) -> Unit) {
        if (!refresh && haLights.isNotEmpty()) {
            Utils.post({ onResult(haLights) })
            return
        }

        NetworkUtils.asyncHaGet<List<HaDevice>>(
            endpoint = "states",
            onResult = { devices ->
                // only keep lights & switches
                val filteredDevices = devices.filter { haDevice ->
                    (haDevice.type == DeviceType.LIGHT || haDevice.type == DeviceType.SWITCH)
                }
                haLights = filteredDevices
                onResult(filteredDevices)
            },
            onError = { e ->
                Utils.log("HomeAssistantApi failed to fetch states")
                Utils.logException(e)
                onResult(emptyList())
            }
        )
    }

    // Turn on lights
    override fun turnOnLights(devices: List<HaDevice>, brightnessPct: Int?) {
        val (lights, switches) = devicesToTypes(devices)

        if (lights.isNotEmpty()) {
            val lightIds = lights.map { it.entityId }
            val lightCommand = HaLightCommand(lightIds, brightnessPct)
            postCommand("light/turn_on", lightCommand)
        }

        if (switches.isNotEmpty()) {
            val switchIds = switches.map { it.entityId }
            // brightness ignored
            val switchCommand = HaLightCommand(switchIds)
            postCommand("switch/turn_on", switchCommand)
        }
    }

    // Turn off lights
    override fun turnOffLights(devices: List<HaDevice>) {
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
        NetworkUtils.asyncHaPost(
            endpoint = endpoint,
            body = command,
            onSuccess = {
                Utils.log("HomeAssistantApi $endpoint response: success")
                updateLightsAndNotify()

                // Refetch as sometimes HA doesn't update the state immediately, so we fetch again after a delay
                Utils.post( {
                    updateLightsAndNotify()
                }, 5000)
            },
            onError = { e ->
                Utils.log("HomeAssistantApi Failed to POST $endpoint")
                Utils.logException(e)
            }
        )
    }

    private fun updateLightsAndNotify() {
        fetchAllLights(true) {
            listeners.forEach { it.get()?.onUpdate() }
        }
    }
}

