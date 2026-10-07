package com.doronyg.mylightscontroller.ui.tests

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.doronyg.mylightscontroller.R
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.ha.mock.HomeAssistantApiMock
import com.doronyg.mylightscontroller.onboarding.applyDemoConfig
import com.doronyg.mylightscontroller.platform.android.AndroidUtils
import com.doronyg.mylightscontroller.utils.MockUtils

/**
 * Test version of MainActivity for visual regression testing.
 *
 * This activity is identical to MainActivity but skips:
 * - Permission requests
 * - VoiceControllerService startup
 * - Deep link handling
 * - AppTranslator initialization
 *
 * This allows us to test the UI without being blocked by system dialogs.
 */
class VisualTestMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize context FIRST
        AndroidUtils.appContext = applicationContext

        // Set up mock data directly (avoid applyDemoConfig circular initialization)
        Settings.useMockHaData = true
        Settings.saveMockHaData = true
        Settings.isConfigured = true
        applyDemoConfig()

        // Populate mock data
        HomeAssistantApiMock.populateMockData(MockUtils.loadMockDevices())

        // Set up UI without requiring permissions
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Skip:
        // - requestPermissions()
        // - VoiceControllerService.startService()
        // - AppTranslator.init()
        // - handleDeepLink()
    }
}



