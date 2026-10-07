package com.doronyg.mylightscontroller.ha

/**
 * Low-level access to Home Assistant (real or mock).
 *
 * Implementations are stateless request executors: no caching, no scheduling, no notifications.
 * All functions throw on failure. Refresh logic and state live in [HomeAssistantRepository].
 */
interface HomeAssistantInterface {
    /** Fetch all lights/switches. */
    suspend fun fetchAllLights(): List<HaDevice>

    // --- Turn lights on/off ---
    suspend fun turnOnLights(devices: List<HaDevice>, brightnessPct: Int? = null)
    suspend fun turnOffLights(devices: List<HaDevice>)
}

