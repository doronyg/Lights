package com.doronyg.mylightscontroller.gateway

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.http.parameters
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GatewayTest {
    private val haUrl = "http://ha.test:8123"
    private val goodToken = "good-token-123"

    private val dataDir = createTempDirectory("lights-data").toFile()
    private val siteDir = createTempDirectory("lights-site").toFile().apply {
        File(this, "index.html").writeText("<html>Lights</html>")
        File(this, "app.wasm").writeBytes(byteArrayOf(0, 97, 115, 109))
    }

    /** What reached "Home Assistant". */
    private val haRequests = mutableListOf<HttpRequestData>()

    private val ha = HttpClient(MockEngine { request ->
        haRequests += request
        when {
            request.headers[HttpHeaders.Authorization] != "Bearer $goodToken" ->
                respond("401: Unauthorized", HttpStatusCode.Unauthorized)
            request.url.encodedPath == "/api/states" ->
                respond("""[{"entity_id":"light.kitchen"}]""", HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, "application/json"))
            else -> respond("""{"message":"ok"}""", HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"))
        }
    })

    private fun gatewayTest(store: HaConfigStore = HaConfigStore(dataDir), block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application { gateway(store, siteDir, ha) }
            block()
        }

    private suspend fun ApplicationTestBuilder.setup(url: String, token: String) =
        client.submitForm("/gateway/setup", parameters {
            append("haUrl", url)
            append("haToken", token)
        })

    @Test
    fun firstRunSetupStoresTheConnectionOnce() = gatewayTest {
        assertEquals("unconfigured", client.get("/gateway/status").bodyAsText())
        assertEquals(HttpStatusCode.ServiceUnavailable, client.get("/api/states").status)

        val rejected = setup(haUrl, "wrong-token-123")
        assertEquals(HttpStatusCode.BadRequest, rejected.status)
        assertTrue("rejected the token" in rejected.bodyAsText())
        assertEquals("unconfigured", client.get("/gateway/status").bodyAsText())

        assertEquals(HttpStatusCode.OK, setup("$haUrl/", goodToken).status)
        assertEquals("configured", client.get("/gateway/status").bodyAsText())

        // Can't be overwritten from the browser
        assertEquals(HttpStatusCode.Conflict, setup("http://evil.test", goodToken).status)

        // Persisted (survives restarts), owner-only
        val file = File(dataDir, HaConfigStore.FILE_NAME)
        assertEquals("rw-------", PosixFilePermissions.toString(Files.getPosixFilePermissions(file.toPath())))
        assertEquals(HaConnection(haUrl, goodToken), HaConfigStore(dataDir).connection)
    }

    @Test
    fun setupRejectsBadUrlsWithoutCallingHa() = gatewayTest {
        assertEquals(HttpStatusCode.BadRequest, setup("ha.test:8123", goodToken).status)
        assertEquals(HttpStatusCode.BadRequest, setup("$haUrl/lovelace", goodToken).status)
        assertEquals(HttpStatusCode.BadRequest, setup(haUrl, "").status)
        assertTrue(haRequests.isEmpty())
    }

    @Test
    fun forwardsAllowedCallsWithTheToken() = gatewayTest(configuredStore()) {
        val states = client.get("/api/states")
        assertEquals(HttpStatusCode.OK, states.status)
        assertEquals("""[{"entity_id":"light.kitchen"}]""", states.bodyAsText())
        assertEquals("$haUrl/api/states", haRequests.last().url.toString())

        val body = """{"entity_id":["light.kitchen"]}"""
        val turnOn = client.post("/api/services/light/turn_on") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertEquals(HttpStatusCode.OK, turnOn.status)
        val forwarded = haRequests.last()
        assertEquals("Bearer $goodToken", forwarded.headers[HttpHeaders.Authorization])
        assertEquals(body, (forwarded.body as ByteArrayContent).bytes().decodeToString())
    }

    @Test
    fun refusesEverythingElseUnderApi() = gatewayTest(configuredStore()) {
        assertEquals(HttpStatusCode.Forbidden, client.post("/api/services/homeassistant/restart").status)
        assertEquals(HttpStatusCode.Forbidden, client.post("/api/services/lock/unlock").status)
        assertEquals(HttpStatusCode.Forbidden, client.get("/api/config").status)
        assertEquals(HttpStatusCode.Forbidden, client.post("/api/states").status)
        assertTrue(haRequests.isEmpty())
    }

    @Test
    fun servesTheWebApp() = gatewayTest {
        assertEquals("<html>Lights</html>", client.get("/").bodyAsText())
        val wasm = client.get("/app.wasm")
        assertEquals(HttpStatusCode.OK, wasm.status)
        assertEquals(ContentType("application", "wasm"), wasm.contentType()?.withoutParameters())
    }

    @Test
    fun tokenIsNeverLogged() {
        assertNull(Regex(goodToken).find(HaConnection(haUrl, goodToken).toString()))
    }

    private fun configuredStore() = HaConfigStore(dataDir).apply { save(HaConnection(haUrl, goodToken)) }
        .also { assertNotNull(it.connection) }
}



