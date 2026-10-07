package com.doronyg.mylightscontroller.platform.android.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.doronyg.mylightscontroller.Config
import com.doronyg.mylightscontroller.R
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.onboarding.models.HaSetupPayload.Companion.parseSetupDeepLinkSafe
import com.doronyg.mylightscontroller.platform.android.AndroidPlatformPermissions
import com.doronyg.mylightscontroller.platform.android.IPermissionHandler
import com.doronyg.mylightscontroller.platform.android.services.VoiceControllerService
import com.doronyg.mylightscontroller.platform.common.Permission
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.utils.Utils

class MainActivity : AppCompatActivity(), IPermissionHandler {

    private var pendingPermission: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        requestPermissions()
        VoiceControllerService.startService(this, false)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    // Newer OS versions deliver results through this overload (with deviceId)...
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        handlePermissionResult(grantResults)
    }

    // ...older ones through this one. On some versions one delegates to the other,
    // so handlePermissionResult() must be safe to call twice for the same result.
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handlePermissionResult(grantResults)
    }

    /**
     * Single entry point for permission results. Idempotent: the pending permission is
     * consumed on the first call, so a second delivery of the same result is ignored.
     */
    private fun handlePermissionResult(grantResults: IntArray) {
        val manifestPermission = pendingPermission ?: return
        pendingPermission = null

        // Extract result from first permission (we only request one at a time)
        val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        Utils.log("MainActivity: Permission result for $manifestPermission: $granted")

        // Convert manifest permission string back to platform-independent Permission enum
        manifestPermissionToEnum(manifestPermission)?.let { permission ->
            PlatformBridge.onPermissionResult(permission, granted)
        }

        VoiceControllerService.startService(this, false)
    }


    override fun requestPermission(manifestPermission: String) {
        Utils.log("MainActivity: Requesting permission: $manifestPermission")
        pendingPermission = manifestPermission
        ActivityCompat.requestPermissions(this, arrayOf(manifestPermission), 1001)
    }

    private fun requestPermissions() {
        if (!AndroidPlatformPermissions.isGranted(this, Permission.MICROPHONE) && !requestMicrophonePermission) {
            requestMicrophonePermission = true
            Utils.requestAppPermissions()
        }
    }

    private fun handleDeepLink(intent: Intent?) {
        Utils.post({
            intent?.data?.toString()?.let {
                parseSetupDeepLinkSafe(it)?.apply {
                    Config.homeAssistantUrl = haUrl
                    Config.homeAssistantToken = haToken
                    porcupineKey?.let { porcupineKey ->
                        Config.porcupineApiKey = porcupineKey
                    }
                    Settings.isConfigured = true
                }
            }
        })
    }

    /**
     * Convert Android manifest permission string to platform-independent Permission enum.
     */
    private fun manifestPermissionToEnum(manifestPermission: String): Permission? {
        return when (manifestPermission) {
            Manifest.permission.CAMERA -> Permission.CAMERA
            Manifest.permission.RECORD_AUDIO -> Permission.MICROPHONE
            Manifest.permission.ACCESS_FINE_LOCATION -> Permission.LOCATION
            else -> {
                Utils.log("MainActivity: Unknown permission: $manifestPermission")
                null
            }
        }
    }

    companion object {
        var requestMicrophonePermission = false
    }
}