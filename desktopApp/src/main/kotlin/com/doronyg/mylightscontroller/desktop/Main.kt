package com.doronyg.mylightscontroller.desktop

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.doronyg.mylightscontroller.ha.HomeAssistantRepository
import com.doronyg.mylightscontroller.platform.desktop.DesktopPlatformInitializer
import com.doronyg.mylightscontroller.platform.desktop.DesktopPlatformUi
import com.doronyg.mylightscontroller.ui.LocalPlatformUi
import com.doronyg.mylightscontroller.ui.MainScreen
import com.doronyg.mylightscontroller.ui.MyTheme
import com.doronyg.mylightscontroller.utils.Utils
import org.jetbrains.skia.Image
import java.awt.Taskbar

private const val APP_NAME = "Lights"

fun main() {
    // macOS menu bar app name when running unpackaged (must be set before AWT starts)
    System.setProperty("apple.awt.application.name", APP_NAME)
    DesktopPlatformInitializer.initialize()
    val appIcon = loadAppIcon()
    appIcon?.let(::setDockIcon)

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = APP_NAME,
            icon = appIcon?.let { BitmapPainter(it) },
            state = rememberWindowState(width = 420.dp, height = 820.dp)
        ) {
            // Same as MainFragment.onResume/onPause: poll HA while the window is shown
            DisposableEffect(Unit) {
                val token = HomeAssistantRepository.instance.registerUi()
                onDispose { HomeAssistantRepository.instance.unregisterUi(token) }
            }

            CompositionLocalProvider(LocalPlatformUi provides DesktopPlatformUi) {
                MyTheme {
                    MainScreen()
                }
            }
        }
    }
}

/** The Android launcher icon (copied onto the classpath by the build). WebP is decoded by Skia. */
private fun loadAppIcon(): ImageBitmap? = runCatching {
    val bytes = object {}.javaClass.getResourceAsStream("/ic_bee_launcher.webp")!!.use { it.readBytes() }
    Image.makeFromEncoded(bytes).toComposeImageBitmap()
}.onFailure { Utils.logException(it, "Could not load app icon") }.getOrNull()

/** The window icon isn't used for the macOS Dock when running unpackaged; set it explicitly. */
private fun setDockIcon(icon: ImageBitmap) {
    if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
        runCatching { Taskbar.getTaskbar().iconImage = icon.toAwtImage() }
    }
}
