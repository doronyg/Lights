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
    var isConfigured by PlatformBridge.preferences.booleanPref("IS_CONFIGURED", !Config.homeAssistantUrl.isEmpty())

    /** Ready to talk to HA: set up in the app, or the connection is managed outside it (web gateway). */
    val hasHaConnection: Boolean
        get() = isConfigured || PlatformBridge.haEndpoint.credentialsManagedExternally

    fun withMockData() = useMockHaData || !hasHaConnection
}
