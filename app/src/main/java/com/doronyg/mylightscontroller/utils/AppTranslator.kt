package com.doronyg.mylightscontroller.utils

import com.doronyg.mylightscontroller.platform.PlatformBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale

/**
 * Multiplatform translator facade.
 * Platform-specific implementation details are delegated via PlatformBridge.
 *
 * This object provides:
 * - Multiplatform StateFlow for reactive ready state
 * - Shared caching logic
 * - Suspend translate/preload functions
 */
object AppTranslator {

    private var appContext: android.content.Context? = null
    private val initMutex = Mutex()

    private val targetLanguage: String by lazy {
        Locale.getDefault().language
    }

    private val isEnglish: Boolean by lazy {
        targetLanguage == "en"
    }

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    private val memoryCache = mutableMapOf<String, String>()

    fun init(context: android.content.Context) {
        appContext = context.applicationContext
    }

    suspend fun initialize() {
        if (isEnglish || _ready.value) {
            _ready.value = true
            return
        }

        initMutex.withLock {
            if (_ready.value) {
                return
            }

            PlatformBridge.translator.initializeTranslator(targetLanguage)
            _ready.value = true
        }
    }

    private suspend fun awaitReady() {
        if (_ready.value) return

        _ready
            .filter { it }
            .first()
    }

    suspend fun translate(text: String): String {
        if (isEnglish) return text

        awaitReady()

        val cached = getFromCache(text)
        if (cached != null) return cached

        val result = PlatformBridge.translator.translateText(text)
        saveToCache(text, result)
        return result
    }

    /**
     * Returns a translation from the in-memory cache if available.
     *
     * This function is fully synchronous and does not perform any translation calls.
     *
     * ⚠️ Limitation:
     * If a translation was not preloaded via AppTranslator.preload(...),
     * this will fall back to returning the original text.
     *
     * Intended usage:
     * - UI rendering (Compose)
     * - Static labels (tabs, navigation, settings)
     *
     * Requirements:
     * - AppTranslator.initialize() must be called first
     * - AppTranslator.preload(...) should be used to populate cache
     *
     * For guaranteed translation, use the suspend translate() function.
     */
    fun cachedTranslation(text: String): String {
        if (isEnglish) return text

        return memoryCache[text] ?: text
    }

    suspend fun preload(texts: List<String>) {
        if (isEnglish) return

        texts.forEach { text ->
            val translated = PlatformBridge.translator.translateText(text)
            memoryCache[text] = translated
        }
    }

    private fun getFromCache(text: String): String? {
        val cached = memoryCache[text]
        if (cached != null) return cached

        // Also check platform-specific storage
        return PlatformBridge.translator.getFromCache(text)?.also {
            memoryCache[text] = it
        }
    }

    private fun saveToCache(original: String, translated: String) {
        memoryCache[original] = translated
        PlatformBridge.translator.saveToCache(original, translated)
    }

    fun close() {
        PlatformBridge.translator.close()
        _ready.value = false
    }
}
