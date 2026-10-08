// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}

// Kotlin/Wasm tooling (Node.js, Yarn, Binaryen) is resolved from the repositories declared in
// settings.gradle.kts. A null downloadBaseUrl stops the Kotlin plugin from adding its own
// repositories, which FAIL_ON_PROJECT_REPOS forbids.
allprojects {
    fun Project.useSettingsRepositoriesForWasmTools() =
        extensions.extensionsSchema.elements.forEach { schema ->
            (extensions.findByName(schema.name) as? org.jetbrains.kotlin.gradle.targets.js.EnvSpec<*>)
                ?.downloadBaseUrl?.set(null as String?)
        }
    plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsPlugin> { useSettingsRepositoriesForWasmTools() }
    plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsRootPlugin> { useSettingsRepositoriesForWasmTools() }
    plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnPlugin> { useSettingsRepositoriesForWasmTools() }
    plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.binaryen.BinaryenPlugin> { useSettingsRepositoriesForWasmTools() }
}