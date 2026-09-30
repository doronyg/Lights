package com.doronyg.mylightscontroller.platform.android

import android.content.Context
import com.doronyg.mylightscontroller.platform.common.IPlatformVoiceController
import com.doronyg.mylightscontroller.services.VoiceControllerService

/**
 * Android implementation of platform-specific voice controller service.
 * Manages Android Service lifecycle for voice control.
 *
 * Future: This will become the `actual` implementation when converting to Kotlin Multiplatform.
 */
object AndroidPlatformVoiceController : IPlatformVoiceController {

    override fun startService(context: Context, boot: Boolean) {
        VoiceControllerService.startService(context, boot)
    }

    override fun stopService(context: Context) {
        VoiceControllerService.stopService(context)
    }
}

