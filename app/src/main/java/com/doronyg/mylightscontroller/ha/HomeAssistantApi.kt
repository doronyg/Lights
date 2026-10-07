package com.doronyg.mylightscontroller.ha

import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.utils.NetworkUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

object HomeAssistantApi : HomeAssistantInterface {

    override suspend fun fetchAllLights(): List<HaDevice> =
        withContext(PlatformBridge.dispatchers.ioDispatcher()) {
            NetworkUtils.haGet<List<HaDevice>>("states")
                // only keep lights & switches
                .filter { it.type == DeviceType.LIGHT || it.type == DeviceType.SWITCH }
        }

    override suspend fun turnOnLights(devices: List<HaDevice>, brightnessPct: Int?) {
        val (lights, switches) = devicesToTypes(devices)
        postCommands(
            "light/turn_on" to lights.toCommand(brightnessPct),
            // brightness ignored for switches
            "switch/turn_on" to switches.toCommand(),
        )
    }

    override suspend fun turnOffLights(devices: List<HaDevice>) {
        val (lights, switches) = devicesToTypes(devices)
        postCommands(
            "light/turn_off" to lights.toCommand(),
            "switch/turn_off" to switches.toCommand(),
        )
    }

    private fun devicesToTypes(devices: List<HaDevice>): Pair<List<HaDevice>, List<HaDevice>> {
        val lights = devices.filter { it.type == DeviceType.LIGHT }
        val switches = devices.filter { it.type == DeviceType.SWITCH }
        return Pair(lights, switches)
    }

    private fun List<HaDevice>.toCommand(brightnessPct: Int? = null): HaLightCommand? =
        if (isEmpty()) null else HaLightCommand(map { it.entityId }, brightnessPct)

    // Sends the non-null commands in parallel; throws if any of them fails
    private suspend fun postCommands(vararg commands: Pair<String, HaLightCommand?>) {
        withContext(PlatformBridge.dispatchers.ioDispatcher()) {
            coroutineScope {
                commands.mapNotNull { (endpoint, command) ->
                    command?.let { async { NetworkUtils.haPost(endpoint, it) } }
                }.awaitAll()
            }
        }
    }
}

