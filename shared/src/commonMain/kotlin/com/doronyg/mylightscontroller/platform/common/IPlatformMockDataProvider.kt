package com.doronyg.mylightscontroller.platform.common

/**
 * Common interface for platform-specific mock data file I/O.
 *
 * This defines the contract that will be implemented per-platform.
 * Future: will become `expect object` when converting to Kotlin Multiplatform.
 */
interface IPlatformMockDataProvider {
    /**
     * Write content to a cache file.
     * @return Platform path of the written file, or null on error
     */
    fun writeFile(fileName: String, content: String): String?

    /**
     * Read content from a bundled asset file.
     * @return File content as string
     * @throws Exception if file not found or read fails
     */
    suspend fun readAssetFile(fileName: String): String
}

