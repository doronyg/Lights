package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.reflect.KMutableProperty0

@Composable
fun SettingsIntSlider(
    title: String,
    description: String,
    pref: KMutableProperty0<Int>,
    min: Int,
    max: Int,
    suffix: String = "s",
    onChange: ((Int) -> Unit)? = null
) {
    var value by remember { mutableIntStateOf(pref.get().coerceIn(min, max)) }

    val (switchThumbColor, switchTrackColor) = lightOnColors()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "$description ($value$suffix)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            Slider(
                value = value.toFloat(),
                onValueChange = {
                    value = it.toInt().coerceIn(min, max)
                },
                onValueChangeFinished = {
                    pref.set(value)
                    onChange?.invoke(value)
                },
                valueRange = min.toFloat()..max.toFloat(),

                // IMPORTANT: no dots
                steps = 0,

                colors = SliderDefaults.colors(
                    thumbColor = switchThumbColor,
                    activeTrackColor = switchTrackColor,
                    inactiveTrackColor = switchTrackColor.copy(alpha = 0.3f)
                )
            )
        }
    }
}