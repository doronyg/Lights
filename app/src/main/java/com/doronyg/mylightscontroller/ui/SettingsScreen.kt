package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.BuildConfig
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.ui.navigator.Screen
import com.doronyg.mylightscontroller.ui.navigator.UiEvent
import com.doronyg.mylightscontroller.utils.EmailHelper
import com.doronyg.mylightscontroller.utils.MockUtils.saveMockData

@Composable
fun SettingsScreen(
    onEvent: (UiEvent) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {

        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            SettingsSwitch(
                title = "Voice Controller Service",
                description = "Enable background voice detection service",
                pref = Settings::withVoiceControllerService,
                onToggle = { enabled ->
                    if (enabled) {
                        PlatformBridge.voiceController.startService(context, boot = false)
                    } else {
                        PlatformBridge.voiceController.stopService(context)
                    }
                }
            )
        }

        item {
            SettingsIntSlider(
                title = "HA refresh interval",
                description = "How often lights are refreshed while UI is visible",
                pref = Settings::secondsForHaUpdateOnUi,
                min = 5,
                max = 300,
                suffix = "s"
            )
        }

        item {
            SettingsSwitch(
                title = "Save mock HA data",
                description = "Save the last Home Assistant data locally for testing",
                pref = Settings::saveMockHaData
            )
        }

        item {
            SettingsSwitch(
                title = "Use mock HA data",
                description = "Use saved Home Assistant data instead of the network",
                pref = Settings::useMockHaData
            )
        }

        item {
            SettingsActionCard(
                title = "Show Setup QR",
                description = "Scan from another device to copy your setup",
                onClick = {
                    onEvent(UiEvent.NavigateTo(Screen.SetupQr))
                }
            )
        }

        item {
            SettingsActionCard(
                title = "Export data via email",
                description = "Export lights JSON and send via email",
                onClick = {
                    EmailHelper.sendHaDataViaEmail(context.applicationContext)
                }
            )
        }

        if (BuildConfig.DEBUG) {
            item {
                SettingsActionCard(
                    title = "Export HA data (debug)",
                    description = "Write lights JSON to cache",
                    onClick = {
                        saveMockData(context.applicationContext)
                    }
                )
            }
        }
    }
}