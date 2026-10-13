import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false // see androidEnabled
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

/** False on machines without the Android SDK (see settings.gradle.kts). */
val androidEnabled = gradle.extra["androidEnabled"] as Boolean
if (androidEnabled) {
    apply(plugin = libs.plugins.android.kotlin.multiplatform.library.get().pluginId)
}

/**
 * Platform-independent app logic and UI (Home Assistant client, settings, command parsing,
 * QR encoding, Compose Multiplatform screens).
 * Platform services are injected at runtime via PlatformBridge (logic) and LocalPlatformUi (UI).
 */
kotlin {
    if (androidEnabled) {
        extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
            namespace = "com.doronyg.mylightscontroller.shared"
            compileSdk = 36
            minSdk = 23

            withHostTest {}
        }
    }

    jvm { // Desktop (Compose Desktop, see :desktopApp); Java 17+ bytecode
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { // Web (see :webApp)
        browser()
    }

    // iOS (see iosApp/): device + Apple-silicon simulator. The Xcode project links the
    // static "Shared" framework, built by the embedAndSignAppleFrameworkForXcode task.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    // skikoMain: code for the Skia-rendered, mouse-driven targets (desktop + web), e.g. scrollbars
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("skiko") {
                withJvm()
                withWasmJs()
            }
        }
    }

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
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin) // NSURLSession-based HTTP engine
        }

        if (androidEnabled) {
            getByName("androidHostTest").dependencies {
                implementation(libs.zxing.core) // only used to verify our QR encoder
            }
        }
    }
}
