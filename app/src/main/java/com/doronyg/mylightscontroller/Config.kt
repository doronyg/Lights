package com.doronyg.mylightscontroller

import com.doronyg.mylightscontroller.Settings.stringPref
import com.doronyg.mylightscontroller.platform.PlatformBridge

object Config {
    var homeAssistantUrl by stringPref("HA_URL", PlatformBridge.defaultConfig.defaultHaUrl)
    var homeAssistantToken by stringPref("HA_TOKEN", PlatformBridge.defaultConfig.defaultHaToken)
    var porcupineApiKey by stringPref("PORCUPINE_API_KEY", PlatformBridge.defaultConfig.defaultPorcupineApiKey)
}
