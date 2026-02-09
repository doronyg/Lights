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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            version("gson", "2.13.2")
            version("okhttp", "4.11.0")
            version("okhttp-logging", "4.11.0")
            version("porcupine", "2.3.0") // latest Android SDK from Picovoice
        }
    }
}

rootProject.name = "My Lights Controller"
include(":app")
 