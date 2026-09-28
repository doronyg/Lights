package com.doronyg.mylightscontroller.utils

import com.doronyg.mylightscontroller.Config
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object NetworkUtils {
    object HttpMethod {
        const val GET = "GET"
        const val POST = "POST"
        const val PUT = "PUT"
        const val DELETE = "DELETE"
    }

    // Single Ktor HttpClient instance with OkHttp engine for Android
    val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            engine {
                config {
                    connectTimeout(10, TimeUnit.SECONDS)
                    readTimeout(15, TimeUnit.SECONDS)
                    writeTimeout(15, TimeUnit.SECONDS)
                }
            }

            install(ContentNegotiation) {
                json(JsonUtils.json)
            }
        }
    }

    // Add common Home Assistant headers to any request
    fun HttpRequestBuilder.withHaHeaders() {
        header(HttpHeaders.Authorization, "Bearer ${Config.homeAssistantToken}")
    }

    // Suspend GET request - returns result directly with try-catch
    suspend inline fun <reified T> haGet(endpoint: String): T {
        return try {
            val url = "${Config.homeAssistantUrl}/api/$endpoint"
            httpClient.get(url) {
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
        CoroutineScope(Dispatchers.IO).launch {
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
            httpClient.post("${Config.homeAssistantUrl}/api/services/$endpoint") {
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                haPost<T>(endpoint, body)
                Utils.post({ onSuccess() })
            } catch (e: Exception) {
                Utils.post({ onError(e) })
            }
        }
    }
}