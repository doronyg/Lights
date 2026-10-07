package com.doronyg.mylightscontroller.ui

import androidx.compose.ui.graphics.vector.ImageVector
import com.doronyg.mylightscontroller.ha.HaIcon

enum class BottomTab(val title: String, val haIcon: HaIcon) {
    Main("Main", HaIcon.MAIN),
    Unavailable("Unavailable", HaIcon.UNAVAILABLE),
    Settings("Settings", HaIcon.SETTINGS)
}

/**
 * Get the Compose ImageVector for this tab's icon.
 * Extension function for easy use in Compose code.
 * Android-only - uses Material Design icons via HaIcon.toImageVector()
 */
fun BottomTab.getIcon(): ImageVector = haIcon.toImageVector()




