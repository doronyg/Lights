package com.doronyg.mylightscontroller.ui.Navigator

import com.doronyg.mylightscontroller.ui.BottomTab

sealed class Screen {

    data class Tab(val tab: BottomTab) : Screen()

    data object SetupQr : Screen()

}