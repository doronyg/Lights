package com.doronyg.mylightscontroller.ui

import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HomeAssistantRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Multiplatform screen state holder - NOT a ViewModel, works on any platform.
 * This replaces DevicesViewModel for better KMP compatibility.
 *
 * Makes no requests itself: all refresh logic (polling, follow-up refreshes after commands)
 * lives in [HomeAssistantRepository].
 */
class DevicesStateHolder(
    private val repository: HomeAssistantRepository = HomeAssistantRepository.instance
) {
    // Proxy repository flows
    val devices: StateFlow<List<HaDevice>> = repository.devices
    val loading: StateFlow<Boolean> = repository.loading

    // Token for UI polling registration
    private val uiToken: Int = repository.registerUi()

    fun turnOn(devices: List<HaDevice>, brightnessPct: Int? = null) {
        repository.turnOnLights(devices, brightnessPct)
    }

    fun turnOff(devices: List<HaDevice>) {
        repository.turnOffLights(devices)
    }

    fun cleanup() {
        repository.unregisterUi(uiToken)
    }
}

