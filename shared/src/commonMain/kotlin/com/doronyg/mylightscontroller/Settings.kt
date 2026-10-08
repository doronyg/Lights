package com.doronyg.mylightscontroller

import com.doronyg.mylightscontroller.platform.common.PlatformBridge

/**
 * Application settings management.
 * Platform-specific preference storage is provided by PlatformBridge.
 */
object Settings {

    // --- Settings ---
    var withVoiceControllerService by PlatformBridge.preferences.booleanPref("WITH_VOICE_CONTROLLER_SERVICE_SHARED_PREF")
    var secondsForHaUpdateOnUi by PlatformBridge.preferences.intPref("SECONDS_FOR_HA_UPDATE_ON_UI", 30)
    var saveMockHaData by PlatformBridge.preferences.booleanPref("SAVE_MOCK_HA_DATA")
    var useMockHaData by PlatformBridge.preferences.booleanPref("USE_MOCK_HA_DATA")

    // On the web, the gateway owns the HA credentials, so the default should be false (let the gateway check
    // in Main.kt decide). On native, default based on whether the app has locally stored URL/token.
    private val defaultIsConfigured: Boolean
        get() = if (PlatformBridge.haEndpoint.credentialsManagedExternally) {
            false  // Web: let gateway status check decide
        } else {
            !Config.homeAssistantUrl.isEmpty()  // Native: check for locally stored config
        }

    var isConfigured by PlatformBridge.preferences.booleanPref("IS_CONFIGURED", defaultIsConfigured)

    /**
     * Set up and ready to talk to HA. On the web this mirrors the gateway's state (checked at
     * startup), since the URL and token live on the gateway.
     */
    val hasHaConnection: Boolean
        get() = isConfigured

    fun withMockData() = useMockHaData || !hasHaConnection
}


