package com.doronyg.mylightscontroller.platform.common

/**
 * Platform-agnostic interface for permission handling.
 * Uses platform-independent Permission enum instead of platform-specific strings.
 *
 * requestPermission() triggers the request but doesn't wait for result.
 * The UI observes a StateFlow on the activity to be notified of permission changes.
 */
interface IPlatformPermissions {
    /**
     * Check if a specific permission is granted.
     * @param permission Platform-independent permission enum
     * @return true if permission is granted, false otherwise
     */
    fun hasPermission(permission: Permission): Boolean

    /**
     * Request a permission from the user.
     * This triggers the permission dialog but returns immediately.
     * The actual result will be delivered via the activity's permissionResult StateFlow.
     * @param permission Platform-independent permission enum
     */
    fun requestPermission(permission: Permission)
}

