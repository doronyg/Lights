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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.ha.HaDevice

@Composable
fun DeviceGrid(
    devices: List<HaDevice>,
    stateHolder: DevicesStateHolder
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val fontScale = LocalDensity.current.fontScale
        val accessibilityMode = fontScale >= 1.3f

        // Responsive column count based on width or font scale
        val columnCount = when {
            // very small phones
            maxWidth < 300.dp -> 1

            // tablets always 3
            maxWidth >= 600.dp -> 3

            // accessibilityMode forces 1 column
            accessibilityMode -> 1

            // phones: larger text => reduce density
            else -> 2
        }

        val horizontalPadding = if (columnCount == 1) 4.dp else 8.dp
        val spacing = if (columnCount == 1) 6.dp else 12.dp
        val gridState = rememberLazyGridState()

        WithVerticalScrollbar(gridState, Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                state = gridState,
                modifier = Modifier
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
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (device.isOn) {
                                stateHolder.turnOff(listOf(device))
                            } else {
                                stateHolder.turnOn(listOf(device))
                            }
                        },
                        onBrightnessChange = { newBrightness ->
                            stateHolder.turnOn(listOf(device), newBrightness)
                        }
                    )
                }
            }
        }
    }
}