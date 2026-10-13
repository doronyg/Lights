package com.doronyg.mylightscontroller.utils

import com.doronyg.mylightscontroller.platform.common.IPlatformTranslator
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakePlatform : IPlatformTranslator {
    var mlCalls = 0
    override fun deviceLanguage() = "ru"
    override suspend fun initializeTranslator(language: String) {}
    override suspend fun translateText(text: String): String {
        mlCalls++
        return "ml($text)"
    }
    override fun close() {}
}

private class FakeStore : TranslationStore {
    val caches = mutableMapOf<String, MutableMap<String, String>>()
    val dictionaries = mutableMapOf<String, StoredDictionary>()
    var cacheWrites = 0

    override fun loadCache(language: String): Map<String, String> = caches[language]?.toMap() ?: emptyMap()
    override fun saveCache(language: String, cache: Map<String, String>) {
        cacheWrites++
        caches[language] = cache.toMutableMap()
    }
    override fun loadDictionary(language: String) = dictionaries[language]
    override fun saveDictionary(language: String, dictionary: StoredDictionary) { dictionaries[language] = dictionary }

    fun cached(language: String, text: String) = caches[language]?.get(text)
    fun putCached(language: String, text: String, value: String) { caches.getOrPut(language) { mutableMapOf() }[text] = value }
}

private class FakeFetcher(var result: DictionaryFetchResult) : DictionaryFetcher {
    var lastEtag: String? = null
    override suspend fun fetch(language: String, etag: String?): DictionaryFetchResult {
        lastEtag = etag
        return result
    }
}

class TranslationServiceTest {
    private fun ok(json: String, etag: String? = "e1") = DictionaryFetchResult.Success(json, etag)

    @Test
    fun newDictionaryEntryOverridesCachedMlKitTranslation() = runTest {
        val platform = FakePlatform()
        val store = FakeStore().also { it.putCached("ru", "Kitchen", "ml-old") }
        val service = TranslationService(platform, "ru", store, FakeFetcher(ok("""{"Kitchen":"Кухня"}""")))
        service.initialize()
        assertEquals("Кухня", service.translate("Kitchen"))
        assertNull(store.cached("ru", "Kitchen"))
    }

    @Test
    fun updatingDictionaryInvalidatesConflictingTranslations() = runTest {
        val platform = FakePlatform()
        val store = FakeStore()
        val fetcher = FakeFetcher(ok("""{"Kitchen":"A"}"""))
        TranslationService(platform, "ru", store, fetcher).initialize()
        store.putCached("ru", "Kitchen", "stale")
        store.putCached("ru", "Porch", "keep")

        fetcher.result = ok("""{"Kitchen":"B"}""", "e2")
        val service = TranslationService(platform, "ru", store, fetcher)
        service.initialize()

        assertEquals("B", service.translate("Kitchen"))
        assertNull(store.cached("ru", "Kitchen"))
        assertEquals("keep", store.cached("ru", "Porch"))
    }

    @Test
    fun removingEntryLeavesNoObsoleteDictionaryResult() = runTest {
        val platform = FakePlatform()
        val store = FakeStore()
        val fetcher = FakeFetcher(ok("""{"Kitchen":"Кухня"}"""))
        TranslationService(platform, "ru", store, fetcher).initialize()

        fetcher.result = ok("""{}""", "e2")
        val service = TranslationService(platform, "ru", store, fetcher)
        service.initialize()

        assertEquals("ml(Kitchen)", service.translate("Kitchen"))
        assertEquals("{}", store.dictionaries["ru"]?.json)
    }

    @Test
    fun updatingOneLanguageDoesNotClearOthers() = runTest {
        val platform = FakePlatform()
        val store = FakeStore()
        store.putCached("fr", "Kitchen", "Cuisine-ml")
        store.dictionaries["fr"] = StoredDictionary("""{"Kitchen":"Cuisine"}""", "f1")
        TranslationService(platform, "ru", store, FakeFetcher(ok("""{"Kitchen":"Кухня"}"""))).initialize()

        assertEquals("Cuisine-ml", store.cached("fr", "Kitchen"))
        assertEquals("""{"Kitchen":"Cuisine"}""", store.dictionaries["fr"]?.json)
    }

    @Test
    fun failedDownloadPreservesLastKnownGoodDictionary() = runTest {
        val platform = FakePlatform()
        val store = FakeStore()
        store.dictionaries["ru"] = StoredDictionary("""{"Kitchen":"Кухня"}""", "e1")
        store.putCached("ru", "Porch", "cached")
        val service = TranslationService(platform, "ru", store, FakeFetcher(DictionaryFetchResult.Failure))
        service.initialize()

        assertEquals("Кухня", service.translate("Kitchen"))
        assertEquals("cached", store.cached("ru", "Porch"))
        assertEquals("e1", store.dictionaries["ru"]?.tag)
    }

    @Test
    fun malformedDownloadIsTreatedAsFailure() = runTest {
        val platform = FakePlatform()
        val store = FakeStore()
        store.dictionaries["ru"] = StoredDictionary("""{"Kitchen":"Кухня"}""", "e1")
        val service = TranslationService(platform, "ru", store, FakeFetcher(ok("not json")))
        service.initialize()
        assertEquals("Кухня", service.translate("Kitchen"))
        assertEquals("e1", store.dictionaries["ru"]?.tag)
    }

    @Test
    fun unchangedDictionaryDoesNotInvalidateCache() = runTest {
        val platform = FakePlatform()
        val store = FakeStore()
        store.dictionaries["ru"] = StoredDictionary("""{"Kitchen":"Кухня"}""", "e1")
        store.putCached("ru", "Kitchen", "x")
        val fetcher = FakeFetcher(ok("""{ "Kitchen": "Кухня" }""", "e1"))
        TranslationService(platform, "ru", store, fetcher).initialize()
        assertEquals(0, store.cacheWrites)
        assertEquals("e1", fetcher.lastEtag)

        fetcher.result = DictionaryFetchResult.NotModified
        TranslationService(platform, "ru", store, fetcher).initialize()
        assertEquals(0, store.cacheWrites)
    }
}
