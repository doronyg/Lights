package com.doronyg.mylightscontroller.platform.android

import com.doronyg.mylightscontroller.platform.common.PlatformBridge

/**
 * Initializes all Android platform implementations for the PlatformBridge.
 *
 * Must be called once during app startup, typically in MainActivity.onCreate(),
 * before any platform services are accessed.
 */
object AndroidPlatformInitializer {
    fun initialize() {
        PlatformBridge.preferences = AndroidPlatformPreferences
        PlatformBridge.translator = AndroidPlatformTranslator
        PlatformBridge.voiceController = AndroidPlatformVoiceController
        PlatformBridge.defaultConfig = AndroidPlatformDefaultConfig
        PlatformBridge.logger = AndroidPlatformLogger
        PlatformBridge.permissions = AndroidPlatformPermissions
        PlatformBridge.dispatchers = AndroidPlatformDispatchers()
    }
}

