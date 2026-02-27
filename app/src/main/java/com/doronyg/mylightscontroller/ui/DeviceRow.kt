package com.doronyg.mylightscontroller.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.ha.HaDevice

private val LightOnBackground = Color(0xFFFFE9A3)
private val LightOnContent = Color(0xFF5C4400)

@Composable
fun DeviceRow(
    device: HaDevice,
    onClick: () -> Unit,
    onBrightnessChange: ((Int) -> Unit)? = null
) {
    var sliderValue by remember {
        mutableFloatStateOf(device.attributes.brightnessPct?.toFloat() ?: 0f)
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (device.isOn)
            LightOnBackground
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "deviceBackground"
    )

    val contentColor =
        if (device.isOn)
            LightOnContent
        else
            MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoomIcon(
                device = device,
                modifier = Modifier.size(36.dp),
                tint = contentColor
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )

                if (device.dimmable && device.isOn) {
                    Spacer(Modifier.height(8.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = {
                            onBrightnessChange?.invoke(sliderValue.toInt())
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = LightOnContent,
                            activeTrackColor = LightOnContent,
                            inactiveTrackColor = LightOnContent.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}