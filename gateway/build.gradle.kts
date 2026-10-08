import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

/**
 * Gateway for the web app on a NAS (Docker): serves the web app, forwards the few Home Assistant
 * calls it needs (adding the token), and stores the HA URL + token entered on first-run setup.
 *
 *   ./gradlew :gateway:bundle   -> gateway/build/bundle/lights/ (+ lights.zip) for Container Manager
 *   ./gradlew :gateway:run      -> local run on :8321 (uses webApp's release build, data in gateway/build/data)
 *
 * See gateway/README.md.
 */
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

application {
    mainClass.set("com.doronyg.mylightscontroller.gateway.MainKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.kotlinx.coroutines.core)
    runtimeOnly(libs.slf4j.simple)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.mock)
}

tasks.test {
    useJUnit()
}

val webDist = rootProject.layout.projectDirectory.dir("webApp/build/dist/wasmJs/productionExecutable")

tasks.named<JavaExec>("run") {
    dependsOn(":webApp:wasmJsBrowserDistribution")
    environment("LIGHTS_SITE_DIR", webDist.asFile.absolutePath)
    environment("LIGHTS_DATA_DIR", layout.buildDirectory.dir("data").get().asFile.absolutePath)
}

/**
 * Server + dependencies in one jar with a fixed name, so uploading a new bundle over the old
 * folder replaces it (File Station doesn't delete leftover files).
 */
val fatJar by tasks.registering(Jar::class) {
    archiveFileName.set("lights-gateway.jar")
    destinationDirectory.set(layout.buildDirectory.dir("fatJar"))
    manifest { attributes("Main-Class" to application.mainClass.get()) }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    from({ configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) } })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/versions/*/module-info.class", "module-info.class")
}

/** The folder to upload to the NAS: Dockerfile + compose file + server jar + web app. */
val bundleFolder by tasks.registering(Sync::class) {
    group = "distribution"
    description = "Builds the Container Manager project folder (gateway/build/bundle/lights)"
    dependsOn(":webApp:wasmJsBrowserDistribution")
    into(layout.buildDirectory.dir("bundle/lights"))
    from("docker")
    from(fatJar)
    into("site") {
        from(webDist)
        exclude("**/*.map")
    }
}

val bundle by tasks.registering(Zip::class) {
    group = "distribution"
    description = "Builds the NAS bundle: gateway/build/bundle/lights/ and lights.zip"
    from(bundleFolder) { into("lights") }
    archiveFileName.set("lights.zip")
    destinationDirectory.set(layout.buildDirectory.dir("bundle"))
}
