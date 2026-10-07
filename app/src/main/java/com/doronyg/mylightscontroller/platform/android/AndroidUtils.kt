package com.doronyg.mylightscontroller.platform.android

import android.content.Context
import kotlinx.coroutines.CoroutineScope

/**
 * Android-specific utilities.
 * Contains Android Context and CoroutineScope management.
 * This should only be accessed from Android platform code.
 */
object AndroidUtils {

    /**
     * Application context - set during Android app initialization.
     * Should be set in MainActivity.onCreate() via AndroidPlatformInitializer.initialize()
     */
    var appContext: Context? = null

    /**
     * Main thread CoroutineScope - set during Android app initialization.
     * Used for UI-related operations that must run on the main thread.
     * Should be set in MainActivity.onCreate()
     */
    var mainScope: CoroutineScope? = null
}

