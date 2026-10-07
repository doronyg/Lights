plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

/**
 * Platform-independent app logic (Home Assistant client, settings, command parsing, QR encoding).
 * Platform services are injected at runtime via PlatformBridge.
 *
 * Only the Android target is enabled for now; add e.g. `wasmJs { browser() }` to extend.
 */
kotlin {
    android {
        namespace = "com.doronyg.mylightscontroller.shared"
        compileSdk = 36
        minSdk = 23

        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            // `api`: these types appear in the module's public API (StateFlow, HttpClient, Json, ...)
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        getByName("androidHostTest").dependencies {
            implementation(libs.zxing.core) // only used to verify our QR encoder
        }
    }
}


