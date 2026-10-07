package com.doronyg.mylightscontroller.platform.desktop

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.ui.AppYellow
import com.doronyg.mylightscontroller.platform.common.IPlatformDataExporter
import com.doronyg.mylightscontroller.platform.common.IPlatformDefaultConfig
import com.doronyg.mylightscontroller.platform.common.IPlatformDispatchers
import com.doronyg.mylightscontroller.platform.common.IPlatformLogger
import com.doronyg.mylightscontroller.platform.common.IPlatformMockDataProvider
import com.doronyg.mylightscontroller.platform.common.IPlatformPermissions
import com.doronyg.mylightscontroller.platform.common.IPlatformPreferences
import com.doronyg.mylightscontroller.platform.common.IPlatformQrScanner
import com.doronyg.mylightscontroller.platform.common.IPlatformTranslator
import com.doronyg.mylightscontroller.platform.common.IPlatformVoiceController
import com.doronyg.mylightscontroller.platform.common.Permission
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.ui.PlatformQrScanner
import com.doronyg.mylightscontroller.ui.PlatformUi
import com.doronyg.mylightscontroller.utils.MockUtils
import com.doronyg.mylightscontroller.utils.StringCodec
import com.doronyg.mylightscontroller.utils.Utils
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.time.LocalTime
import java.util.Properties
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** App data directory: ~/.mylightscontroller (owner-only access: it holds the HA token). */
internal val appDir: File = File(System.getProperty("user.home"), ".mylightscontroller").apply {
    mkdirs()
    restrictToOwner(this, "rwx------")
}

/** Best effort: POSIX file systems only (macOS/Linux); a no-op elsewhere. */
internal fun restrictToOwner(file: File, permissions: String) {
    runCatching { Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString(permissions)) }
}

/** Wires all desktop implementations into [PlatformBridge]. Call once, before any other app code. */
object DesktopPlatformInitializer {
    fun initialize() {
        PlatformBridge.preferences = DesktopPlatformPreferences
        PlatformBridge.translator = DesktopPlatformTranslator
        PlatformBridge.voiceController = DesktopPlatformVoiceController
        PlatformBridge.defaultConfig = DesktopPlatformDefaultConfig
        PlatformBridge.logger = DesktopPlatformLogger
        PlatformBridge.permissions = DesktopPlatformPermissions
        PlatformBridge.dispatchers = DesktopPlatformDispatchers
        PlatformBridge.mockDataProvider = DesktopMockDataProvider
        PlatformBridge.dataExporter = DesktopDataExporter
        PlatformBridge.httpEngine = OkHttp
    }
}

/**
 * Properties file in [appDir]. (java.util.prefs is not used because it limits values
 * to 8 KB, and the mock-devices JSON can be larger.)
 */
object DesktopPlatformPreferences : IPlatformPreferences {
    private val file = File(appDir, "settings.properties")
    private val props = Properties().apply {
        if (file.exists()) file.inputStream().use { load(it) }
    }

    @Synchronized
    private fun get(key: String): String? = props.getProperty(key)

    @Synchronized
    private fun put(key: String, value: String) {
        props.setProperty(key, value)
        file.outputStream().use { props.store(it, "Lights settings") }
        restrictToOwner(file, "rw-------")
    }

    override fun booleanPref(key: String, default: Boolean) = object : ReadWriteProperty<Any, Boolean> {
        override fun getValue(thisRef: Any, property: KProperty<*>) =
            get(key)?.toBooleanStrictOrNull() ?: default

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) =
            put(key, value.toString())
    }

    override fun intPref(key: String, default: Int) = object : ReadWriteProperty<Any, Int> {
        override fun getValue(thisRef: Any, property: KProperty<*>) =
            get(key)?.toIntOrNull() ?: default

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) =
            put(key, value.toString())
    }

    override fun stringPref(key: String, default: String, codec: StringCodec) =
        object : ReadWriteProperty<Any, String> {
            override fun getValue(thisRef: Any, property: KProperty<*>): String {
                val raw = get(key) ?: return default
                return runCatching { codec.decode(raw) }.getOrDefault(default)
            }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: String) =
                put(key, runCatching { codec.encode(value) }.getOrDefault(value))
        }
}

/** No on-device translation on desktop yet: report English so AppTranslator short-circuits. */
object DesktopPlatformTranslator : IPlatformTranslator {
    override fun deviceLanguage() = "en"
    override suspend fun initializeTranslator(language: String) = Unit
    override suspend fun translateText(text: String) = text
    override fun getFromCache(text: String): String? = null
    override fun saveToCache(original: String, translated: String) = Unit
    override fun close() = Unit
}

/** Hotword/voice control is Android-only for now. */
object DesktopPlatformVoiceController : IPlatformVoiceController {
    override fun start() = Utils.log("Voice control is not available on desktop")
    override fun stop() = Unit
}

/** Defaults from environment variables (handy for development). */
object DesktopPlatformDefaultConfig : IPlatformDefaultConfig {
    override val defaultHaUrl: String = System.getenv("HA_URL") ?: ""
    override val defaultHaToken: String = System.getenv("HA_TOKEN") ?: ""
    override val defaultPorcupineApiKey: String = System.getenv("PORCUPINE_API_KEY") ?: ""
    override val isDebug: Boolean = System.getenv("MYLIGHTS_DEBUG") == "true"
}

object DesktopPlatformLogger : IPlatformLogger {
    private fun line(level: String, tag: String, message: String) =
        "${LocalTime.now()} $level/$tag: $message"

    override fun debug(tag: String, message: String) = println(line("D", tag, message))
    override fun info(tag: String, message: String) = println(line("I", tag, message))
    override fun warning(tag: String, message: String) = System.err.println(line("W", tag, message))
    override fun error(tag: String, message: String, exception: Throwable?) {
        System.err.println(line("E", tag, message))
        exception?.printStackTrace()
    }
}

/** Camera/microphone features are not supported on desktop yet: everything is denied. */
object DesktopPlatformPermissions : IPlatformPermissions {
    override suspend fun hasPermission(permission: Permission) = false
    override fun requestPermission(permission: Permission) =
        PlatformBridge.onPermissionResult(permission, granted = false)
}

object DesktopPlatformDispatchers : IPlatformDispatchers {
    override fun uiDispatcher(): CoroutineDispatcher = Dispatchers.Main // Swing EDT (coroutines-swing)
    override fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
    override fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

object DesktopMockDataProvider : IPlatformMockDataProvider {
    override fun writeFile(fileName: String, content: String): String? = try {
        File(appDir, fileName).apply { writeText(content) }.absolutePath
            .also { Utils.log("File written to: $it") }
    } catch (e: Exception) {
        Utils.logException(e, "Failed to write file: $fileName")
        null
    }

    /** Bundled files come from the classpath (desktopApp reuses app/src/main/assets). */
    override suspend fun readAssetFile(fileName: String): String = withContext(Dispatchers.IO) {
        val stream = DesktopMockDataProvider::class.java.getResourceAsStream("/$fileName")
            ?: throw IllegalStateException("Resource not found: $fileName")
        stream.bufferedReader().use { it.readText() }
    }
}

/** Saves the export and reveals it in Finder. */
object DesktopDataExporter : IPlatformDataExporter {
    override fun exportHaData() {
        MockUtils.saveMockData { path ->
            if (path == null) return@saveMockData
            runCatching { Desktop.getDesktop().open(File(path).parentFile) }
                .onFailure { Utils.logException(it, "Could not open export folder") }
        }
    }
}

/** No camera on desktop: the scanner reports an error so QrScanScreen shows it. */
private class UnsupportedQrScanner : IPlatformQrScanner {
    override val scannedValue: StateFlow<String?> = MutableStateFlow(null)
    override val isScanning: StateFlow<Boolean> = MutableStateFlow(false)
    override val errorMessage: StateFlow<String?> =
        MutableStateFlow("QR scanning is not available on desktop. Please use manual setup.")

    override suspend fun startScanning() = Unit
    override fun stopScanning() = Unit
    override fun reset() = Unit
}

object DesktopPlatformUi : PlatformUi {
    /** No system back on desktop; screens have their own back buttons. */
    @Composable
    override fun BackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

    @Composable
    override fun rememberQrScanner(): PlatformQrScanner = remember {
        PlatformQrScanner(scanner = UnsupportedQrScanner(), preview = { modifier -> Box(modifier) })
    }

    // --- Scrollbars ---
    // Mouse users need a visible, draggable scrollbar (wheel/trackpad scrolling works regardless).
    // Always shown while the content is taller than the viewport, in its own gutter.

    private val scrollbarThickness = 8.dp
    private val scrollbarMargin = 2.dp

    override val scrollbarGutter: Dp = scrollbarThickness + scrollbarMargin * 2

    /** Strong app-yellow thumb: the Compose default (12% black) is invisible in dark mode. */
    private val scrollbarStyle = ScrollbarStyle(
        minimalHeight = 24.dp,
        thickness = scrollbarThickness,
        shape = RoundedCornerShape(scrollbarThickness / 2),
        hoverDurationMillis = 200,
        unhoverColor = AppYellow.copy(alpha = 0.75f),
        hoverColor = AppYellow
    )

    @Composable
    private fun Scrollbar(adapter: ScrollbarAdapter, modifier: Modifier) =
        androidx.compose.foundation.VerticalScrollbar(
            adapter = adapter,
            modifier = modifier.padding(horizontal = scrollbarMargin),
            style = scrollbarStyle
        )

    @Composable
    override fun VerticalScrollbar(state: ScrollState, modifier: Modifier) =
        Scrollbar(rememberScrollbarAdapter(state), modifier)

    @Composable
    override fun VerticalScrollbar(state: LazyListState, modifier: Modifier) =
        Scrollbar(rememberScrollbarAdapter(state), modifier)

    @Composable
    override fun VerticalScrollbar(state: LazyGridState, modifier: Modifier) =
        Scrollbar(remember(state) { SpanAwareGridScrollbarAdapter(state) }, modifier)
}

/**
 * Scrollbar adapter for [LazyGridState] that handles items spanning several columns.
 *
 * The Compose Desktop adapter assumes `rows = itemCount / columns`, so full-width items
 * (e.g. dimmable lights that are on) make it underestimate the content height. The thumb
 * then fills the track and is drawn transparent. Here the content height is estimated
 * from the average pixels per item of the visible items, which accounts for spans and
 * for varying row heights.
 */
internal class SpanAwareGridScrollbarAdapter(private val state: LazyGridState) : ScrollbarAdapter {

    private val info get() = state.layoutInfo

    /** Average main-axis size per item (including spacing), from the visible items. */
    private val pixelsPerItem: Double
        get() {
            val visible = info.visibleItemsInfo
            if (visible.isEmpty()) return 0.0
            val first = visible.minBy { it.index }
            val last = visible.maxBy { it.offset.y + it.size.height }
            val extent = (last.offset.y + last.size.height) - first.offset.y + info.mainAxisItemSpacing
            val count = visible.maxOf { it.index } - first.index + 1
            return extent.toDouble() / count
        }

    override val viewportSize: Double
        get() = info.viewportSize.height.toDouble()

    override val contentSize: Double
        get() {
            val total = info.totalItemsCount
            if (total == 0) return 0.0
            return pixelsPerItem * total - info.mainAxisItemSpacing +
                info.beforeContentPadding + info.afterContentPadding
        }

    override val scrollOffset: Double
        get() = state.firstVisibleItemIndex * pixelsPerItem + state.firstVisibleItemScrollOffset

    override suspend fun scrollTo(scrollOffset: Double) {
        val target = scrollOffset.coerceIn(0.0, (contentSize - viewportSize).coerceAtLeast(0.0))
        val distance = target - this.scrollOffset
        if (kotlin.math.abs(distance) <= viewportSize) {
            state.scrollBy(distance.toFloat())
        } else {
            val ppi = pixelsPerItem.takeIf { it > 0 } ?: return
            val index = (target / ppi).toInt().coerceIn(0, (info.totalItemsCount - 1).coerceAtLeast(0))
            state.scrollToItem(index, (target - index * ppi).toInt().coerceAtLeast(0))
        }
    }
}
