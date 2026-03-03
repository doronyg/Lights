package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.ha.HaDevice

@Composable
fun DeviceGrid(
    devices: List<HaDevice>,
    viewModel: DevicesViewModel
) {
    BoxWithConstraints(
        modifier = Modifier.Companion.fillMaxSize()
    ) {
        // Responsive column count based on width
        val columnCount = when {
            maxWidth < 300.dp -> 1 // super narrow
            maxWidth < 600.dp -> 2 // compact / small
            else -> 3              // normal
        }

        val horizontalPadding = if (columnCount == 1) 4.dp else 8.dp
        val spacing = if (columnCount == 1) 6.dp else 12.dp

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            contentPadding = PaddingValues(vertical = spacing)
        ) {
            items(
                items = devices,
                key = { it.entityId },
                span = { device ->
                    if (device.dimmable && device.isOn)
                        GridItemSpan(columnCount)   // full width dynamically
                    else
                        GridItemSpan(1)
                }
            ) { device ->
                DeviceRow(
                    device = device,
                    modifier = Modifier.Companion.fillMaxWidth(),
                    onClick = {
                        if (device.isOn) {
                            viewModel.turnOff(listOf(device))
                        } else {
                            viewModel.turnOn(listOf(device))
                        }
                    },
                    onBrightnessChange = { newBrightness ->
                        viewModel.turnOn(listOf(device), newBrightness)
                    }
                )
            }
        }
    }
}