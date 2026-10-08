package com.doronyg.mylightscontroller.gateway

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import java.util.Properties

/** Where the gateway forwards to, and the token it adds. */
data class HaConnection(val url: String, val token: String) {
    override fun toString() = "HaConnection(url=$url, token=***)" // never log the token
}

/**
 * The HA URL + token entered on first-run setup, in `<dataDir>/ha.properties`.
 *
 * The file is readable by the server's user only (mode 600) and is never sent to browsers.
 * It lives in the Docker volume, so it survives restarts and updates.
 * Deleting it (and restarting) brings back the setup screen.
 */
class HaConfigStore(private val dataDir: File) {
    private val file = File(dataDir, FILE_NAME)

    @Volatile
    private var cached: HaConnection? = load()

    val connection: HaConnection? get() = cached

    private fun load(): HaConnection? {
        if (!file.isFile) return null
        val props = Properties().apply { file.inputStream().use { load(it) } }
        val url = props.getProperty(KEY_URL)?.takeIf { it.isNotBlank() } ?: return null
        val token = props.getProperty(KEY_TOKEN)?.takeIf { it.isNotBlank() } ?: return null
        return HaConnection(url, token)
    }

    @Synchronized
    fun save(connection: HaConnection) {
        dataDir.mkdirs()
        val tmp = File(dataDir, "$FILE_NAME.tmp").toPath()
        Files.deleteIfExists(tmp)
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            Files.createFile(tmp, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")))
        } else {
            Files.createFile(tmp)
        }
        Files.newOutputStream(tmp).use { out ->
            Properties().apply {
                setProperty(KEY_URL, connection.url)
                setProperty(KEY_TOKEN, connection.token)
            }.store(out, "Lights gateway - Home Assistant connection. Delete this file and restart to set up again.")
        }
        Files.move(tmp, file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        cached = connection
    }

    companion object {
        const val FILE_NAME = "ha.properties"
        private const val KEY_URL = "haUrl"
        private const val KEY_TOKEN = "haToken"
    }
}

