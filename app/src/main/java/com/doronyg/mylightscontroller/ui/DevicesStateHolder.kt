package com.doronyg.mylightscontroller.ui

import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HomeAssistantInterface
import com.doronyg.mylightscontroller.ha.HomeAssistantRepository
import com.doronyg.mylightscontroller.utils.Utils
import kotlinx.coroutines.flow.StateFlow

/**
 * Multiplatform screen state holder - NOT a ViewModel, works on any platform.
 * This replaces DevicesViewModel for better KMP compatibility.
 */
class DevicesStateHolder(
    private val repository: HomeAssistantRepository = HomeAssistantRepository.instance
) {
    // Proxy repository flows
    val devices: StateFlow<List<HaDevice>> = repository.devices
    val loading: StateFlow<Boolean> = repository.loading

    // Token for UI polling registration
    private var uiToken: Int = -1

    private val homeAssistantListener = object : HomeAssistantInterface.Listener {
        override fun onUpdate() {
            // Data already updated in repository, UI automatically recomposes
            // via collectAsState() on devices and loading StateFlows
        }
    }

    init {
        // Register UI to enable background polling
        // Don't call refresh() here - polling will handle initial load
        uiToken = repository.registerUi()
        repository.registerListener(homeAssistantListener)
    }

    fun refresh(showLoading: Boolean = false) {
        repository.fetchAllLights(showLoading) {}
    }

    fun turnOn(devices: List<HaDevice>, brightnessPct: Int? = null) {
        repository.turnOnLights(devices, brightnessPct)
        delayedRefresh()
    }

    fun turnOff(devices: List<HaDevice>) {
        repository.turnOffLights(devices)
        delayedRefresh()
    }

    private fun delayedRefresh() {
        Utils.post({ refresh(false) }, 500)
    }

    fun cleanup() {
        repository.deregisterListener(homeAssistantListener)
        repository.unregisterUi(uiToken)
    }
}
