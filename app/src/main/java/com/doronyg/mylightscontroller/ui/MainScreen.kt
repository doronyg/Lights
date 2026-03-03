package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.ha.HaDevice

// --- Tabs ---
enum class BottomTab(val title: String, val icon: ImageVector) {
    Main("Main", Icons.Default.Lightbulb),
    Unavailable("Unavailable", Icons.Default.PowerOff),
    Settings("Settings", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    viewModel: DevicesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val devices by viewModel.devices.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var selectedTab by remember { mutableStateOf(BottomTab.Main) }

    val contentColor = MaterialTheme.colorScheme.onBackground
    val unavailableCount = devices.count { it.state == "unavailable" }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                BottomTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            if (tab == BottomTab.Unavailable && unavailableCount > 0) {
                                BadgedBox(
                                    badge = { Badge { Text(unavailableCount.toString()) } }
                                ) {
                                    Icon(tab.icon, contentDescription = tab.title)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.title)
                            }
                        },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp)
        ) {

            if (loading) {
                Text(
                    "Loading...",
                    modifier = Modifier.padding(12.dp),
                    color = contentColor
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
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isCompact = maxWidth < 420.dp
        val columnCount = if (isCompact) 2 else 3

        val horizontalPadding = if (isCompact) 4.dp else 8.dp
        val spacing = if (isCompact) 6.dp else 12.dp

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
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