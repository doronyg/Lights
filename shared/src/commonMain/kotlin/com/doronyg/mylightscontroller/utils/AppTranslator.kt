package com.doronyg.mylightscontroller.utils

import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import kotlinx.coroutines.flow.StateFlow

/**
 * Multiplatform translator facade over [TranslationService].
 * Platform-specific details (ML Kit, cache and dictionary storage) come via PlatformBridge.
 */
object AppTranslator {

    private var current: TranslationService? = null

    /** The service for the device's current language; rebuilt if the language changed since last use. */
    private val service: TranslationService
        get() {
            val language = PlatformBridge.translator.deviceLanguage()
            current?.takeIf { it.targetLanguage == language }?.let { return it }
            current?.close()
            return TranslationService(PlatformBridge.translator, language).also { current = it }
        }

    val ready: StateFlow<Boolean> get() = service.ready

    suspend fun initialize() = service.initialize()

    suspend fun translate(text: String): String = service.translate(text)

    /**
     * Returns a translation from the dictionary or in-memory cache if available, else the original text.
     * Fully synchronous; requires initialize() and (for ML Kit results) preload(...) first.
     * For guaranteed translation, use the suspend translate() function.
     */
    fun cachedTranslation(text: String): String = service.cachedTranslation(text)

    suspend fun preload(texts: List<String>) = service.preload(texts)

    fun close() = service.close()
}
