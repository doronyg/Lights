package com.doronyg.mylightscontroller.platform

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.doronyg.mylightscontroller.platform.android.AndroidPlatformDefaultConfig
import com.doronyg.mylightscontroller.platform.android.AndroidPlatformLogger
import com.doronyg.mylightscontroller.platform.android.AndroidPlatformPermissions
import com.doronyg.mylightscontroller.platform.android.AndroidPlatformPreferences
import com.doronyg.mylightscontroller.platform.android.AndroidPlatformQrScanner
import com.doronyg.mylightscontroller.platform.android.AndroidPlatformTranslator
import com.doronyg.mylightscontroller.platform.android.AndroidPlatformVoiceController
import com.doronyg.mylightscontroller.platform.common.IPlatformDefaultConfig
import com.doronyg.mylightscontroller.platform.common.IPlatformLogger
import com.doronyg.mylightscontroller.platform.common.IPlatformPermissions
import com.doronyg.mylightscontroller.platform.common.IPlatformPreferences
import com.doronyg.mylightscontroller.platform.common.IPlatformQrScanner
import com.doronyg.mylightscontroller.platform.common.IPlatformTranslator
import com.doronyg.mylightscontroller.platform.common.IPlatformVoiceController

/**
 * Single access point for all platform-specific implementations.
 *
 * This bridge holds references to platform implementations and provides them via common interfaces.
 * The app uses interfaces defined in `platform.common` package.
 *
 * Future: When converting to Kotlin Multiplatform, this will be replaced by the expect/actual
 * mechanism, and each platform will provide its own implementations automatically.
 */
object PlatformBridge {
    val preferences: IPlatformPreferences = AndroidPlatformPreferences
    val translator: IPlatformTranslator = AndroidPlatformTranslator
    val voiceController: IPlatformVoiceController = AndroidPlatformVoiceController
    val defaultConfig: IPlatformDefaultConfig = AndroidPlatformDefaultConfig
    val logger: IPlatformLogger = AndroidPlatformLogger

    /**
     * Create a QR scanner instance. Needs to be called with the appropriate context and lifecycle.
     * For Android, pass the current Activity's lifecycle for proper resource cleanup.
     * @param previewView Optional PreviewView to display camera preview (required for actual scanning)
     */
    fun createQrScanner(
        context: Context,
        lifecycleOwner: LifecycleOwner? = null,
        previewView: PreviewView? = null
    ): IPlatformQrScanner {
        return AndroidPlatformQrScanner(context, lifecycleOwner, previewView)
    }

    /**
     * Create a permissions handler. Needs context to check/request permissions.
     */
    fun createPermissions(context: Context): IPlatformPermissions {
        return AndroidPlatformPermissions(context)
    }
}

