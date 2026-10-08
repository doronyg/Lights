package com.doronyg.mylightscontroller.gateway

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import java.io.File
import kotlin.system.exitProcess
import io.ktor.client.engine.cio.CIO as ClientCIO

/**
 * Settings come from environment variables (set in the Dockerfile):
 *   LIGHTS_PORT      default 8321
 *   LIGHTS_SITE_DIR  the web app build (index.html, .wasm, ...), default ./site
 *   LIGHTS_DATA_DIR  where the HA connection is stored, default ./data
 */
fun main() {
    val port = System.getenv("LIGHTS_PORT")?.toIntOrNull() ?: 8321
    val siteDir = File(System.getenv("LIGHTS_SITE_DIR") ?: "site").absoluteFile
    val dataDir = File(System.getenv("LIGHTS_DATA_DIR") ?: "data").absoluteFile

    if (!File(siteDir, "index.html").isFile) {
        System.err.println("No web app in $siteDir (index.html missing)")
        exitProcess(1)
    }

    val store = HaConfigStore(dataDir)
    println("Lights gateway on port $port")
    println(
        store.connection?.let { "Home Assistant: ${it.url}" }
            ?: "Not set up yet: open the page in a browser to enter the Home Assistant URL and token"
    )

    val ha = HttpClient(ClientCIO) {
        install(HttpTimeout) {
            connectTimeoutMillis = 5_000
            requestTimeoutMillis = 15_000
        }
    }

    embeddedServer(CIO, port = port) {
        gateway(store, siteDir, ha)
    }.start(wait = true)
}

