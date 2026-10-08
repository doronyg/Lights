package com.doronyg.mylightscontroller.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.ha.HaAttributes
import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.platform.desktop.DesktopPlatformInitializer
import com.doronyg.mylightscontroller.platform.desktop.DesktopPlatformUi
import com.doronyg.mylightscontroller.ui.DeviceGrid
import com.doronyg.mylightscontroller.ui.DevicesStateHolder
import com.doronyg.mylightscontroller.ui.LocalPlatformUi
import com.doronyg.mylightscontroller.ui.MyTheme
import com.doronyg.mylightscontroller.ui.ScrollableColumn
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Renders a tall [ScrollableColumn] offscreen and checks that the desktop scrollbar
 * is actually drawn (and visible against the background) in the right-hand gutter.
 */
class ScrollbarRenderTest {

    @Test
    fun scrollbarIsVisibleInGutter() {
        val width = 300
        val height = 300
        val scene = ImageComposeScene(width, height, Density(1f)) {
            CompositionLocalProvider(LocalPlatformUi provides DesktopPlatformUi) {
                MyTheme(darkTheme = true) {
                    Surface(Modifier.fillMaxSize()) {
                        ScrollableColumn(Modifier.fillMaxSize()) {
                            repeat(40) {
                                Box(Modifier.fillMaxWidth().height(40.dp)) { Text("Row $it") }
                            }
                        }
                    }
                }
            }
        }
        // A few frames so the scrollbar adapter sees measured content
        var image = scene.render(0)
        repeat(3) { image = scene.render((it + 1) * 16_000_000L) }
        scene.close()

        val bitmap = Bitmap.makeFromImage(image)
        val background = bitmap.getColor(width / 2, height - 1)
        val gutterX = width - 6 // center of the 12dp gutter
        val gutterColors = (0 until height step 10).map { bitmap.getColor(gutterX, it) }
        println("background=${hex(background)} gutter=${gutterColors.map(::hex)}")

        assertTrue(gutterColors.any(::isYellowish), "Yellow scrollbar thumb not drawn in gutter")
    }

    @Test
    fun scrollbarIsVisibleOnDeviceGrid() {
        DesktopPlatformInitializer.initialize()
        val devices = (0 until 30).map { i ->
            HaDevice(
                entityId = "light.l$i",
                state = if (i % 3 == 0) "on" else "off",
                attributes = HaAttributes(
                    friendlyName = "Light $i",
                    supportedColorModes = listOf("brightness"),
                    brightness = 128
                )
            )
        }
        val width = 420
        val height = 700
        val scene = ImageComposeScene(width, height, Density(1f)) {
            CompositionLocalProvider(LocalPlatformUi provides DesktopPlatformUi) {
                MyTheme(darkTheme = true) {
                    Surface(Modifier.fillMaxSize()) {
                        DeviceGrid(devices, remember { DevicesStateHolder() })
                    }
                }
            }
        }
        var image = scene.render(0)
        repeat(3) { image = scene.render((it + 1) * 16_000_000L) }
        scene.close()

        val bitmap = Bitmap.makeFromImage(image)
        val gutterX = width - 6
        val gutterColors = (0 until height step 20).map { bitmap.getColor(gutterX, it) }
        println("grid gutter=${gutterColors.map(::hex)}")
        assertTrue(gutterColors.any(::isYellowish), "Yellow scrollbar thumb not drawn on DeviceGrid")
    }

    /** App yellow (#F2C94C) blended over a dark surface: strong red/green, weak blue. */
    private fun isYellowish(c: Int): Boolean {
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        return r > 150 && g > 120 && b < 110
    }

    @Test
    fun scrollbarStaysAboveBottomBar() {
        DesktopPlatformInitializer.initialize()
        val devices = (0 until 30).map { i ->
            HaDevice(
                entityId = "light.l$i",
                state = "off",
                attributes = HaAttributes(friendlyName = "Light $i")
            )
        }
        val width = 420
        val height = 700
        var barTop = -1
        val scene = ImageComposeScene(width, height, Density(1f)) {
            CompositionLocalProvider(LocalPlatformUi provides DesktopPlatformUi) {
                MyTheme(darkTheme = true) {
                    androidx.compose.material3.Scaffold(
                        bottomBar = {
                            androidx.compose.material3.NavigationBar(
                                windowInsets = androidx.compose.foundation.layout.WindowInsets(),
                                modifier = Modifier.onGloballyPositioned {
                                    barTop = it.positionInRoot().y.toInt()
                                }
                            ) {}
                        }
                    ) { padding ->
                        Box(Modifier.padding(padding).fillMaxSize().padding(12.dp)) {
                            DeviceGrid(devices, remember { DevicesStateHolder() })
                        }
                    }
                }
            }
        }
        scene.render(0)
        // Scroll to the very end with the mouse wheel
        repeat(20) {
            scene.sendPointerEvent(
                PointerEventType.Scroll, Offset(width / 2f, 200f), scrollDelta = Offset(0f, 50f)
            )
            scene.render((it + 1) * 16_000_000L)
        }
        var image = scene.render(1_000_000_000L)
        image = scene.render(1_100_000_000L)
        scene.close()

        val bitmap = Bitmap.makeFromImage(image)
        val gutterX = width - 12 - 6 // 12dp outer padding, then gutter center
        val yellowRows = (0 until height).filter { isYellowish(bitmap.getColor(gutterX, it)) }
        println("barTop=$barTop thumb=${yellowRows.minOrNull()}..${yellowRows.maxOrNull()}")
        assertTrue(yellowRows.isNotEmpty(), "Thumb not drawn")
        assertTrue(yellowRows.max() < barTop, "Thumb overlaps bottom bar")
    }

    @Test
    fun realMainScreenScrollbarStaysAboveBottomBar() {
        DesktopPlatformInitializer.initialize()
        PlatformBridge.preferences = InMemoryPreferences() // don't touch ~/.mylightscontroller
        kotlinx.coroutines.runBlocking { com.doronyg.mylightscontroller.onboarding.applyDemoConfig() }

        val width = 420
        val height = 600
        val scene = ImageComposeScene(width, height, Density(2f)) {
            CompositionLocalProvider(LocalPlatformUi provides DesktopPlatformUi) {
                MyTheme(darkTheme = true) { com.doronyg.mylightscontroller.ui.MainScreen() }
            }
        }
        var t = 0L
        fun frame() = scene.render(t).also { t += 16_000_000L }
        repeat(60) { frame(); Thread.sleep(50) } // let devices load
        repeat(30) {
            scene.sendPointerEvent(PointerEventType.Scroll, Offset(width / 2f, 150f), scrollDelta = Offset(0f, 50f))
            frame()
        }
        repeat(10) { frame(); Thread.sleep(20) }
        val image = frame()
        scene.close()

        val bitmap = Bitmap.makeFromImage(image)
        // Thumb: yellow pixels in the right-hand strip (content padding + gutter)
        val yellowRows = (0 until height).filter { y ->
            (width - 1 downTo width - 60).any { isYellowish(bitmap.getColor(it, y)) }
        }
        // Bottom bar: first row (near the bottom) with the bar's container color
        val barColor = bitmap.getColor(width - 20, height - 1)
        val barTop = (height - 200 until height).first { bitmap.getColor(width - 20, it) == barColor }
        println("main thumb rows=${yellowRows.minOrNull()}..${yellowRows.maxOrNull()} barTop=$barTop")
        assertTrue(yellowRows.isNotEmpty(), "Thumb not drawn on MainScreen")
        assertTrue(yellowRows.max() < barTop, "Thumb overlaps the bottom bar")
    }

    private class InMemoryPreferences : com.doronyg.mylightscontroller.platform.common.IPlatformPreferences {
        private val map = mutableMapOf<String, Any>()
        private fun <T : Any> pref(key: String, default: T) = object : kotlin.properties.ReadWriteProperty<Any, T> {
            @Suppress("UNCHECKED_CAST")
            override fun getValue(thisRef: Any, property: kotlin.reflect.KProperty<*>) = (map[key] as T?) ?: default
            override fun setValue(thisRef: Any, property: kotlin.reflect.KProperty<*>, value: T) { map[key] = value }
        }
        override fun booleanPref(key: String, default: Boolean) = pref(key, default)
        override fun intPref(key: String, default: Int) = pref(key, default)
        override fun stringPref(key: String, default: String, codec: com.doronyg.mylightscontroller.utils.StringCodec) = pref(key, default)
    }

    /** Full-width items (dimmable lights that are on) used to hide the thumb: see SpanAwareGridScrollbarAdapter. */
    @Test
    fun scrollbarIsVisibleWithFullWidthItems() {
        DesktopPlatformInitializer.initialize()
        val devices = (0 until 12).map { i ->
            HaDevice(
                entityId = "light.l$i",
                state = if (i < 8) "on" else "off",
                attributes = HaAttributes(
                    friendlyName = "Light $i",
                    supportedColorModes = listOf("brightness"),
                    brightness = 128
                )
            )
        }
        val width = 420
        val height = 700
        val scene = ImageComposeScene(width, height, Density(1f)) {
            CompositionLocalProvider(LocalPlatformUi provides DesktopPlatformUi) {
                MyTheme(darkTheme = true) {
                    Surface(Modifier.fillMaxSize()) {
                        DeviceGrid(devices, remember { DevicesStateHolder() })
                    }
                }
            }
        }
        var image = scene.render(0)
        repeat(3) { image = scene.render((it + 1) * 16_000_000L) }
        scene.close()

        val bitmap = Bitmap.makeFromImage(image)
        val gutterX = width - 6
        val yellowRows = (0 until height).count { isYellowish(bitmap.getColor(gutterX, it)) }
        println("full-width grid thumb rows=$yellowRows")
        // Real content is ~1.6x the viewport; the stock adapter estimated ~1.1x (thumb 628/700, or hidden)
        assertTrue(yellowRows in 1 until (height * 0.8).toInt(), "Thumb hidden or too large with full-width items")
    }

    private fun hex(c: Int) = "%08X".format(c)
}
