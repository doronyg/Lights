package com.doronyg.mylightscontroller.ui
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.doronyg.mylightscontroller.ha.HaDevice
@Composable
fun RoomIcon(
    device: HaDevice,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    val imageVector = remember(device) {
        device.getDeviceIcon()
    }
    Icon(
        imageVector = imageVector,
        contentDescription = device.name,
        modifier = modifier,
        tint = tint
    )
}
