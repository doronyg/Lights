package com.doronyg.mylightscontroller.utils

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** A dictionary as last successfully downloaded: its raw JSON plus the validator for conditional requests. */
data class StoredDictionary(val json: String, val tag: String?)

sealed class DictionaryFetchResult {
    data object NotModified : DictionaryFetchResult()
    data class Success(val json: String, val etag: String?) : DictionaryFetchResult()
    data object Failure : DictionaryFetchResult()
}

fun interface DictionaryFetcher {
    /** [etag] is the validator of the stored dictionary (if any), for a conditional request. */
    suspend fun fetch(language: String, etag: String?): DictionaryFetchResult
}

object TranslationDictionaryParser {
    private val serializer = MapSerializer(String.serializer(), String.serializer())

    /** Parses a flat JSON map of English source string to translation; null if malformed. */
    fun parse(json: String): Map<String, String>? =
        try {
            Json.decodeFromString(serializer, json)
        } catch (e: Exception) {
            null
        }
}

/** Downloads `translations/{language}.json` from GitHub using ETag conditional requests. */
object GitHubDictionaryFetcher : DictionaryFetcher {
    private const val BASE_URL = "https://raw.githubusercontent.com/doronyg/Lights/main/translations"

    override suspend fun fetch(language: String, etag: String?): DictionaryFetchResult =
        try {
            val response = NetworkUtils.httpClient.get("$BASE_URL/$language.json") {
                if (etag != null) header(HttpHeaders.IfNoneMatch, etag)
            }
            when (response.status) {
                HttpStatusCode.NotModified -> DictionaryFetchResult.NotModified
                HttpStatusCode.OK ->
                    DictionaryFetchResult.Success(response.bodyAsText(), response.headers[HttpHeaders.ETag])
                else -> DictionaryFetchResult.Failure
            }
        } catch (e: Exception) {
            Utils.log("Dictionary download failed for '$language': $e")
            DictionaryFetchResult.Failure
        }
}
