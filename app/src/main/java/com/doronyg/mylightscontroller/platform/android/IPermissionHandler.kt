package com.doronyg.mylightscontroller.platform.android

/**
 * Interface that Activities must implement to handle permission requests.
 * The activity will trigger the permission request and update its permissionResult StateFlow
 * when the result arrives via onRequestPermissionsResult().
 */
interface IPermissionHandler {
    /**
     * Request a permission. The activity will show the system permission dialog
     * and update its permissionResult StateFlow when done.
     * @param manifestPermission Android manifest permission string (e.g., "android.permission.CAMERA")
     */
    fun requestPermission(manifestPermission: String)
}

