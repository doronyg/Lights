package com.doronyg.mylightscontroller.platform.common

/**
 * Platform-agnostic interface for permission handling.
 * Uses platform-independent Permission enum instead of platform-specific strings.
 *
 * requestPermission() triggers the request but doesn't wait for result.
 * Results are published to [PlatformBridge.permissionResults], which the UI can observe.
 */
interface IPlatformPermissions {
    /**
     * Check if a specific permission is granted.
     * Suspending because some platforms (e.g. browsers) can only query permissions asynchronously.
     * @param permission Platform-independent permission enum
     * @return true if permission is granted, false otherwise
     */
    suspend fun hasPermission(permission: Permission): Boolean

    /**
     * Request a permission from the user.
     * This triggers the permission dialog but returns immediately.
     * The result will be delivered via [PlatformBridge.permissionResults].
     * @param permission Platform-independent permission enum
     */
    fun requestPermission(permission: Permission)
}

