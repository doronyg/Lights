package com.doronyg.mylightscontroller.onboarding

import com.doronyg.mylightscontroller.Config
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.ha.mock.HomeAssistantApiMock
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.utils.MockUtils

/**
 * Configures the app to run in demo mode with mock Home Assistant data.
 */
suspend fun applyDemoConfig() {
    // Web: the gateway owns the HA credentials - don't store (even fake) ones in the browser
    if (!PlatformBridge.haEndpoint.credentialsManagedExternally) {
        Config.homeAssistantUrl = "http://demo.local"
        Config.homeAssistantToken = "demo-token"
    }
//    Config.PORCUPINE_API_KEY = "demo-key"

    Settings.useMockHaData = true
    Settings.isConfigured = true
    Settings.saveMockHaData = true

    HomeAssistantApiMock.populateMockData(MockUtils.loadMockDevices())
}
