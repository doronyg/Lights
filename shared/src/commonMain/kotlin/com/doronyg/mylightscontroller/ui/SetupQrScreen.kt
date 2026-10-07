package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.onboarding.models.AppConfig
import com.doronyg.mylightscontroller.onboarding.models.HaSetupPayload
import com.doronyg.mylightscontroller.onboarding.qr.QrCode
import com.doronyg.mylightscontroller.onboarding.qr.QrEncoder
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import kotlinx.coroutines.withContext

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

    val qr = produceState<QrCode?>(initialValue = null, encoded) {
        value = withContext(PlatformBridge.dispatchers.defaultDispatcher()) {
            QrEncoder.encodeText(encoded)
        }
    }.value

    ScrollableColumn(
        modifier = Modifier.padding(16.dp)
    ) {

        SimpleTopBar(
            title = "Settings",
            onBack = onClose
        )

        Text("Scan to set up Home Assistant")

        Spacer(Modifier.height(16.dp))

        if (qr != null) {
            QrCodeImage(
                qr = qr,
                contentDescription = "Setup QR",
                modifier = Modifier.size(250.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text("Scan this QR from another device to copy your setup.")
    }
}
