pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Lets Gradle download a full JDK (with jpackage) for desktop packaging, see :desktopApp
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "My Lights Controller"

/**
 * Android modules need the Android SDK. Machines without it (e.g. a Windows box used only
 * for the desktop app) skip them. Override with -Plights.android=true|false.
 */
val androidEnabled: Boolean = providers.gradleProperty("lights.android").orNull?.toBoolean()
    ?: run {
        val fromEnv = listOfNotNull(System.getenv("ANDROID_HOME"), System.getenv("ANDROID_SDK_ROOT"))
        val fromLocalProps = file("local.properties").takeIf { it.exists() }?.let { f ->
            java.util.Properties().apply { f.inputStream().use { load(it) } }.getProperty("sdk.dir")
        }
        (fromEnv + listOfNotNull(fromLocalProps)).any { File(it).isDirectory }
    }
gradle.extra["androidEnabled"] = androidEnabled
if (!androidEnabled) logger.lifecycle("Android SDK not found: building without :app and the Android target")

if (androidEnabled) include(":app")
include(":shared")
include(":desktopApp")
