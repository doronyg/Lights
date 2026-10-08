package com.doronyg.mylightscontroller

import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.utils.Base64Codec

/**
 * Connection settings (Base64-encoded in preferences).
 *
 * Must not depend on [Settings]: Settings reads Config for its defaults, so a dependency in the
 * other direction creates an initialization cycle (a null-pointer crash when Config is used first).
 */
object Config {
    private fun encodedPref(key: String, default: String) =
        PlatformBridge.preferences.stringPref(key, default, Base64Codec)

    var homeAssistantUrl by encodedPref("HA_URL", PlatformBridge.defaultConfig.defaultHaUrl)
    var homeAssistantToken by encodedPref("HA_TOKEN", PlatformBridge.defaultConfig.defaultHaToken)
    var porcupineApiKey by encodedPref("PORCUPINE_API_KEY", PlatformBridge.defaultConfig.defaultPorcupineApiKey)
}
