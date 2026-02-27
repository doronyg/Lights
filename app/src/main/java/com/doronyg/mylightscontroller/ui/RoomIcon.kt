package com.doronyg.mylightscontroller.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HaIconResolver

@Composable
fun RoomIcon(
    device: HaDevice,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    val icon = remember(device) {
        HaIconResolver.resolve(device)
    }

    Icon(
        imageVector = icon,
        contentDescription = device.name,
        modifier = modifier,
        tint = tint
    )
}