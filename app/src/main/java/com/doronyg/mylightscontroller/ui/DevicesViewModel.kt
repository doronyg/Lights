package com.doronyg.mylightscontroller.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HomeAssistantApi
import com.doronyg.mylightscontroller.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DevicesViewModel : ViewModel() {

    private val _devices = MutableStateFlow<List<HaDevice>>(emptyList())
    val devices: StateFlow<List<HaDevice>> = _devices

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    val homeAssistantListener  = object : HomeAssistantApi.Listener {
        override fun onUpdate() {
            delayedRefresh()
        }
    }

    init {
        refresh()
        HomeAssistantApi.registerListener(homeAssistantListener)
    }

    override fun onCleared() {
        HomeAssistantApi.deregisterListener(homeAssistantListener)
        super.onCleared()
    }

    fun refresh(showLoading: Boolean = false) {
        if (showLoading) {
            _loading.value = true
        }

        HomeAssistantApi.fetchAllLights(true) { list ->
            viewModelScope.launch {
                _devices.value = list
                _loading.value = false
            }
        }
    }

    fun turnOn(devices: List<HaDevice>, brightnessPct: Int? = null) {
        HomeAssistantApi.turnOnLights(devices, brightnessPct)
    }

    fun turnOff(devices: List<HaDevice>) {
        HomeAssistantApi.turnOffLights(devices)
    }

    private fun delayedRefresh() {
        Utils.post({refresh(false)}, 500)
    }
}
