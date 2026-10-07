package com.doronyg.mylightscontroller.platform.common

/**
 * Common interface for platform-specific translation provider.
 *
 * This defines the contract that will be implemented per-platform.
 * Future: will become `expect object` when converting to Kotlin Multiplatform.
 */
interface IPlatformTranslator {
    /** ISO 639 language code of the device's current locale (e.g. "en", "he"). */
    fun deviceLanguage(): String
    suspend fun initializeTranslator(language: String)
    suspend fun translateText(text: String): String
    fun getFromCache(text: String): String?
    fun saveToCache(original: String, translated: String)
    fun close()
}

