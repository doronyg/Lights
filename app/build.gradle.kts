plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

val withHardCodedSecrets = false

android {
    namespace = "com.doronyg.mylightscontroller"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.doronyg.mylightscontroller"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    kotlinOptions {
        jvmTarget = "11"
    }

}

dependencies {
    // Porcupine Picovoice SDK
    implementation(libs.porcupine.android)
    // Networking for REST calls
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    implementation(libs.gson)

    // Android imports
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.graphics)

    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    implementation("com.google.zxing:javase:3.5.4")

    // CameraX
    implementation("androidx.camera:camera-core:1.6.0")
    implementation("androidx.camera:camera-camera2:1.6.0")
    implementation("androidx.camera:camera-lifecycle:1.6.0")
    implementation("androidx.camera:camera-view:1.6.0")
    implementation("com.google.guava:guava:31.1-android")

    // ML Kit QR scanning
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.mlkit:translate:17.0.3")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}