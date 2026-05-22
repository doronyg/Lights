package com.doronyg.mylightscontroller.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.doronyg.mylightscontroller.Config
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.onboarding.models.AppConfig
import com.doronyg.mylightscontroller.onboarding.ui.ManualSetupScreen
import com.doronyg.mylightscontroller.onboarding.ui.OnboardingScreen
import com.doronyg.mylightscontroller.ui.navigator.AppNavigator
import com.doronyg.mylightscontroller.ui.navigator.Screen
import com.doronyg.mylightscontroller.ui.navigator.UiEvent
import com.doronyg.mylightscontroller.ui.navigator.UiEvent.*
import com.doronyg.mylightscontroller.utils.AppTranslator

// --- Tabs ---
enum class BottomTab(val title: String, val icon: ImageVector) {
    Main("Main", Icons.Default.Lightbulb),
    Unavailable("Unavailable", Icons.Default.PowerOff),
    Settings("Settings", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    devicesViewModel: DevicesViewModel = viewModel(),
    translatorViewModel: TranslatorViewModel = viewModel()
) {
    val devices by devicesViewModel.devices.collectAsState()
    val loading by devicesViewModel.loading.collectAsState()
    val translatorReady by translatorViewModel.ready.collectAsState()

    val navigator = remember { AppNavigator() }

    val canGoBack = navigator.stack.size > 1

    BackHandler(enabled = canGoBack) {
        navigator.handle(NavigateBack)
    }

    // initialize root once
    LaunchedEffect(Unit) {
        navigator.setRoot(if (Settings.isConfigured) Screen.Tab(BottomTab.Main) else Screen.Onboarding)
    }

    val current = navigator.current ?: Screen.Tab(BottomTab.Main)

    val showBottomBar = current is Screen.Tab

    val fontScale = LocalConfiguration.current.fontScale
    val showLabels = fontScale < 1.3f

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar && translatorReady) {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        val title = AppTranslator.cachedTranslation(tab.title)
                        NavigationBarItem(
                            selected = current.tab == tab,
                            onClick = {
                                navigator.setRoot(Screen.Tab(tab))
                            },
                            icon = { Icon(tab.icon, contentDescription = title) },
                            label = if (showLabels) {
                                { Text(title, maxLines = 1, softWrap = false) }
                            } else null
                        )
                    }
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

            if (loading || !translatorReady) {
                Text("Loading...")
                return@Scaffold
            }

            when (current) {

                is Screen.Tab -> {
                    when (current.tab) {

                        BottomTab.Main -> {
                            DeviceGrid(devices.filter { !it.unavailable }, devicesViewModel)
                        }

                        BottomTab.Unavailable -> {
                            DeviceGrid(devices.filter { it.unavailable }, devicesViewModel)
                        }

                        BottomTab.Settings -> {
                            SettingsScreen(
                                onEvent = navigator::handle
                            )
                        }
                    }
                }

                Screen.SetupQr -> {
                    SetupQrScreen(
                        config = buildAppConfig(),
                        onClose = {
                            navigator.handle(UiEvent.NavigateBack)
                        }
                    )
                }

                Screen.Onboarding -> {
                    OnboardingScreen(
                        onManualSetup = {
                            navigator.handle(NavigateTo(Screen.ManualSetup))
                        },
                        onSetupComplete = {
                            navigator.handle(Replace(Screen.Tab(BottomTab.Main)))
                        },
                        onClose = {
                            navigator.handle(NavigateBack)
                        }
                    )
                }

                Screen.ManualSetup -> {
                    ManualSetupScreen(
                        onSave = {
                            navigator.handle(SetRoot(Screen.Tab(BottomTab.Main)))
                        },
                        onBack = {
                            navigator.handle(NavigateBack)
                        }
                    )
                }
            }
        }
    }
}

private fun buildAppConfig(): AppConfig {
    return AppConfig(
        homeAssistantUrl = Config.homeAssistantUrl,
        homeAssistantToken = Config.homeAssistantToken,
        porcupineApiKey = Config.porcupineApiKey
    )
}
