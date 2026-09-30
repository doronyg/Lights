package com.doronyg.mylightscontroller.platform.android

import com.doronyg.mylightscontroller.BuildConfig
import com.doronyg.mylightscontroller.platform.common.IPlatformDefaultConfig

object AndroidPlatformDefaultConfig : IPlatformDefaultConfig {
    override val defaultHaUrl: String = BuildConfig.HA_URL
    override val defaultHaToken: String = BuildConfig.HA_TOKEN
    override val defaultPorcupineApiKey: String = BuildConfig.PORCUPINE_API_KEY
}

