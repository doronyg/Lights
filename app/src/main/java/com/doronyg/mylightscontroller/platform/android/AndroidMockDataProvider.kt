package com.doronyg.mylightscontroller.platform.android

import com.doronyg.mylightscontroller.platform.common.IPlatformMockDataProvider
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.utils.Utils
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android implementation of IPlatformMockDataProvider.
 * Uses the app cache dir for writes and assets for reads.
 */
object AndroidMockDataProvider : IPlatformMockDataProvider {

    override fun writeFile(fileName: String, content: String): String? {
        return try {
            val appContext = AndroidUtils.appContext
                ?: throw IllegalStateException("appContext not initialized")

            val file = File(appContext.cacheDir, fileName)
            file.writeText(content)

            Utils.log("File written to: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Utils.logException(e, "Failed to write file: $fileName")
            null
        }
    }

    override suspend fun readAssetFile(fileName: String): String =
        withContext(PlatformBridge.dispatchers.ioDispatcher()) {
            val appContext = AndroidUtils.appContext
                ?: throw IllegalStateException("appContext not initialized")

            appContext.assets
                .open(fileName)
                .bufferedReader()
                .use { it.readText() }
        }
}
