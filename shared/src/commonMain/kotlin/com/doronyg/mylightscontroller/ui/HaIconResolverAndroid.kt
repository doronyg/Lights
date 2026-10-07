package com.doronyg.mylightscontroller.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Deck
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.ui.graphics.vector.ImageVector
import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HaIcon
import com.doronyg.mylightscontroller.ha.HaIconResolver
import com.doronyg.mylightscontroller.ui.icons.Basement

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
            HaIcon.MAIN -> Icons.Default.Lightbulb
            HaIcon.UNAVAILABLE -> Icons.Default.PowerSettingsNew
            HaIcon.SETTINGS -> Icons.Default.Settings

            // Rooms
            HaIcon.BATHTUB -> Icons.Default.Bathtub
            HaIcon.WC -> Icons.Default.Wc
            HaIcon.BEDROOM -> Icons.Default.Bed
            HaIcon.KITCHEN -> Icons.Default.Kitchen
            HaIcon.RESTAURANT -> Icons.Default.Restaurant
            HaIcon.LIVING_ROOM -> Icons.Default.Weekend
            HaIcon.BASEMENT -> Basement
            HaIcon.PLAYROOM -> Icons.Default.ChildCare
            HaIcon.PATIO -> Icons.Default.Deck
            HaIcon.MUDROOM -> Icons.Default.DoorFront
            HaIcon.LAUNDRY -> Icons.Default.LocalLaundryService
            HaIcon.GARAGE -> Icons.Default.Garage

            // Devices
            HaIcon.LIGHTBULB -> Icons.Default.Lightbulb
            HaIcon.POWER -> Icons.Default.Power
            HaIcon.ROUTER -> Icons.Default.Router
            HaIcon.THERMOSTAT -> Icons.Default.Thermostat
            HaIcon.SWITCH -> Icons.Default.ToggleOn

            // Fallback
            HaIcon.DEVICES -> Icons.Default.Devices
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

