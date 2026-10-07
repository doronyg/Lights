package com.doronyg.mylightscontroller.platform.common

/**
 * Platform-agnostic interface for accessing default configuration values.
 * These are typically build-time constants that come from BuildConfig on Android.
 */
interface IPlatformDefaultConfig {
    /**
     * Default Home Assistant URL from build configuration
     */
    val defaultHaUrl: String

    /**
     * Default Home Assistant token from build configuration
     */
    val defaultHaToken: String

    /**
     * Default Porcupine API key from build configuration
     */
    val defaultPorcupineApiKey: String

    /**
     * True for debug builds (enables developer-only UI).
     */
    val isDebug: Boolean
}

