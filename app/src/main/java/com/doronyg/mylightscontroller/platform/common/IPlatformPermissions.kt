package com.doronyg.mylightscontroller.platform.common

/**
 * Platform-agnostic interface for permission handling.
 * Allows requesting and checking permissions across different platforms.
 */
interface IPlatformPermissions {
    /**
     * Check if a specific permission is granted.
     * @param permission Platform-specific permission string (e.g., "CAMERA" on Android)
     * @return true if permission is granted, false otherwise
     */
    fun hasPermission(permission: String): Boolean

    /**
     * Request a permission from the user.
     * @param permission Platform-specific permission string (e.g., "CAMERA" on Android)
     * @return true if permission was granted, false otherwise
     */
    suspend fun requestPermission(permission: String): Boolean
}

