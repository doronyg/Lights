package com.doronyg.mylightscontroller.platform.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.doronyg.mylightscontroller.platform.common.IPlatformPermissions

class AndroidPlatformPermissions(
    private val context: Context
) : IPlatformPermissions {

    override fun hasPermission(permission: String): Boolean {
        val manifestPermission = when (permission) {
            "CAMERA" -> Manifest.permission.CAMERA
            else -> permission
        }

        return ContextCompat.checkSelfPermission(
            context,
            manifestPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestPermission(permission: String): Boolean {
        // If already granted, return true immediately
        if (hasPermission(permission)) {
            return true
        }

        // On Android, permissions must be requested from an Activity context
        // This will require using the permission request flow through Compose
        // For now, we return false - the actual request happens in the Compose layer
        return false
    }
}


