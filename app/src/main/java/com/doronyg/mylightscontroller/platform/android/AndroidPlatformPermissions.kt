package com.doronyg.mylightscontroller.platform.android

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.doronyg.mylightscontroller.platform.common.IPlatformPermissions
import com.doronyg.mylightscontroller.platform.common.Permission
import com.doronyg.mylightscontroller.utils.Utils

/**
 * Platform-specific permissions handler for Android.
 * Singleton that uses ActivityHolder to get current activity and delegate to it.
 * Permission results are tracked in PlatformBridge (platform-independent layer).
 */
object AndroidPlatformPermissions : IPlatformPermissions {

    /**
     * Map platform-independent Permission enum to Android permission strings.
     */
    private fun permissionToAndroidString(permission: Permission): String {
        return when (permission) {
            Permission.CAMERA -> Manifest.permission.CAMERA
            Permission.MICROPHONE -> Manifest.permission.RECORD_AUDIO
            Permission.LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
        }
    }

    override fun hasPermission(permission: Permission): Boolean {
        val context = AndroidActivityHolder.getCurrentActivity()?.baseContext ?: return false
        val manifestPermission = permissionToAndroidString(permission)

        return ContextCompat.checkSelfPermission(
            context,
            manifestPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission(permission: Permission) {
        // Get the current resumed activity from the holder
        val activity = AndroidActivityHolder.getCurrentActivity()
        if (activity == null) {
            Utils.log("AndroidPlatformPermissions: No activity available for permission request")
            return
        }

        // Activity must implement IPermissionHandler
        if (activity !is IPermissionHandler) {
            Utils.log("AndroidPlatformPermissions: Current activity does not implement IPermissionHandler")
            return
        }

        val manifestPermission = permissionToAndroidString(permission)
        Utils.log("AndroidPlatformPermissions: Requesting $permission from ${activity.javaClass.simpleName}")

        // Delegate to the activity to handle the permission request
        activity.requestPermission(manifestPermission)
    }
}


