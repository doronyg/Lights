@file:OptIn(ExperimentalWasmJsInterop::class)

package com.doronyg.mylightscontroller.platform.web

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.doronyg.mylightscontroller.platform.common.IHaEndpoint
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
import com.doronyg.mylightscontroller.ui.SkikoPlatformUi
import com.doronyg.mylightscontroller.utils.MockUtils
import com.doronyg.mylightscontroller.utils.StringCodec
import com.doronyg.mylightscontroller.utils.Utils
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Wires all web implementations into [PlatformBridge]. Call once, before any other app code. */
object WebPlatformInitializer {
    fun initialize() {
        PlatformBridge.preferences = WebPlatformPreferences
        PlatformBridge.translator = WebPlatformTranslator
        PlatformBridge.voiceController = WebPlatformVoiceController
        PlatformBridge.defaultConfig = WebPlatformDefaultConfig
        PlatformBridge.logger = WebPlatformLogger
        PlatformBridge.permissions = WebPlatformPermissions
        PlatformBridge.dispatchers = WebPlatformDispatchers
        PlatformBridge.mockDataProvider = WebMockDataProvider
        PlatformBridge.dataExporter = WebDataExporter
        PlatformBridge.httpEngine = Js
        PlatformBridge.haEndpoint = GatewayHaEndpoint
    }
}

/**
 * The web app never talks to Home Assistant directly: requests go to the gateway that serves
 * the app (same origin, so no CORS), and the gateway adds the HA token. The browser never has it.
 */
object GatewayHaEndpoint : IHaEndpoint {
    override val baseUrl: String get() = pageOrigin()
    override fun authorize(request: HttpRequestBuilder) = Unit // the gateway authenticates
    override val credentialsManagedExternally = true

    private val client by lazy { HttpClient(Js) }

    /** Whether the gateway has HA details; null if it couldn't be asked. */
    suspend fun isGatewayConfigured(): Boolean? = try {
        val response = client.get("$baseUrl/gateway/status")
        if (response.status.isSuccess()) response.bodyAsText().trim() == "configured" else null
    } catch (e: Exception) {
        Utils.logException(e, "Gateway status check failed")
        null
    }

    /** First-run setup: the gateway checks the details against HA and stores them. */
    override suspend fun configureRemote(haUrl: String, haToken: String): String? = try {
        val response = client.submitForm(
            url = "$baseUrl/gateway/setup",
            formParameters = parameters {
                append("haUrl", haUrl)
                append("haToken", haToken)
            }
        )
        if (response.status.isSuccess()) null
        else response.bodyAsText().take(300).ifBlank { "Setup failed (${response.status})" }
    } catch (e: Exception) {
        Utils.logException(e, "Gateway setup failed")
        "Could not reach the server: ${e.message}"
    }
}

/** Settings in the browser's localStorage (per site). The HA token is never stored here. */
object WebPlatformPreferences : IPlatformPreferences {
    private const val PREFIX = "lights."

    private fun get(key: String): String? = storageGet(PREFIX + key)
    private fun put(key: String, value: String) = storageSet(PREFIX + key, value)

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

/** No translation on the web yet: report English so AppTranslator short-circuits. */
object WebPlatformTranslator : IPlatformTranslator {
    override fun deviceLanguage() = "en"
    override suspend fun initializeTranslator(language: String) = Unit
    override suspend fun translateText(text: String) = text
    override fun getFromCache(text: String): String? = null
    override fun saveToCache(original: String, translated: String) = Unit
    override fun close() = Unit
}

/** Hotword/voice control is Android-only for now. */
object WebPlatformVoiceController : IPlatformVoiceController {
    override fun start() = Utils.log("Voice control is not available on the web")
    override fun stop() = Unit
}

/** No URL/token defaults: the gateway owns them. Add `?debug` to the URL for debug options. */
object WebPlatformDefaultConfig : IPlatformDefaultConfig {
    override val defaultHaUrl: String = ""
    override val defaultHaToken: String = ""
    override val defaultPorcupineApiKey: String = ""
    override val isDebug: Boolean = isQueryParamSet("debug")
}

/** Logs to the browser console (DevTools). */
object WebPlatformLogger : IPlatformLogger {
    override fun debug(tag: String, message: String) = consoleLog("D/$tag: $message")
    override fun info(tag: String, message: String) = consoleLog("I/$tag: $message")
    override fun warning(tag: String, message: String) = consoleWarn("W/$tag: $message")
    override fun error(tag: String, message: String, exception: Throwable?) =
        consoleError("E/$tag: $message" + (exception?.let { "\n" + it.stackTraceToString() } ?: ""))
}

/** Camera/microphone features are not supported on the web yet: everything is denied. */
object WebPlatformPermissions : IPlatformPermissions {
    override suspend fun hasPermission(permission: Permission) = false
    override fun requestPermission(permission: Permission) =
        PlatformBridge.onPermissionResult(permission, granted = false)
}

/** The browser has a single thread; coroutines still interleave on it. */
object WebPlatformDispatchers : IPlatformDispatchers {
    override fun uiDispatcher(): CoroutineDispatcher = Dispatchers.Main
    override fun ioDispatcher(): CoroutineDispatcher = Dispatchers.Default
    override fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

object WebMockDataProvider : IPlatformMockDataProvider {
    /** Bundled files are served next to index.html; fetched without any HA authentication. */
    private val assetClient by lazy { HttpClient(Js) }

    /** No file system: "writing" a file downloads it. */
    override fun writeFile(fileName: String, content: String): String? = try {
        downloadText(fileName, content, "application/json")
        fileName
    } catch (e: Throwable) {
        Utils.logException(e, "Failed to download file: $fileName")
        null
    }

    override suspend fun readAssetFile(fileName: String): String {
        val response = assetClient.get(resolveUrl(fileName))
        check(response.status.isSuccess()) { "Resource not found: $fileName (${response.status})" }
        return response.bodyAsText()
    }
}

/** Exports the HA data as a downloaded JSON file. */
object WebDataExporter : IPlatformDataExporter {
    override fun exportHaData() = MockUtils.saveMockData { }
}

/** No camera on the web (yet): the scanner reports an error so QrScanScreen shows it. */
private class UnsupportedQrScanner : IPlatformQrScanner {
    override val scannedValue: StateFlow<String?> = MutableStateFlow(null)
    override val isScanning: StateFlow<Boolean> = MutableStateFlow(false)
    override val errorMessage: StateFlow<String?> =
        MutableStateFlow("QR scanning is not available on the web.")

    override suspend fun startScanning() = Unit
    override fun stopScanning() = Unit
    override fun reset() = Unit
}

/** Scrollbars come from [SkikoPlatformUi] (shared with desktop). */
object WebPlatformUi : SkikoPlatformUi() {
    /** The browser back button isn't wired yet; screens have their own back buttons. */
    @Composable
    override fun BackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

    @Composable
    override fun rememberQrScanner(): PlatformQrScanner = remember {
        PlatformQrScanner(scanner = UnsupportedQrScanner(), preview = { modifier -> Box(modifier) })
    }
}

// --- Browser APIs (Kotlin/Wasm JS interop) ---

private fun storageGet(key: String): String? = js("localStorage.getItem(key)")
private fun storageSet(key: String, value: String): Unit = js("localStorage.setItem(key, value)")
private fun pageOrigin(): String = js("window.location.origin")
private fun resolveUrl(path: String): String = js("new URL(path, document.baseURI).href")
private fun consoleLog(message: String): Unit = js("console.log(message)")
private fun consoleWarn(message: String): Unit = js("console.warn(message)")
private fun consoleError(message: String): Unit = js("console.error(message)")

private fun downloadText(fileName: String, content: String, mimeType: String): Unit = js(
    """{
        const a = document.createElement('a');
        a.href = URL.createObjectURL(new Blob([content], { type: mimeType }));
        a.download = fileName;
        a.click();
        setTimeout(() => URL.revokeObjectURL(a.href), 1000);
    }"""
)

/** True when the `?name` query parameter is present (e.g. `?demo`). */
internal fun isQueryParamSet(name: String): Boolean = js("new URLSearchParams(window.location.search).has(name)")

internal fun isPageVisible(): Boolean = js("document.visibilityState === 'visible'")

/** Calls [callback] with the new visibility whenever the tab is shown or hidden. */
internal fun onPageVisibilityChange(callback: (Boolean) -> Unit): Unit =
    js("document.addEventListener('visibilitychange', () => callback(document.visibilityState === 'visible'))")





