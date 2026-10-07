package com.doronyg.mylightscontroller.ha

/**
 * Platform-agnostic icon resolver.
 * Resolves device names to HaIcon without any platform-specific dependencies.
 * This API is KMP-safe and works in commonMain.
 *
 * For Compose-specific icon rendering (ImageVector), use the Android extension functions in androidMain:
 * - HaIcon.toImageVector()
 * - HaDevice.getDeviceIcon()
 */
object HaIconResolver {

    private data class IconRule(
        val icon: HaIcon,
        val keywords: List<String>
    )

    private val rules = listOf(

        IconRule(
            HaIcon.BATHTUB,
            listOf("bath")
        ),

        IconRule(
            HaIcon.WC,
            listOf( "washroom", "toilet", "restroom")
        ),

        IconRule(
            HaIcon.BEDROOM,
            listOf("bedroom", "master", "guest", "child", "kids", "nursery")
        ),

        IconRule(
            HaIcon.KITCHEN,
            listOf("kitchen", "cook", "oven", "pantry")
        ),

        IconRule(
            HaIcon.RESTAURANT,
            listOf("dining", "dinner", "breakfast")
        ),

        IconRule(
            HaIcon.LIVING_ROOM,
            listOf("living", "family", "lounge")
        ),

        IconRule(
            HaIcon.BASEMENT,
            listOf("basement", "cellar", "stairs")
        ),

        IconRule(
            HaIcon.PLAYROOM,
            listOf("play", "game", "toy")
        ),

        IconRule(
            HaIcon.PATIO,
            listOf("porch", "patio", "deck", "balcony")
        ),

        IconRule(
            HaIcon.MUDROOM,
            listOf("mud", "entry", "hall", "foyer")
        ),

        IconRule(
            HaIcon.LAUNDRY,
            listOf("laundry", "washer", "dryer")
        ),

        IconRule(
            HaIcon.GARAGE,
            listOf("garage", "carport")
        ),

        IconRule(
            HaIcon.LIGHTBULB,
            listOf("lamp", "light")
        ),

        IconRule(
            HaIcon.POWER,
            listOf("plug", "outlet", "socket")
        ),

        IconRule(
            HaIcon.ROUTER,
            listOf("router", "wifi", "network")
        ),

        IconRule(
            HaIcon.THERMOSTAT,
            listOf("thermostat", "climate", "temperature", "hvac")
        )
    )

    /**
     * Resolves a device to a platform-agnostic HaIcon.
     * This is the primary KMP-safe API.
     *
     * @param device The device to resolve
     * @return HaIcon representing the device type
     */
    fun resolve(device: HaDevice): HaIcon {
        if (device.state == "unavailable") {
            return HaIcon.DEVICES
        }

        val name = normalize(device.englishName)

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

    private fun fallback(device: HaDevice): HaIcon {
        return when (device.type) {
            DeviceType.LIGHT -> HaIcon.LIGHTBULB
            DeviceType.SWITCH -> HaIcon.SWITCH
            DeviceType.OTHER -> HaIcon.DEVICES
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