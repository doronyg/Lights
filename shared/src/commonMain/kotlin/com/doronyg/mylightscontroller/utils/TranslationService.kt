package com.doronyg.mylightscontroller.utils

import com.doronyg.mylightscontroller.platform.common.IPlatformTranslator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Translation logic behind [AppTranslator]: community dictionary first, then the translation
 * cache (persisted via [TranslationStore]), then ML Kit via the platform translator.
 *
 * Dictionary results are never written to the translation cache, so they always win over
 * ML Kit, and a removed entry cannot leave a stale dictionary result behind.
 */
class TranslationService(
    private val platform: IPlatformTranslator,
    val targetLanguage: String,
    private val store: TranslationStore = PreferencesTranslationStore(),
    private val fetcher: DictionaryFetcher = GitHubDictionaryFetcher,
    private val downloadTimeoutMs: Long = 10_000,
) {
    private val isEnglish = targetLanguage == "en"
    private val initMutex = Mutex()

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    /** ML Kit results for [targetLanguage], persisted through [store]. */
    private val translationCache = mutableMapOf<String, String>()
    private var dictionary: Map<String, String> = emptyMap()

    suspend fun initialize() {
        if (isEnglish || _ready.value) {
            _ready.value = true
            return
        }

        initMutex.withLock {
            if (_ready.value) return

            translationCache.putAll(store.loadCache(targetLanguage))
            try {
                syncDictionary()
            } catch (e: Exception) {
                Utils.log("Dictionary sync failed: $e")
            }
            try {
                platform.initializeTranslator(targetLanguage)
            } catch (e: Exception) {
                // e.g. model download failed: stay usable (dictionary + cache) rather than block translate() forever
                Utils.log("Translator init failed: $e")
            }
            _ready.value = true
        }
    }

    /** Loads the stored dictionary, then refreshes it from the network and invalidates changed entries. */
    suspend fun syncDictionary() {
        if (isEnglish) return

        val stored = store.loadDictionary(targetLanguage)
        val current = stored?.let { TranslationDictionaryParser.parse(it.json) } ?: emptyMap()
        dictionary = current

        val result = withTimeoutOrNull(downloadTimeoutMs.milliseconds) {
            fetcher.fetch(targetLanguage, stored?.tag)
        } ?: DictionaryFetchResult.Failure
        if (result !is DictionaryFetchResult.Success) return

        val downloaded = TranslationDictionaryParser.parse(result.json) ?: return

        if (downloaded != current) {
            val changed = (current.keys + downloaded.keys).filter { current[it] != downloaded[it] }
            invalidate(changed)
        }
        // Saved after invalidating: if interrupted in between, the next sync re-diffs and re-invalidates
        if (stored == null || downloaded != current || stored.tag != result.etag) {
            store.saveDictionary(targetLanguage, StoredDictionary(result.json, result.etag))
        }
        dictionary = downloaded
    }

    private fun invalidate(texts: List<String>) {
        if (texts.isEmpty()) return
        texts.forEach { translationCache.remove(it) }
        store.saveCache(targetLanguage, translationCache)
    }

    private suspend fun awaitReady() {
        if (_ready.value) return
        _ready.filter { it }.first()
    }

    suspend fun translate(text: String): String {
        if (isEnglish) return text

        awaitReady()

        dictionary[text]?.let { return it }

        translationCache[text]?.let { return it }

        val result = platform.translateText(text)
        // ML Kit returns the input on failure, so an identical result is not cached (it would stick forever)
        if (result != text) {
            translationCache[text] = result
            store.saveCache(targetLanguage, translationCache)
        }
        return result
    }

    /** Synchronous lookup (dictionary, then memory cache); returns [text] if nothing is available. */
    fun cachedTranslation(text: String): String {
        if (isEnglish) return text
        return dictionary[text] ?: translationCache[text] ?: text
    }

    suspend fun preload(texts: List<String>) {
        if (isEnglish) return

        var changed = false
        texts.forEach { text ->
            if (text in dictionary || text in translationCache) return@forEach
            val result = platform.translateText(text)
            if (result == text) return@forEach
            translationCache[text] = result
            changed = true
        }
        if (changed) store.saveCache(targetLanguage, translationCache)
    }

    fun close() {
        platform.close()
        _ready.value = false
    }
}
