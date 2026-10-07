package com.doronyg.mylightscontroller.platform.android

import android.content.Context
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Initializes all Android platform implementations for the PlatformBridge.
 *
 * Must be called once during app startup, typically in MainActivity.onCreate(),
 * before any platform services are accessed.
 */
object AndroidPlatformInitializer {
    fun initialize(context: Context) {
        // Initialize platform bridge services
        PlatformBridge.preferences = AndroidPlatformPreferences
        PlatformBridge.translator = AndroidPlatformTranslator
        PlatformBridge.voiceController = AndroidPlatformVoiceController
        PlatformBridge.defaultConfig = AndroidPlatformDefaultConfig
        PlatformBridge.logger = AndroidPlatformLogger
        PlatformBridge.permissions = AndroidPlatformPermissions
        PlatformBridge.dispatchers = AndroidPlatformDispatchers()
        PlatformBridge.mockDataProvider = AndroidMockDataProvider
        PlatformBridge.dataExporter = AndroidEmailHelper
        PlatformBridge.httpEngine = OkHttp

        // Initialize platform implementations that need context
        AndroidPlatformVoiceController.initialize(context)
    }
}

