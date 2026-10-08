package com.doronyg.mylightscontroller.ui

import androidx.compose.ui.graphics.vector.ImageVector
import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HaIcon
import com.doronyg.mylightscontroller.ha.HaIconResolver
import com.doronyg.mylightscontroller.ui.icons.Basement
import com.doronyg.mylightscontroller.ui.icons.Bathtub
import com.doronyg.mylightscontroller.ui.icons.Bed
import com.doronyg.mylightscontroller.ui.icons.ChildCare
import com.doronyg.mylightscontroller.ui.icons.Deck
import com.doronyg.mylightscontroller.ui.icons.Devices
import com.doronyg.mylightscontroller.ui.icons.DoorFront
import com.doronyg.mylightscontroller.ui.icons.Garage
import com.doronyg.mylightscontroller.ui.icons.Kitchen
import com.doronyg.mylightscontroller.ui.icons.Lightbulb
import com.doronyg.mylightscontroller.ui.icons.LocalLaundryService
import com.doronyg.mylightscontroller.ui.icons.Power
import com.doronyg.mylightscontroller.ui.icons.PowerSettingsNew
import com.doronyg.mylightscontroller.ui.icons.Restaurant
import com.doronyg.mylightscontroller.ui.icons.Router
import com.doronyg.mylightscontroller.ui.icons.Settings
import com.doronyg.mylightscontroller.ui.icons.Thermostat
import com.doronyg.mylightscontroller.ui.icons.ToggleOn
import com.doronyg.mylightscontroller.ui.icons.Wc
import com.doronyg.mylightscontroller.ui.icons.Weekend

/**
 * Android-specific icon resolver extension.
 * Converts KMP-safe HaIcon to Compose ImageVector.
 * This is Android-only and should only be used within the UI package in @Composable functions.
 *
 * For KMP code that doesn't need Compose rendering, use:
 * - HaIconResolver.resolve(device) -> HaIcon
 * - IconMetadata.from(icon) -> metadata
 *
 * This pattern allows creating equivalent implementations for other platforms (Web/WASM, iOS, etc.)
 */
object HaIconResolverAndroid {

    /**
     * Converts a platform-agnostic HaIcon to a Compose ImageVector.
     * Android-only: Do not use in KMP code.
     *
     * @param icon The HaIcon to convert
     * @return ImageVector for rendering in Compose
     */
    fun getComposableIcon(icon: HaIcon): ImageVector {
        return when (icon) {
            // Bottom tabs
            HaIcon.MAIN -> Lightbulb
            HaIcon.UNAVAILABLE -> PowerSettingsNew
            HaIcon.SETTINGS -> Settings

            // Rooms
            HaIcon.BATHTUB -> Bathtub
            HaIcon.WC -> Wc
            HaIcon.BEDROOM -> Bed
            HaIcon.KITCHEN -> Kitchen
            HaIcon.RESTAURANT -> Restaurant
            HaIcon.LIVING_ROOM -> Weekend
            HaIcon.BASEMENT -> Basement
            HaIcon.PLAYROOM -> ChildCare
            HaIcon.PATIO -> Deck
            HaIcon.MUDROOM -> DoorFront
            HaIcon.LAUNDRY -> LocalLaundryService
            HaIcon.GARAGE -> Garage

            // Devices
            HaIcon.LIGHTBULB -> Lightbulb
            HaIcon.POWER -> Power
            HaIcon.ROUTER -> Router
            HaIcon.THERMOSTAT -> Thermostat
            HaIcon.SWITCH -> ToggleOn

            // Fallback
            HaIcon.DEVICES -> Devices
        }
    }
}

/**
 * Extension function for easy access in Compose code.
 * Example: icon.toImageVector()
 * Android-only implementation - other platforms will have their own.
 */
fun HaIcon.toImageVector(): ImageVector = HaIconResolverAndroid.getComposableIcon(this)

/**
 * Convenience function for devices.
 * Example: device.getDeviceIcon()
 * Android-only implementation - other platforms will have their own.
 */
fun HaDevice.getDeviceIcon(): ImageVector = HaIconResolverAndroid.getComposableIcon(HaIconResolver.resolve(this))

