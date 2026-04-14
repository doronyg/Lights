package com.doronyg.mylightscontroller.ui

import android.Manifest
import android.content.Intent
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
import com.doronyg.mylightscontroller.services.VoiceControllerService
import com.doronyg.mylightscontroller.utils.Utils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.appContext = applicationContext
        handleDeepLink(intent)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestPermissions()
        VoiceControllerService.startService(this, false)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String?>, grantResults: IntArray, deviceId: Int) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        VoiceControllerService.startService(this, false)
    }

    private fun requestPermissions() {
        if (!Utils.checkAppPermissions(this)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    private fun handleDeepLink(intent: Intent?) {
        Utils.post( {
            intent?.data?.toString()?.let {
                parseSetupDeepLinkSafe(it)?.apply {
                    Config.homeAssistantUrl = haUrl
                    Config.homeAssistantToken = haToken
                    porcupineKey?.let {
                        Config.porcupineApiKey = it
                    }
                    Settings.isConfigured = true
                }
            }
        })
    }

}