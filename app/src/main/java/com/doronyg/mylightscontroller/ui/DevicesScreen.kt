package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DevicesScreen(
    viewModel: DevicesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val devices by viewModel.devices.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (loading) {
            Text(
                "Loading...",
                modifier = Modifier.padding(12.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(
                items = devices,
                key = { it.entityId },
                span = { device ->
                    if (device.dimmable && device.isOn)
                        GridItemSpan(2)   // Full width
                    else
                        GridItemSpan(1)   // Half width
                }
            ) { device ->

                DeviceRow(
                    device = device,
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