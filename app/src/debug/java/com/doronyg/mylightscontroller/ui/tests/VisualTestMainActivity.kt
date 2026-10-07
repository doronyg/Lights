package com.doronyg.mylightscontroller.ui.tests

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.doronyg.mylightscontroller.R
import com.doronyg.mylightscontroller.onboarding.applyDemoConfig
import com.doronyg.mylightscontroller.platform.android.AndroidUtils
import kotlinx.coroutines.launch

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

        enableEdgeToEdge()

        // Demo config loads mock devices asynchronously; show UI only once data is ready
        // so screenshots are deterministic.
        lifecycleScope.launch {
            applyDemoConfig()

            // Set up UI without requiring permissions
            setContentView(R.layout.activity_main)

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Skip:
        // - requestPermissions()
        // - VoiceControllerService.startService()
        // - AppTranslator.init()
        // - handleDeepLink()
    }
}



