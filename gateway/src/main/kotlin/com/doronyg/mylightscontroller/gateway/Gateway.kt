package com.doronyg.mylightscontroller.gateway

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.http.content.staticFiles
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.request.contentType
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.uri
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

/** The only HA services the app calls (HomeAssistantApi.kt); everything else under /api is refused. */
private val ALLOWED_SERVICES = setOf("light/turn_on", "light/turn_off", "switch/turn_on", "switch/turn_off")

/**
 * - `/`                 the web app (static files from [siteDir])
 * - `/gateway/status`   "configured" / "unconfigured"
 * - `/gateway/setup`    first-run setup (form: haUrl, haToken); only while unconfigured
 * - `/api/...`          forwarded to HA with the stored token: GET states + light/switch on/off only
 */
fun Application.gateway(store: HaConfigStore, siteDir: File, ha: HttpClient) {
    install(Compression) // the .wasm files shrink ~3x

    val setupLock = Mutex()

    routing {
        get("/gateway/status") {
            call.respondText(if (store.connection != null) "configured" else "unconfigured")
        }

        post("/gateway/setup") {
            val params = call.receiveParameters()
            val url = params["haUrl"].orEmpty().trim().trimEnd('/')
            val token = params["haToken"].orEmpty().trim()

            val (status, message) = setupLock.withLock {
                if (store.connection != null) {
                    return@withLock HttpStatusCode.Conflict to
                        "Already set up. To change it, delete data/${HaConfigStore.FILE_NAME} on the server and restart it."
                }
                val problem = validateUrl(url)
                    ?: if (token.isBlank()) "The token is required" else checkHomeAssistant(ha, url, token)
                if (problem != null) return@withLock HttpStatusCode.BadRequest to problem

                store.save(HaConnection(url, token))
                this@gateway.log.info("Set up: forwarding to $url")
                HttpStatusCode.OK to "configured"
            }
            call.respondText(message, status = status)
        }

        get("/api/states") { forward(call, store, ha) }

        post("/api/services/{domain}/{service}") {
            val service = "${call.parameters["domain"]}/${call.parameters["service"]}"
            if (service in ALLOWED_SERVICES) forward(call, store, ha) else call.forbidden()
        }

        route("/api/{...}") {
            handle { call.forbidden() }
        }

        staticFiles("/", siteDir) {
            contentType { file -> if (file.extension == "wasm") ContentType("application", "wasm") else null }
        }
    }
}

private suspend fun ApplicationCall.forbidden() =
    respondText("Not available through the Lights gateway", status = HttpStatusCode.Forbidden)

/** Sends the request on to HA with the stored token and relays HA's answer. */
private suspend fun forward(call: ApplicationCall, store: HaConfigStore, ha: HttpClient) {
    val connection = store.connection
        ?: return call.respondText("The gateway is not set up yet", status = HttpStatusCode.ServiceUnavailable)

    val body = if (call.request.httpMethod == HttpMethod.Post) call.receive<ByteArray>() else null
    try {
        val response = ha.request(connection.url + call.request.uri) {
            method = call.request.httpMethod
            header(HttpHeaders.Authorization, "Bearer ${connection.token}")
            if (body != null) setBody(ByteArrayContent(body, call.request.contentType()))
        }
        call.respondBytes(response.bodyAsBytes(), response.contentType(), response.status)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        call.application.log.warn("Home Assistant request failed: ${e.message}")
        call.respondText("Home Assistant is not reachable from the gateway", status = HttpStatusCode.BadGateway)
    }
}

internal fun validateUrl(url: String): String? {
    if (url.isBlank()) return "The Home Assistant URL is required"
    if (!url.startsWith("http://") && !url.startsWith("https://")) return "The URL must start with http:// or https://"
    val parsed = runCatching { Url(url) }.getOrNull() ?: return "Invalid URL"
    if (parsed.host.isBlank()) return "Invalid URL"
    if (parsed.encodedPath.trim('/').isNotEmpty()) return "Use only the address, e.g. http://192.168.1.10:8123"
    return null
}

/** Tries the details against HA (GET /api/). Returns a message for the user, or null if they work. */
internal suspend fun checkHomeAssistant(ha: HttpClient, url: String, token: String): String? = try {
    val response = ha.get("$url/api/") { header(HttpHeaders.Authorization, "Bearer $token") }
    when {
        response.status.isSuccess() -> null
        response.status == HttpStatusCode.Unauthorized -> "Home Assistant rejected the token"
        else -> "Home Assistant answered ${response.status} at $url"
    }
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    "Can't reach Home Assistant at $url from the server (${e.message ?: e::class.simpleName}). " +
        "If it runs on the same NAS, use the NAS's LAN IP, not localhost."
}


