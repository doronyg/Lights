package com.doronyg.mylightscontroller.utils

import android.content.Context
import androidx.core.content.edit
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import kotlin.coroutines.resumeWithException

object AppTranslator {

    private lateinit var appContext: Context
    private val prefs by lazy {
        appContext.getSharedPreferences("translations", Context.MODE_PRIVATE)
    }

    private val initMutex = Mutex()

    private var translator: Translator? = null

    private val targetLanguage: String by lazy {
        Locale.getDefault().language
    }

    private val isEnglish: Boolean by lazy {
        targetLanguage == "en"
    }

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    private val memoryCache = mutableMapOf<String, String>()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun initialize() {
        if (isEnglish || _ready.value) {
            _ready.value = true
            return
        }

        initMutex.withLock {
            if (translator != null) {
                _ready.value = true
                return
            }

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(targetLanguage)
                .build()

            translator = Translation.getClient(options)

            downloadModelIfNeeded()

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

        val t = translator ?: return text

        val result = suspendCancellableCoroutine<String> { cont ->
            t.translate(text)
                .addOnSuccessListener { cont.resume(it) { _, _, _ -> } }
                .addOnFailureListener {
                    Utils.log("Translation failed for '$text': ${it.message}")
                    cont.resume(text) { _, _, _ -> }
                }
        }

        saveToCache(text, result)
        return result
    }

    /**
     * Returns a translation from the in-memory cache if available.
     *
     * This function is fully synchronous and does not perform any ML Kit calls.
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

        val t = translator ?: return
        texts.forEach { text ->
            val translated = suspendCancellableCoroutine<String> { cont ->
                t.translate(text)
                    .addOnSuccessListener { cont.resume(it) { _, _, _ -> } }
                    .addOnFailureListener { cont.resume(text) { _, _, _ -> } }
            }
            memoryCache[text] = translated
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

    private fun getFromCache(text: String): String? {
        return prefs.getString(cacheKey(text), null)
    }

    private fun saveToCache(original: String, translated: String) {
        prefs.edit {
            putString(cacheKey(original), translated)
        }
    }

    private fun cacheKey(text: String): String {
        return "$targetLanguage:$text"
    }

    fun close() {
        translator?.close()
        translator = null
        _ready.value = false
    }
}
