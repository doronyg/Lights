package com.doronyg.mylightscontroller.ui.onboarding

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.Config
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.ui.ScrollableColumn
import com.doronyg.mylightscontroller.ui.SettingsActionCard
import com.doronyg.mylightscontroller.ui.SettingsTextField
import com.doronyg.mylightscontroller.ui.SimpleTopBar
import com.doronyg.mylightscontroller.ui.lightOnColors
import com.doronyg.mylightscontroller.utils.Utils
import io.ktor.http.Url

@Composable
fun ManualSetupScreen(
    onSave: () -> Unit,
    onBack: () -> Unit
) {

    var url by remember { mutableStateOf(Config.homeAssistantUrl) }
    var token by remember { mutableStateOf(Config.homeAssistantToken) }
    var porcupineKey by remember { mutableStateOf("") }

    var hasSubmitted by remember { mutableStateOf(false) }

    val lightOnColors = lightOnColors()
    val accentColor = if (isSystemInDarkTheme()) lightOnColors.first else lightOnColors.second

    // ----------------------------
    // Validation functions
    // ----------------------------
    fun validateUrl(value: String): String? {
        if (value.isBlank()) return "URL is required"

        // Ktor's parser defaults a missing scheme to http, so check the raw string first
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            return "Must start with http or https"
        }

        val uri = try {
            Url(value)
        } catch (e: Exception) {
            Utils.logException(e, "ManualSetupScreen validation failed")
            return "Invalid URL"
        }

        return when {
            uri.host.isBlank() -> "Invalid host"
            else -> null
        }
    }

    fun validateToken(value: String): String? {
        return when {
            value.isBlank() -> "Token is required"
            value.length < 10 -> "Token looks too short"
            else -> null
        }
    }

    fun validatePorcupine(value: String): String? {
        return when {
            value.isBlank() -> null
            value.length < 10 -> "Key looks too short"
            else -> null
        }
    }

    val urlError = if (hasSubmitted) validateUrl(url) else null
    val tokenError = if (hasSubmitted) validateToken(token) else null
    val porcupineError = if (hasSubmitted) validatePorcupine(porcupineKey) else null

    val isValid =
        validateUrl(url) == null &&
                validateToken(token) == null &&
                validatePorcupine(porcupineKey) == null

    // ----------------------------
    // UI
    // ----------------------------
    ScrollableColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
    ) {

        SimpleTopBar(
            title = "Manual Setup",
            onBack = onBack
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Enter your Home Assistant details",
            color = accentColor
        )

        Spacer(Modifier.height(16.dp))

        // URL
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                SettingsTextField(
                    value = url,
                    onValueChange = { url = it.trim() },
                    label = "Home Assistant URL",
                    accentColor = accentColor,
                    error = urlError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Token
                SettingsTextField(
                    value = token,
                    onValueChange = { token = it.trim() },
                    label = "Access Token",
                    accentColor = accentColor,
                    error = tokenError,
                    secure = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Porcupine
                SettingsTextField(
                    value = porcupineKey,
                    onValueChange = { porcupineKey = it.trim() },
                    label = "Porcupine API Key (optional)",
                    accentColor = accentColor,
                    error = porcupineError,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        SettingsActionCard(
            title = "Save configuration",
            description = if (isValid) {
                "Apply settings and continue"
            } else {
                "Fix the errors above"
            },
            onClick = {

                if (!isValid) return@SettingsActionCard

                Config.homeAssistantUrl = url
                Config.homeAssistantToken = token

                if (porcupineKey.isNotBlank()) {
                    Config.porcupineApiKey = porcupineKey
                }

                Settings.useMockHaData = false
                Settings.isConfigured = true

                onSave()
            }
        )
    }
}

