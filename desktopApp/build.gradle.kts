import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

// Bytecode for Java 17+, whatever JDK runs Gradle (it must also run on the bundled JRE)
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
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

        // Packaging needs `jpackage`, which Android Studio's bundled JDK lacks. For packaging
        // tasks only, use a full JDK (downloaded once by Gradle if not installed).
        val packaging = gradle.startParameter.taskNames.any { task ->
            listOf("package", "Distributable", "RuntimeModules").any { task.contains(it, ignoreCase = true) }
        }
        if (packaging) {
            javaHome = javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
                vendor.set(JvmVendorSpec.ADOPTIUM)
            }.get().metadata.installationPath.asFile.absolutePath
        }
        nativeDistributions {
            // Each format must be built on its own OS (jpackage can't cross-compile):
            //   macOS:   ./gradlew :desktopApp:packageDmg
            //   Windows: gradlew.bat :desktopApp:packageMsi   (or packageExe)
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe)
            packageName = "Lights"
            packageVersion = "1.0.0" // MAJOR.MINOR.BUILD (MSI requires this form)
            description = "Control your Home Assistant lights"
            vendor = "doronyg"

            // Bundled JRE: only the modules the app needs (from :desktopApp:suggestRuntimeModules,
            // plus jdk.crypto.ec for HTTPS servers with EC certificates)
            modules("java.instrument", "java.management", "jdk.unsupported", "jdk.crypto.ec")

            macOS {
                iconFile.set(project.file("icons/Lights.icns"))
                bundleID = "com.doronyg.mylightscontroller.desktop"
            }
            windows {
                iconFile.set(project.file("icons/Lights.ico"))
                menu = true             // Start menu entry
                menuGroup = "Lights"
                shortcut = true         // Desktop shortcut
                perUserInstall = true   // No admin rights needed
                dirChooser = true
                // Must never change: lets new versions upgrade (replace) old installs
                upgradeUuid = "42B9650F-FDDD-4553-A1B6-D0F19D2D91EF"
            }
        }
    }
}





