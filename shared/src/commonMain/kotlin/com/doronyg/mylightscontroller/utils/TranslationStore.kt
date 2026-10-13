package com.doronyg.mylightscontroller.utils

import com.doronyg.mylightscontroller.platform.common.IPlatformPreferences
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** Persistence for the per-language translation cache and downloaded dictionaries. */
interface TranslationStore {
    fun loadCache(language: String): Map<String, String>
    fun saveCache(language: String, cache: Map<String, String>)
    fun loadDictionary(language: String): StoredDictionary?
    fun saveDictionary(language: String, dictionary: StoredDictionary)
}

/**
 * [TranslationStore] on top of the existing platform preferences. Each language is a separate
 * key, so updating one language never touches another's cache.
 */
class PreferencesTranslationStore(
    private val prefs: () -> IPlatformPreferences = { PlatformBridge.preferences },
) : TranslationStore {
    private val mapSerializer = MapSerializer(String.serializer(), String.serializer())

    // Wraps a preference delegate so it can be read and written by dynamic key
    private class Slot(pref: kotlin.properties.ReadWriteProperty<Any, String>) {
        var value: String by pref
    }

    private fun slot(key: String) = Slot(prefs().stringPref(key))

    override fun loadCache(language: String): Map<String, String> =
        TranslationDictionaryParser.parse(slot("translation_cache:$language").value) ?: emptyMap()

    override fun saveCache(language: String, cache: Map<String, String>) {
        slot("translation_cache:$language").value = Json.encodeToString(mapSerializer, cache)
    }

    override fun loadDictionary(language: String): StoredDictionary? {
        val json = slot("translation_dictionary:$language").value
        if (json.isEmpty()) return null
        return StoredDictionary(json, slot("translation_dictionary_etag:$language").value.ifEmpty { null })
    }

    override fun saveDictionary(language: String, dictionary: StoredDictionary) {
        slot("translation_dictionary:$language").value = dictionary.json
        slot("translation_dictionary_etag:$language").value = dictionary.tag.orEmpty()
    }
}
