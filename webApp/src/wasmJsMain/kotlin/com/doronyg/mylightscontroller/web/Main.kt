package com.doronyg.mylightscontroller.web

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.ha.HomeAssistantRepository
import com.doronyg.mylightscontroller.onboarding.applyDemoConfig
import com.doronyg.mylightscontroller.platform.web.WebPlatformInitializer
import com.doronyg.mylightscontroller.platform.web.WebPlatformPreferences
import com.doronyg.mylightscontroller.platform.web.WebPlatformUi
import com.doronyg.mylightscontroller.platform.web.isPageVisible
import com.doronyg.mylightscontroller.platform.web.isQueryParamSet
import com.doronyg.mylightscontroller.platform.web.onPageVisibilityChange
import com.doronyg.mylightscontroller.ui.LocalPlatformUi
import com.doronyg.mylightscontroller.ui.MainScreen
import com.doronyg.mylightscontroller.ui.MyTheme
import com.doronyg.mylightscontroller.utils.Utils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    WebPlatformInitializer.initialize()

    // `?demo`: built-in demo devices, no Home Assistant / gateway needed
    if (isQueryParamSet("demo")) {
        WebDemoState.active = true
        MainScope().launch {
            try {
                applyDemoConfig()
            } catch (e: Exception) {
                Utils.logException(e, "Failed to apply demo config")
            }
        }
    } else if (WebDemoState.active) {
        // Demo settings are persisted in localStorage; leave demo mode once `?demo` is dropped
        WebDemoState.active = false
        Settings.useMockHaData = false
        Settings.saveMockHaData = false
    }

    pollHomeAssistantWhileVisible()

    ComposeViewport {
        CompositionLocalProvider(LocalPlatformUi provides WebPlatformUi) {
            MyTheme {
                MainScreen()
            }
        }
    }
}

/** Remembers that the last load was a `?demo` session. */
private object WebDemoState {
    var active by WebPlatformPreferences.booleanPref("WEB_DEMO_ACTIVE", false)
}

/** Same as MainFragment.onResume/onPause: poll HA only while the tab is visible. */
private fun pollHomeAssistantWhileVisible() {
    val repository = HomeAssistantRepository.instance
    var token: Int? = null
    fun update(visible: Boolean) {
        if (visible && token == null) token = repository.registerUi()
        if (!visible) token?.let { repository.unregisterUi(it); token = null }
    }
    update(isPageVisible())
    onPageVisibilityChange(::update)
}






