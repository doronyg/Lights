package com.doronyg.mylightscontroller.platform.common

/**
 * Common interface for platform-specific background voice control.
 *
 * Each platform decides how voice control runs (Android: foreground service).
 * Platform-specific context/setup is handled at initialization time.
 */
interface IPlatformVoiceController {
    fun start()
    fun stop()
}

