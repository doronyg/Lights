package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.Config
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.onboarding.models.AppConfig
import com.doronyg.mylightscontroller.onboarding.models.HaSetupPayload.Companion.parseSetupDeepLinkSafe
import com.doronyg.mylightscontroller.ui.navigator.AppNavigator
import com.doronyg.mylightscontroller.ui.navigator.Screen
import com.doronyg.mylightscontroller.ui.navigator.Screen.Tab
import com.doronyg.mylightscontroller.ui.navigator.UiEvent.NavigateBack
import com.doronyg.mylightscontroller.ui.navigator.UiEvent.NavigateTo
import com.doronyg.mylightscontroller.ui.navigator.UiEvent.Replace
import com.doronyg.mylightscontroller.ui.navigator.UiEvent.SetRoot
import com.doronyg.mylightscontroller.ui.onboarding.ManualSetupScreen
import com.doronyg.mylightscontroller.ui.onboarding.OnboardingScreen
import com.doronyg.mylightscontroller.utils.AppTranslator


@Composable
fun MainScreen(
    stateHolder: DevicesStateHolder = remember { DevicesStateHolder() },
    translatorStateHolder: TranslatorStateHolder = remember { TranslatorStateHolder() }
) {
    val devices by stateHolder.devices.collectAsState()
    val loading by stateHolder.loading.collectAsState()
    val translatorReady by translatorStateHolder.ready.collectAsState()

    val navigator = remember { AppNavigator() }

    // Cleanup listener when screen exits
    DisposableEffect(Unit) {
        onDispose {
            stateHolder.cleanup()
        }
    }

    val stackList by navigator.stack.collectAsState()
    val canGoBack = stackList.size > 1

    PlatformBackHandler(enabled = canGoBack) {
        navigator.handle(NavigateBack)
    }

    // initialize root once
    LaunchedEffect(Unit) {
        navigator.setRoot(if (Settings.isConfigured) Screen.Tab(BottomTab.Main) else Screen.Onboarding)
    }

    val current = navigator.current ?: Screen.Tab(BottomTab.Main)

    val showBottomBar = current is Screen.Tab

    val fontScale = LocalDensity.current.fontScale
    val showLabels = fontScale < 1.3f

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar && translatorReady) {
                NavigationBar (windowInsets = WindowInsets()) {
                    BottomTab.entries.forEach { tab ->
                        val title = AppTranslator.cachedTranslation(tab.title)
                        NavigationBarItem(
                            selected = current.tab == tab,
                            onClick = {
                                navigator.setRoot(Screen.Tab(tab))
                            },
                            icon = { Icon(tab.getIcon(), contentDescription = title) },
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

            if (!translatorReady || (loading && devices.isEmpty())) {
                Text("Loading...")
                return@Scaffold
            }

            when (current) {

                is Screen.Tab -> {
                    when (current.tab) {

                        BottomTab.Main -> {
                            DeviceGrid(devices.filter { !it.unavailable }, stateHolder)
                        }

                        BottomTab.Unavailable -> {
                            DeviceGrid(devices.filter { it.unavailable }, stateHolder)
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
                            navigator.handle(NavigateBack)
                        }
                    )
                }

                Screen.Onboarding -> {
                    OnboardingScreen(
                        onManualSetup = {
                            navigator.handle(NavigateTo(Screen.ManualSetup))
                        },
                        onSetupComplete = {
                            navigator.handle(Replace(Tab(BottomTab.Main)))
                        },
                        onClose = {
                            navigator.handle(NavigateBack)
                        },
                        onQrScan = {
                            navigator.handle(NavigateTo(Screen.QrScan))
                        }
                    )
                }

                Screen.ManualSetup -> {
                    ManualSetupScreen(
                        onSave = {
                            navigator.handle(SetRoot(Tab(BottomTab.Main)))
                        },
                        onBack = {
                            navigator.handle(NavigateBack)
                        }
                    )
                }

                Screen.QrScan -> {
                    QrScanScreen(
                        onQrScanned = { scannedConfig ->
                            parseSetupDeepLinkSafe(scannedConfig)?.apply {
                                Config.homeAssistantUrl = haUrl
                                Config.homeAssistantToken = haToken
                                porcupineKey?.let { porcupineKey ->
                                    Config.porcupineApiKey = porcupineKey
                                }
                                Settings.isConfigured = true
                            }
                            navigator.handle(SetRoot(Tab(BottomTab.Main)))
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
