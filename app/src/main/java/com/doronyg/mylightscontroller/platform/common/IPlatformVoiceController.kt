package com.doronyg.mylightscontroller.platform.common

/**
 * Common interface for platform-specific voice controller service.
 *
 * This defines the contract that will be implemented per-platform.
 * Platform-specific context/setup is handled at initialization time.
 * Future: will become `expect object` when converting to Kotlin Multiplatform.
 */
interface IPlatformVoiceController {
    fun startService(boot: Boolean = false)
    fun stopService()
}

