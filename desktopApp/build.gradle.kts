import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing) // Dispatchers.Main on the Swing/AWT thread
    implementation(libs.ktor.client.okhttp)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
}

// Reuse the Android demo data as classpath resources (single source of truth)
sourceSets {
    main {
        resources.srcDir("../app/src/main/assets")
    }
}

// App icon: reuse the Android launcher icon (copied to the classpath root)
tasks.processResources {
    from("../app/src/main/res/mipmap-xxxhdpi") {
        include("ic_bee_launcher.webp")
    }
}

compose.desktop {
    application {
        mainClass = "com.doronyg.mylightscontroller.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "Lights"
            packageVersion = "1.0.0"
        }
    }
}
