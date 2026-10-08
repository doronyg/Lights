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

        // Kotlin/Wasm tooling, downloaded once by Gradle (see "downloadBaseUrl" in build.gradle.kts)
        ivy("https://nodejs.org/dist") {
            name = "Node.js distributions"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
        ivy("https://github.com/yarnpkg/yarn/releases/download") {
            name = "Yarn distributions"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
        ivy("https://github.com/WebAssembly/binaryen/releases/download") {
            name = "Binaryen distributions"
            patternLayout { artifact("version_[revision]/binaryen-version_[revision]-[classifier].[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.github.webassembly", "binaryen") }
        }
    }
}

// No spaces: the Kotlin/Wasm build derives npm package names from it
rootProject.name = "MyLightsController"

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
include(":webApp")
