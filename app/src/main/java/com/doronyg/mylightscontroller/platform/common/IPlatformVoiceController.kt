package com.doronyg.mylightscontroller.platform.common

import android.content.Context

/**
 * Common interface for platform-specific voice controller service.
 *
 * This defines the contract that will be implemented per-platform.
 * Future: will become `expect object` when converting to Kotlin Multiplatform.
 */
interface IPlatformVoiceController {
    fun startService(context: Context, boot: Boolean = false)
    fun stopService(context: Context)
}

