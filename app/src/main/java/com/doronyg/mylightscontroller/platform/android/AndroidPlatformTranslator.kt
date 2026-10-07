package com.doronyg.mylightscontroller.platform.android

import androidx.core.content.edit
import com.doronyg.mylightscontroller.platform.common.IPlatformTranslator
import com.doronyg.mylightscontroller.utils.Utils
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resumeWithException

/**
 * Android implementation of platform-specific translation provider.
 * Uses Google ML Kit for on-device translation.
 * Uses SharedPreferences (via AndroidPlatformPreferences) for caching translations.
 *
 * Future: This will become the `actual` implementation when converting to Kotlin Multiplatform.
 */
object AndroidPlatformTranslator : IPlatformTranslator {

    private var translator: Translator? = null
    private var targetLanguage: String = ""

    private val prefs by lazy {
        AndroidPlatformPreferences.getTranslationPreferences()
    }

    override fun deviceLanguage(): String = Locale.getDefault().language

    override suspend fun initializeTranslator(language: String) {
        if (translator != null) return

        targetLanguage = language
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(language)
            .build()

        translator = Translation.getClient(options)
        downloadModelIfNeeded()
    }

    override suspend fun translateText(text: String): String {
        val t = translator ?: return text

        return suspendCancellableCoroutine<String> { cont ->
            t.translate(text)
                .addOnSuccessListener { cont.resume(it) { _, _, _ -> } }
                .addOnFailureListener {
                    Utils.log("Translation failed for '$text': ${it.message}")
                    cont.resume(text) { _, _, _ -> }
                }
        }
    }

    private suspend fun downloadModelIfNeeded() {
        val t = translator ?: return

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        suspendCancellableCoroutine { cont ->
            t.downloadModelIfNeeded(conditions)
                .addOnSuccessListener { cont.resume(Unit) { _, _, _ -> } }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    override fun getFromCache(text: String): String? {
        return prefs?.getString(cacheKey(text), null)
    }

    override fun saveToCache(original: String, translated: String) {
        prefs?.edit {
            putString(cacheKey(original), translated)
        }
    }

    private fun cacheKey(text: String): String {
        return "$targetLanguage:$text"
    }

    override fun close() {
        translator?.close()
        translator = null
    }
}


