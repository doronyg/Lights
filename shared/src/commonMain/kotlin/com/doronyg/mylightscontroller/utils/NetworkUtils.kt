package com.doronyg.mylightscontroller.utils

import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object NetworkUtils {
    object HttpMethod {
        const val GET = "GET"
        const val POST = "POST"
        const val PUT = "PUT"
        const val DELETE = "DELETE"
    }

    // Single Ktor HttpClient; the engine is supplied by the platform layer
    val httpClient: HttpClient by lazy {
        HttpClient(PlatformBridge.httpEngine) {
            install(HttpTimeout) {
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }

            install(ContentNegotiation) {
                json(JsonUtils.json)
            }
        }
    }

    // Authenticate a request for HA (Bearer token natively; nothing on web, the gateway adds it)
    fun HttpRequestBuilder.withHaHeaders() {
        PlatformBridge.haEndpoint.authorize(this)
    }

    /** Full URL of an HA REST path, e.g. haUrl("states") -> "<base>/api/states". */
    fun haUrl(path: String): String = "${PlatformBridge.haEndpoint.baseUrl}/api/$path"

    // Suspend GET request - returns result directly with try-catch
    suspend inline fun <reified T> haGet(endpoint: String): T {
        return try {
            httpClient.get(haUrl(endpoint)) {
                withHaHeaders()
            }.body()
        } catch (e: Exception) {
            Utils.log("Failed to GET $endpoint: $e")
            Utils.logException(e)
            throw e
        }
    }

    // Async GET request - handles coroutine dispatch, error handling, headers, and main thread callbacks
    inline fun <reified T> asyncHaGet(
        endpoint: String,
        crossinline onResult: (T) -> Unit,
        crossinline onError: (Exception) -> Unit = {}
    ) {
        CoroutineScope(PlatformBridge.dispatchers.ioDispatcher()).launch {
            try {
                val result = haGet<T>(endpoint)
                Utils.post({ onResult(result) })
            } catch (e: Exception) {
                Utils.post({ onError(e) })
            }
        }
    }

    // Suspend POST request - with try-catch protection
    suspend inline fun <reified T> haPost(
        endpoint: String,
        body: T
    ) {
        try {
            httpClient.post(haUrl("services/$endpoint")) {
                withHaHeaders()
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            Utils.log("Successfully POST $endpoint")
        } catch (e: Exception) {
            Utils.log("Failed to POST $endpoint: $e")
            Utils.logException(e)
            throw e
        }
    }

    // Async POST request - handles coroutine dispatch, error handling, headers, and main thread callbacks
    inline fun <reified T> asyncHaPost(
        endpoint: String,
        body: T,
        crossinline onSuccess: () -> Unit = {},
        crossinline onError: (Exception) -> Unit = {}
    ) {
        CoroutineScope(PlatformBridge.dispatchers.ioDispatcher()).launch {
            try {
                haPost<T>(endpoint, body)
                Utils.post({ onSuccess() })
            } catch (e: Exception) {
                Utils.post({ onError(e) })
            }
        }
    }
}