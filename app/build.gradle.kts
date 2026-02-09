plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.doronyg.mylightscontroller"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.doronyg.mylightscontroller"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        android.buildFeatures.buildConfig = true
        buildConfigField(
            "String",
            "HA_URL",
            "\"${System.getenv("HA_URL") ?: ""}\""
        )
        buildConfigField(
            "String",
            "HA_TOKEN",
            "\"${System.getenv("HA_TOKEN") ?: ""}\""
        )
        buildConfigField(
            "String",
            "PORCUPINE_API_KEY",
            "\"${System.getenv("PORCUPINE_API_KEY") ?: ""}\""
        )
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
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}