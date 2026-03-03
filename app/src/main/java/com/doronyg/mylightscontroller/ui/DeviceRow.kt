package com.doronyg.mylightscontroller.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.ha.HaDevice

private val twoLineHeight = 48.dp

@Composable
fun DeviceRow(
    device: HaDevice,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onBrightnessChange: ((Int) -> Unit)? = null
) {
    var sliderValue by remember {
        mutableFloatStateOf(device.attributes.brightnessPct?.toFloat() ?: 0f)
    }

    val (lightOnBackground, lightOnContent) = lightOnColors()

    val backgroundColor by animateColorAsState(
        targetValue = if (device.isOn) lightOnBackground
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "deviceBackground"
    )

    val contentColor =
        if (device.isOn) lightOnContent
        else MaterialTheme.colorScheme.onSurfaceVariant

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
            .animateContentSize()
    ) {
        // Container-based breakpoint
        val horizontalPadding =
            if (maxWidth < 420.dp) 4.dp else 12.dp

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
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

                Spacer(Modifier.width(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.height(twoLineHeight),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor,
                            maxLines = 2
                        )
                    }

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
                                thumbColor = lightOnContent,
                                activeTrackColor = lightOnContent,
                                inactiveTrackColor = lightOnContent.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    }
}
