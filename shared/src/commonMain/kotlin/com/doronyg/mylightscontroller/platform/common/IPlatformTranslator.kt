package com.doronyg.mylightscontroller.platform.common

/**
 * Platform-specific machine translation (the fallback when the community dictionary has no entry).
 * Caching and dictionaries live in common code (see TranslationService).
 */
interface IPlatformTranslator {
    /** Language code of the device's current locale (e.g. "en", "he"). */
    fun deviceLanguage(): String
    suspend fun initializeTranslator(language: String)
    suspend fun translateText(text: String): String
    fun close()
}
