package com.doronyg.mylightscontroller.ui.navigator

import com.doronyg.mylightscontroller.ui.BottomTab

sealed class Screen {

    data class Tab(val tab: BottomTab) : Screen()

    data object SetupQr : Screen()

    data object Onboarding : Screen()

    data object ManualSetup : Screen()
}