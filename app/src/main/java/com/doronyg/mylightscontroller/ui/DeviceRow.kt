package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.ha.DeviceType
import com.doronyg.mylightscontroller.ha.HaDevice

@Composable
fun DeviceRow(
    device: HaDevice,
    onClick: () -> Unit,
    onBrightnessChange: ((Int) -> Unit)? = null
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when (device.type) {
            DeviceType.LIGHT -> Icons.Default.Lightbulb
            DeviceType.SWITCH -> Icons.Default.ToggleOn
            else -> Icons.Default.Devices
        }

        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(device.name)

            val stateText = when {
                device.isOn && device.type == DeviceType.LIGHT &&
                        device.attributes.brightnessPct != null ->
                    "ON • brightness ${device.attributes.brightnessPct}"

                device.isOn -> "ON"
                else -> "OFF"
            }
            Text(stateText, style = MaterialTheme.typography.bodySmall)

            if (device.dimmable && device.isOn) {
                val currentBrightness = device.attributes.brightnessPct ?: 0
                var sliderValue by remember { mutableFloatStateOf(currentBrightness.toFloat()) }
                Slider(
                    value = currentBrightness.toFloat(),
                    onValueChange = { newValue ->
                        sliderValue = newValue // track the drag
                    },
                    onValueChangeFinished = {
                        onBrightnessChange?.invoke(sliderValue.toInt())
                    },
                    valueRange = 0f..100f,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        val color = if (device.isOn) Color.Green else Color.Gray
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
    }
}



