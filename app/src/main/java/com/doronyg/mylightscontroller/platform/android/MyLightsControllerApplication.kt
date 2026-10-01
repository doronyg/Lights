package com.doronyg.mylightscontroller.platform.android

import android.app.Application
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.utils.Utils
import kotlinx.coroutines.CoroutineScope

/**
 * Application entry point - initializes platform bridge and app-level services before any code runs.
 * This ensures platform bridge is ready for broadcast receivers, services, and activities.
 */
class MyLightsControllerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize in order:
        // 1. Platform bridge - needed by everything else
        AndroidPlatformInitializer.initialize()

        // 2. Set app context - needed by Utils
        Utils.appContext = applicationContext

        // 3. Create main UI scope using platform-agnostic dispatcher
        Utils.mainScope = CoroutineScope(PlatformBridge.dispatchers.uiDispatcher())

        // 4. Activity lifecycle tracking - for permission requests
        AndroidActivityHolder.registerWithApplication(this)
    }
}