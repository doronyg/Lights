package com.doronyg.mylightscontroller.platform.android

import android.content.Context
import com.doronyg.mylightscontroller.platform.android.services.VoiceControllerService
import com.doronyg.mylightscontroller.platform.common.IPlatformVoiceController
import com.doronyg.mylightscontroller.utils.Utils

/**
 * Android implementation of platform-specific voice controller service.
 * Manages Android Service lifecycle for voice control.
 * Context is stored internally at initialization time.
 *
 * Future: This will become the `actual` implementation when converting to Kotlin Multiplatform.
 */
object AndroidPlatformVoiceController : IPlatformVoiceController {

    private var appContext: Context? = null

    /**
     * Initialize the voice controller with application context.
     * Must be called once during app startup (in AndroidPlatformInitializer).
     */
    fun initialize(context: Context) {
        appContext = context
    }

    override fun start() {
        val context = appContext ?: run {
            Utils.log("VoiceController not initialized")
            return
        }
        VoiceControllerService.startService(context, boot = false)
    }

    override fun stop() {
        val context = appContext ?: run {
            Utils.log("VoiceController not initialized")
            return
        }
        VoiceControllerService.stopService(context)
    }
}

