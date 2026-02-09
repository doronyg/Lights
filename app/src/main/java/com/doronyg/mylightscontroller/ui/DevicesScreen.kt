package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    Column {
        if (loading) {
            Text("Loading...", modifier = Modifier.padding(8.dp))
        }

        LazyColumn {
            items(devices) { device ->
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

