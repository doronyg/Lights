plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

val withHardCodedSecrets = false

fun env(name: String): String =
    System.getenv(name) ?: ""

android {
    namespace = "com.doronyg.mylightscontroller"
    compileSdk {
        version = release(36)
    }

    signingConfigs {
        create("release") {
            val storeFilePath = env("RELEASE_STORE_FILE")

            if (storeFilePath != "") {
                storeFile = file(env("RELEASE_STORE_FILE"))
                storePassword = env("RELEASE_STORE_PASSWORD")
                keyAlias = env("RELEASE_KEY_ALIAS")
                keyPassword = env("RELEASE_KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "com.doronyg.mylightscontroller"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Pass arguments to disable isolated storage during test execution
        testInstrumentationRunnerArguments += mapOf(
            "useTestStorageService" to "true",
            "no-isolated-storage" to "true",
            "clearPackageData" to "false"
        )

        android.buildFeatures.buildConfig = true
        buildConfigField(
            "String",
            "HA_URL",
            if (withHardCodedSecrets) "\"${System.getenv("HA_URL") ?: ""}\"" else "\"\""
        )
        buildConfigField(
            "String",
            "HA_TOKEN",
            if (withHardCodedSecrets) "\"${System.getenv("HA_TOKEN") ?: ""}\"" else "\"\""
        )
        buildConfigField(
            "String",
            "PORCUPINE_API_KEY",
            if (withHardCodedSecrets) "\"${System.getenv("PORCUPINE_API_KEY") ?: ""}\"" else "\"\""
        )

        manifestPlaceholders["deepLinkScheme"] = "mylightscontroller"
        manifestPlaceholders["deepLinkHostSetup"] = "setup"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // latest stable
    }
}

dependencies {

    // ---------- Core Android ----------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ---------- Compose ----------
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material.icons.extended)

    // ---------- Networking ----------
    // Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)

    // kotlinx.serialization
    implementation(libs.kotlinx.serialization.json)

    // ---------- CameraX ----------
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ---------- ML Kit ----------
    implementation(libs.mlkit.barcode)
    implementation(libs.mlkit.translate)

    // ---------- Other ----------
    implementation(libs.zxing)
    implementation(libs.guava)
    implementation(libs.porcupine.android)

    // ---------- Unit & Instrumentation Tests ----------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Activity testing (ActivityScenario for UI tests)
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test.services:test-services:1.4.2")

    // Test activity hosting runner (allows launching activities in UI tests)
    debugImplementation(libs.androidx.ui.test.manifest)
}