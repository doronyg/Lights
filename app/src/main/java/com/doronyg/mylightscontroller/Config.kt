package com.doronyg.mylightscontroller

import com.doronyg.mylightscontroller.Settings.stringPref

object Config {
    var homeAssistantUrl by stringPref("HA_URL", BuildConfig.HA_URL)
    var homeAssistantToken by stringPref("HA_TOKEN", BuildConfig.HA_TOKEN)
    var porcupineApiKey by stringPref("PORCUPINE_API_KEY", BuildConfig.PORCUPINE_API_KEY)
}
