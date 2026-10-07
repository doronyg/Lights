plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

/**
 * Platform-independent app logic and UI (Home Assistant client, settings, command parsing,
 * QR encoding, Compose Multiplatform screens).
 * Platform services are injected at runtime via PlatformBridge (logic) and LocalPlatformUi (UI).
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

    jvm() // Desktop (Compose Desktop, see :desktopApp)

    sourceSets {
        commonMain.dependencies {
            // `api`: these types appear in the module's public API (StateFlow, HttpClient, Json, ...)
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)

            // Compose Multiplatform (api: composables and Modifier appear in the public API)
            api(libs.cmp.runtime)
            api(libs.cmp.foundation)
            api(libs.cmp.ui)
            api(libs.cmp.material3)
            implementation(libs.cmp.material.icons.extended)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        getByName("androidHostTest").dependencies {
            implementation(libs.zxing.core) // only used to verify our QR encoder
        }
    }
}




