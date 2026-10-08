package com.doronyg.mylightscontroller.platform.common

import com.doronyg.mylightscontroller.Config
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

/**
 * Where Home Assistant API requests go, and how they are authenticated.
 *
 * Requests always use the HA REST paths ("$baseUrl/api/states", "$baseUrl/api/services/...");
 * only the base URL and the authentication differ per platform.
 */
interface IHaEndpoint {
    /** Base URL without a trailing slash; requests go to "$baseUrl/api/...". */
    val baseUrl: String

    /** Adds authentication to a request (a no-op when a server adds it on the way to HA). */
    fun authorize(request: HttpRequestBuilder)

    /**
     * True when the HA URL and token are configured outside the app (e.g. by the web gateway),
     * so the app never sees the token: no setup screens, nothing to export.
     */
    val credentialsManagedExternally: Boolean get() = false
}

/** Native apps (Android, desktop): talk to HA directly with the user's long-lived token. */
object DirectHaEndpoint : IHaEndpoint {
    override val baseUrl: String get() = Config.homeAssistantUrl

    override fun authorize(request: HttpRequestBuilder) {
        request.header(HttpHeaders.Authorization, "Bearer ${Config.homeAssistantToken}")
    }
}

