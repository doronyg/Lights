package com.doronyg.mylightscontroller

import com.doronyg.mylightscontroller.Settings.stringPref

object Config {
    var HOME_ASSISTANT_URL by stringPref("HA_URL", BuildConfig.HA_URL)
    var HOME_ASSISTANT_TOKEN by stringPref("HA_TOKEN", BuildConfig.HA_TOKEN)
    var PORCUPINE_API_KEY by stringPref("PORCUPINE_API_KEY", BuildConfig.PORCUPINE_API_KEY)
}
