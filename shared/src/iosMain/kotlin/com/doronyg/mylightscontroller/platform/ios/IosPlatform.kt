@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

package com.doronyg.mylightscontroller.platform.ios

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSBundle
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSLog
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import kotlin.experimental.ExperimentalNativeApi
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Wires all iOS implementations into [PlatformBridge]. Call once, before any other app code. */
object IosPlatformInitializer {
    fun initialize() {
        PlatformBridge.preferences = IosPlatformPreferences
        PlatformBridge.translator = IosPlatformTranslator
        PlatformBridge.voiceController = IosPlatformVoiceController
        PlatformBridge.defaultConfig = IosPlatformDefaultConfig
        PlatformBridge.logger = IosPlatformLogger
        PlatformBridge.permissions = IosPlatformPermissions
        PlatformBridge.dispatchers = IosPlatformDispatchers
        PlatformBridge.mockDataProvider = IosMockDataProvider
        PlatformBridge.dataExporter = IosDataExporter
        PlatformBridge.httpEngine = Darwin
        // haEndpoint: the default (DirectHaEndpoint), like Android and desktop
    }
}

/**
 * Settings in NSUserDefaults (the iOS equivalent of SharedPreferences), stored as strings.
 * Note: like on Android, the HA token is only Base64-encoded here; the Keychain would be safer.
 */
object IosPlatformPreferences : IPlatformPreferences {
    private val defaults get() = NSUserDefaults.standardUserDefaults

    private fun get(key: String): String? = defaults.stringForKey(key)
    private fun put(key: String, value: String) = defaults.setObject(value, forKey = key)

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

/** No translation on iOS yet: report English so AppTranslator short-circuits (as on desktop/web). */
object IosPlatformTranslator : IPlatformTranslator {
    override fun deviceLanguage() = "en"
    override suspend fun initializeTranslator(language: String) = Unit
    override suspend fun translateText(text: String) = text
    override fun getFromCache(text: String): String? = null
    override fun saveToCache(original: String, translated: String) = Unit
    override fun close() = Unit
}

/** Hotword/voice control is Android-only for now. */
object IosPlatformVoiceController : IPlatformVoiceController {
    override fun start() = Utils.log("Voice control is not available on iOS")
    override fun stop() = Unit
}

/** No build-time HA defaults on iOS: set up via onboarding (manual or demo). */
object IosPlatformDefaultConfig : IPlatformDefaultConfig {
    override val defaultHaUrl: String = ""
    override val defaultHaToken: String = ""
    override val defaultPorcupineApiKey: String = ""
    override val isDebug: Boolean = Platform.isDebugBinary
}

/** NSLog: shows in the Xcode console and in Console.app (also for a device). */
object IosPlatformLogger : IPlatformLogger {
    // No varargs: a Kotlin String passed as a C vararg for "%@" isn't an NSString and crashes.
    // Pass the text as the format itself, with '%' escaped so it's printed literally.
    private fun log(line: String) = NSLog(line.replace("%", "%%"))
    override fun debug(tag: String, message: String) = log("D/$tag: $message")
    override fun info(tag: String, message: String) = log("I/$tag: $message")
    override fun warning(tag: String, message: String) = log("W/$tag: $message")
    override fun error(tag: String, message: String, exception: Throwable?) =
        log("E/$tag: $message" + (exception?.let { "\n" + it.stackTraceToString() } ?: ""))
}

/** Camera/microphone features are not implemented on iOS yet: everything is denied. */
object IosPlatformPermissions : IPlatformPermissions {
    override suspend fun hasPermission(permission: Permission) = false
    override fun requestPermission(permission: Permission) =
        PlatformBridge.onPermissionResult(permission, granted = false)
}

object IosPlatformDispatchers : IPlatformDispatchers {
    override fun uiDispatcher(): CoroutineDispatcher = Dispatchers.Main
    override fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
    override fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

object IosMockDataProvider : IPlatformMockDataProvider {
    private val documentsDir: String
        get() = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
            .first() as String

    /** Writes to the app's Documents directory. */
    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun writeFile(fileName: String, content: String): String? {
        val path = "$documentsDir/$fileName"
        val ok = (content as NSString).writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        return if (ok) path else null.also { Utils.log("Failed to write file: $path") }
    }

    /** Bundled files (e.g. ha_lights_demo.json) are app-bundle resources (see iosApp/project.yml). */
    override suspend fun readAssetFile(fileName: String): String {
        val path = NSBundle.mainBundle.pathForResource(fileName.substringBeforeLast('.'), fileName.substringAfterLast('.'))
            ?: error("Resource not found in app bundle: $fileName")
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
            ?: error("Could not read resource: $fileName")
    }
}

/** Exports the HA data as a JSON file and opens the iOS share sheet (Mail, Files, AirDrop, ...). */
object IosDataExporter : IPlatformDataExporter {
    override fun exportHaData() = MockUtils.saveMockData { path ->
        if (path == null) return@saveMockData
        val presenter = topViewController() ?: return@saveMockData
        val share = UIActivityViewController(listOf(NSURL.fileURLWithPath(path)), null)
        share.popoverPresentationController?.sourceView = presenter.view // required on iPad
        presenter.presentViewController(share, animated = true, completion = null)
    }

    private fun topViewController(): UIViewController? {
        val scene = UIApplication.sharedApplication.connectedScenes
            .firstOrNull { it is UIWindowScene } as? UIWindowScene
        var top = scene?.keyWindow?.rootViewController
        while (top?.presentedViewController != null) top = top.presentedViewController
        return top
    }
}

/** No camera scanning on iOS yet: the scanner reports an error so QrScanScreen shows it. */
private class UnsupportedQrScanner : IPlatformQrScanner {
    override val scannedValue: StateFlow<String?> = MutableStateFlow(null)
    override val isScanning: StateFlow<Boolean> = MutableStateFlow(false)
    override val errorMessage: StateFlow<String?> =
        MutableStateFlow("QR scanning is not available on iOS yet. Use manual setup.")

    override suspend fun startScanning() = Unit
    override fun stopScanning() = Unit
    override fun reset() = Unit
}

/** Touch platform: no scrollbars (PlatformUi defaults). */
object IosPlatformUi : PlatformUi {
    /** iOS has no system back button; screens have their own back buttons. */
    @Composable
    override fun BackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

    @Composable
    override fun rememberQrScanner(): PlatformQrScanner = remember {
        PlatformQrScanner(scanner = UnsupportedQrScanner(), preview = { modifier -> Box(modifier) })
    }
}



