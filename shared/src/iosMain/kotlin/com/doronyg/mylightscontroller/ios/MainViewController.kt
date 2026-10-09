package com.doronyg.mylightscontroller.ios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.doronyg.mylightscontroller.ha.HomeAssistantRepository
import com.doronyg.mylightscontroller.platform.ios.IosPlatformInitializer
import com.doronyg.mylightscontroller.platform.ios.IosPlatformUi
import com.doronyg.mylightscontroller.ui.LocalPlatformUi
import com.doronyg.mylightscontroller.ui.MainScreen
import com.doronyg.mylightscontroller.ui.MyTheme
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIViewController

private var started = false

/**
 * Entry point for the iOS app (called from Swift as `MainViewControllerKt.MainViewController()`).
 * Returns the whole app UI as a UIViewController.
 */
@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController {
    if (!started) {
        started = true
        IosPlatformInitializer.initialize()
        pollHomeAssistantWhileActive()
    }

    return ComposeUIViewController {
        CompositionLocalProvider(LocalPlatformUi provides IosPlatformUi) {
            MyTheme {
                // The Swift host draws edge to edge; keep the UI out of the notch/home indicator
                // (same as the Android activity padding the system bars).
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    MainScreen()
                }
            }
        }
    }
}

/** Same as MainFragment.onResume/onPause: poll HA only while the app is in the foreground. */
private fun pollHomeAssistantWhileActive() {
    val repository = HomeAssistantRepository.instance
    var token: Int? = repository.registerUi()

    val center = NSNotificationCenter.defaultCenter
    val queue = NSOperationQueue.mainQueue
    center.addObserverForName(UIApplicationDidBecomeActiveNotification, null, queue) { _ ->
        if (token == null) token = repository.registerUi()
    }
    center.addObserverForName(UIApplicationDidEnterBackgroundNotification, null, queue) { _ ->
        token?.let { repository.unregisterUi(it) }
        token = null
    }
}

