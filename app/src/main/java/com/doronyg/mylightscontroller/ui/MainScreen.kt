package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.ha.HaDevice
import androidx.compose.foundation.lazy.grid.items

// --- Tabs ---
sealed class BottomTab(val title: String) {
    object Main : BottomTab("Main")
    object Unavailable : BottomTab("Unavailable")
    object Settings : BottomTab("Settings")
}

@Composable
fun MainScreen(
    viewModel: DevicesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val devices by viewModel.devices.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var selectedTab by remember { mutableStateOf<BottomTab>(BottomTab.Main) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                // Main tab
                NavigationBarItem(
                    selected = selectedTab == BottomTab.Main,
                    onClick = { selectedTab = BottomTab.Main },
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Main") },
                    label = { Text("Main") }
                )

                // Unavailable tab with badge
                val unavailableCount = devices.count { it.state == "unavailable" }
                NavigationBarItem(
                    selected = selectedTab == BottomTab.Unavailable,
                    onClick = { selectedTab = BottomTab.Unavailable },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unavailableCount > 0) {
                                    Badge { Text(unavailableCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.PowerOff, contentDescription = "Unavailable")
                        }
                    },
                    label = { Text("Unavailable") }
                )

                // Settings tab
                NavigationBarItem(
                    selected = selectedTab == BottomTab.Settings,
                    onClick = { selectedTab = BottomTab.Settings },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            if (loading) {
                Text(
                    "Loading...",
                    modifier = Modifier.padding(12.dp)
                )
            }

            when (selectedTab) {
                BottomTab.Main -> {
                    val availableDevices = devices.filter { it.state != "unavailable" }
                    DeviceGrid(availableDevices, viewModel)
                }
                BottomTab.Unavailable -> {
                    val unavailableDevices = devices.filter { it.state == "unavailable" }
                    DeviceGrid(unavailableDevices, viewModel)
                }
                BottomTab.Settings -> {
                    SettingsScreen()
                }
            }
        }
    }
}

@Composable
fun DeviceGrid(
    devices: List<HaDevice>,
    viewModel: DevicesViewModel
) {
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
                    GridItemSpan(2) // full width
                else
                    GridItemSpan(1) // half width
            }
        ) { device ->
            DeviceRow(
                device = device,
                modifier = Modifier.fillMaxWidth(),
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