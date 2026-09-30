package com.doronyg.mylightscontroller.platform

import com.doronyg.mylightscontroller.platform.android.AndroidPlatformPreferences
import com.doronyg.mylightscontroller.platform.android.AndroidPlatformTranslator
import com.doronyg.mylightscontroller.platform.android.AndroidPlatformVoiceController
import com.doronyg.mylightscontroller.platform.common.IPlatformPreferences
import com.doronyg.mylightscontroller.platform.common.IPlatformTranslator
import com.doronyg.mylightscontroller.platform.common.IPlatformVoiceController

/**
 * Single access point for all platform-specific implementations.
 *
 * This bridge holds references to platform implementations and provides them via common interfaces.
 * The app uses interfaces defined in `platform.common` package.
 *
 * Future: When converting to Kotlin Multiplatform, this will be replaced by the expect/actual
 * mechanism, and each platform will provide its own implementations automatically.
 */
object PlatformBridge {
    val preferences: IPlatformPreferences = AndroidPlatformPreferences
    val translator: IPlatformTranslator = AndroidPlatformTranslator
    val voiceController: IPlatformVoiceController = AndroidPlatformVoiceController
}

