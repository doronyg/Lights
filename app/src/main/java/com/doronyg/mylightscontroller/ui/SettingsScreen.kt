package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.services.VoiceControllerService

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Voice Controller Service
        SettingsSwitch(
            title = "Voice Controller Service",
            description = "Enable background voice detection service",
            pref = Settings::withVoiceControllerService,
            onToggle = { enabled ->
                if (enabled) {
                    VoiceControllerService.startService(context, boot = false)
                } else {
                    VoiceControllerService.stopService(context)
                }
            }
        )

        // Save Mock HA Data
        SettingsSwitch(
            title = "Save mock HA data",
            description = "Save the last Home Assistant data locally for testing",
            pref = Settings::saveMockHaData
        )

        // Use Mock HA Data
        SettingsSwitch(
            title = "Use mock HA data",
            description = "Use saved Home Assistant data instead of the network",
            pref = Settings::useMockHaData
        )
    }
}