package com.doronyg.mylightscontroller.ui

import com.doronyg.mylightscontroller.Settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.services.VoiceControllerService

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    var withVoiceControllerService by remember {
        mutableStateOf(Settings.withVoiceControllerService)
    }

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

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = "Voice Controller Service",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Enable background voice detection service",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = withVoiceControllerService,
                    onCheckedChange = { enabled ->
                        withVoiceControllerService = enabled
                        Settings.withVoiceControllerService = enabled

                        if (enabled) {
                            VoiceControllerService.startService(context, boot = false)
                        } else {
                            VoiceControllerService.stopService(context)
                        }
                    }
                )
            }
        }
    }
}