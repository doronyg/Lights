import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

/**
 * Web app (Kotlin/Wasm + Compose). Talks to Home Assistant through a gateway on the same origin
 * (`/api/...`), which adds the HA token: the browser never sees it.
 *
 *   Dev:     HA_URL=http://ha:8123 HA_TOKEN=... ./gradlew :webApp:wasmJsBrowserDevelopmentRun
 *            (the dev server plays the gateway, see webpack.config.d/gateway.js)
 *   Demo:    open http://localhost:8080/?demo (no HA needed)
 *   Release: ./gradlew :webApp:wasmJsBrowserDistribution
 *            -> webApp/build/dist/wasmJs/productionExecutable/
 */
kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "webApp"
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain {
            // Reuse the Android demo data (ha_lights_demo.json) as web resources
            resources.srcDir("../app/src/main/assets")
            dependencies {
                implementation(project(":shared"))
                implementation(libs.ktor.client.js)
            }
        }
    }
}

// Favicon: the Android launcher icon
tasks.named<ProcessResources>("wasmJsProcessResources") {
    from("../app/src/main/res/mipmap-xxxhdpi") {
        include("ic_bee_launcher.webp")
    }
}

