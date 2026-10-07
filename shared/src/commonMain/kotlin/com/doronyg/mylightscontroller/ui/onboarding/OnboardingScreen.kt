package com.doronyg.mylightscontroller.ui.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.onboarding.applyDemoConfig
import com.doronyg.mylightscontroller.ui.ScrollableColumn
import com.doronyg.mylightscontroller.ui.SettingsActionCard
import com.doronyg.mylightscontroller.ui.SimpleTopBar
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onManualSetup: () -> Unit,
    onSetupComplete: () -> Unit,
    onQrScan: () -> Unit,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()

    ScrollableColumn(
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
                scope.launch {
                    applyDemoConfig()
                    onSetupComplete()
                }
            }
        )
    }
}

