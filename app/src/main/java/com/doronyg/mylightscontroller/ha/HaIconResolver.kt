package com.doronyg.mylightscontroller.ha

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
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.ui.graphics.vector.ImageVector

object HaIconResolver {

    private data class IconRule(
        val icon: ImageVector,
        val keywords: List<String>
    )

    private val rules = listOf(

        IconRule(
            Icons.Default.Bathtub,
            listOf("bath", "washroom", "toilet", "restroom")
        ),

        IconRule(
            Icons.Default.Bed,
            listOf("bedroom", "master", "guest", "child", "kids", "nursery")
        ),

        IconRule(
            Icons.Default.Kitchen,
            listOf("kitchen", "cook", "oven", "pantry")
        ),

        IconRule(
            Icons.Default.Restaurant,
            listOf("dining", "dinner", "breakfast")
        ),

        IconRule(
            Icons.Default.Weekend,
            listOf("living", "family", "lounge")
        ),

        IconRule(
            Icons.Default.Stairs,
            listOf("basement", "cellar", "stairs")
        ),

        IconRule(
            Icons.Default.ChildCare,
            listOf("play", "game", "toy")
        ),

        IconRule(
            Icons.Default.Deck,
            listOf("porch", "patio", "deck", "balcony")
        ),

        IconRule(
            Icons.Default.DoorFront,
            listOf("mud", "entry", "hall", "foyer")
        ),

        IconRule(
            Icons.Default.LocalLaundryService,
            listOf("laundry", "washer", "dryer")
        ),

        IconRule(
            Icons.Default.Garage,
            listOf("garage", "carport")
        ),

        IconRule(
            Icons.Default.Lightbulb,
            listOf("lamp", "light")
        ),

        IconRule(
            Icons.Default.Power,
            listOf("plug", "outlet", "socket")
        ),

        IconRule(
            Icons.Default.Router,
            listOf("router", "wifi", "network")
        ),

        IconRule(
            Icons.Default.Thermostat,
            listOf("thermostat", "climate", "temperature", "hvac")
        )
    )

    fun resolve(device: HaDevice): ImageVector {

        if (device.state == "unavailable") {
            return Icons.Default.Devices
        }

        val name = normalize(device.name)

        val bestMatch = rules
            .map { rule ->
                val score = rule.keywords.count { keyword ->
                    name.contains(keyword)
                }
                rule to score
            }
            .maxByOrNull { it.second }

        return if (bestMatch != null && bestMatch.second > 0) {
            bestMatch.first.icon
        } else {
            fallback(device)
        }
    }

    private fun fallback(device: HaDevice): ImageVector {
        return when (device.type) {
            DeviceType.LIGHT -> Icons.Default.Lightbulb
            DeviceType.SWITCH -> Icons.Default.ToggleOn
            DeviceType.OTHER -> Icons.Default.Devices
        }
    }

    private fun normalize(input: String): String {
        return input
            .lowercase()
            .replace("_", " ")
            .replace("-", " ")
            .trim()
    }
}