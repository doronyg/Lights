package com.doronyg.mylightscontroller.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.onboarding.QrImageGenerator
import com.doronyg.mylightscontroller.onboarding.models.AppConfig
import com.doronyg.mylightscontroller.onboarding.models.HaSetupPayload

@Composable
fun SetupQrScreen(
    config: AppConfig,
    onClose: () -> Unit) {

    val payload = remember(config) {
        HaSetupPayload(
            haUrl = config.homeAssistantUrl,
            haToken = config.homeAssistantToken,
            porcupineKey = config.porcupineApiKey
        )
    }

    val encoded = remember(payload) {
        HaSetupPayload.buildSetupDeepLink(payload)
    }

    val bitmapState = produceState<Bitmap?>(initialValue = null, encoded) {
        value = QrImageGenerator.generateQrBitmap(encoded)
    }

    val bitmap = bitmapState.value

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        SimpleTopBar(
            title = "Settings",
            onBack = onClose
        )

        Text("Scan to set up Home Assistant")

        Spacer(Modifier.height(16.dp))

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Setup QR",
                modifier = Modifier.size(250.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text("Scan this QR from another device to copy your setup.")

        Spacer(Modifier.height(16.dp))
    }
}
