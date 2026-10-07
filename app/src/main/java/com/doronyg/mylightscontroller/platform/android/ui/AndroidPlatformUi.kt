package com.doronyg.mylightscontroller.platform.android.ui

import androidx.activity.compose.BackHandler as AndroidBackHandler
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.doronyg.mylightscontroller.platform.android.AndroidPlatformQrScanner
import com.doronyg.mylightscontroller.ui.PlatformQrScanner
import com.doronyg.mylightscontroller.ui.PlatformUi

/**
 * Android implementation of the shared UI's platform hooks.
 * Provided to the shared UI via LocalPlatformUi (see MainFragment).
 */
object AndroidPlatformUi : PlatformUi {

    @Composable
    override fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
        AndroidBackHandler(enabled = enabled, onBack = onBack)
    }

    @Composable
    override fun rememberQrScanner(): PlatformQrScanner {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        return remember {
            val previewView = PreviewView(context)
            PlatformQrScanner(
                scanner = AndroidPlatformQrScanner(context, lifecycleOwner, previewView),
                preview = { modifier -> AndroidView(factory = { previewView }, modifier = modifier) }
            )
        }
    }
}

