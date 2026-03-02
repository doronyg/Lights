package com.doronyg.mylightscontroller.ha

interface HomeAssistantInterface {
    // Listener for updates
    interface Listener {
        fun onUpdate()
    }

    // --- Listener management ---
    fun registerListener(listener: Listener)
    fun deregisterListener(listener: Listener)

    // --- Fetch devices ---
    /**
     * Fetch all lights/switches from Home Assistant.
     * @param refresh if false, may return cached data.
     * @param onResult callback with the resulting devices.
     */
    fun fetchAllLights(refresh: Boolean = true, onResult: (List<HaDevice>) -> Unit)

    // --- Turn lights on/off ---
    fun turnOnLights(devices: List<HaDevice>, brightnessPct: Int? = null)
    fun turnOffLights(devices: List<HaDevice>)
}