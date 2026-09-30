package com.doronyg.mylightscontroller.onboarding.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.Config
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.ha.mock.HomeAssistantApiMock
import com.doronyg.mylightscontroller.ui.SettingsActionCard
import com.doronyg.mylightscontroller.ui.SimpleTopBar
import com.doronyg.mylightscontroller.utils.MockUtils
import com.doronyg.mylightscontroller.utils.Utils

@Composable
fun OnboardingScreen(
    onManualSetup: () -> Unit,
    onSetupComplete: () -> Unit,
    onQrScan: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        SimpleTopBar(
            title = "Welcome",
            onBack = null // Can't close
        )

        Text("Set up Home Assistant")

        Spacer(Modifier.height(16.dp))

        // 1. Manual setup
        SettingsActionCard(
            title = "Enter details manually",
            description = "Input your Home Assistant URL, token and API key",
            onClick = onManualSetup
        )

        // 2. Scan via external camera
        SettingsActionCard(
            title = "Scan QR code",
            description = "Open camera to scan setup QR from another device",
            onClick = onQrScan
        )

        // 3. Demo mode
        SettingsActionCard(
            title = "Use demo mode",
            description = "Explore the app with sample data",
            onClick = {
                applyDemoConfig()
                onSetupComplete()
            }
        )
    }
}

fun applyDemoConfig() {
    Config.homeAssistantUrl = "http://demo.local"
    Config.homeAssistantToken = "demo-token"
//    Config.PORCUPINE_API_KEY = "demo-key"

    Settings.useMockHaData = true
    Settings.isConfigured = true
    Settings.saveMockHaData = true

    Utils.appContext?.let {
        HomeAssistantApiMock.populateMockData(MockUtils.loadMockDevices(it))
    }

}