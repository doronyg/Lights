package com.doronyg.mylightscontroller.ha

/**
 * Platform-agnostic icon identifier.
 * Can be used in KMP code without Compose dependencies.
 */
enum class HaIcon {
    // Bottom tabs
    MAIN,
    UNAVAILABLE,
    SETTINGS,

    // Room/Location icons
    BATHTUB,
    WC,
    BEDROOM,
    KITCHEN,
    RESTAURANT,
    LIVING_ROOM,
    BASEMENT,
    PLAYROOM,
    PATIO,
    MUDROOM,
    LAUNDRY,
    GARAGE,

    // Device type icons
    LIGHTBULB,
    POWER,
    ROUTER,
    THERMOSTAT,
    SWITCH,

    // Fallback
    DEVICES
}

/**
 * Maps HaIcon to icon metadata.
 * Can be used to get icon name or description for platform-specific rendering.
 */
data class IconMetadata(
    val name: String,
    val description: String
) {
    companion object {
        fun from(icon: HaIcon): IconMetadata = when (icon) {
            // Bottom tabs
            HaIcon.MAIN -> IconMetadata("lightbulb", "Main devices")
            HaIcon.UNAVAILABLE -> IconMetadata("power_off", "Unavailable devices")
            HaIcon.SETTINGS -> IconMetadata("settings", "Settings")

            // Rooms
            HaIcon.BATHTUB -> IconMetadata("bathtub", "Bathroom")
            HaIcon.WC -> IconMetadata("wc", "Restroom")
            HaIcon.BEDROOM -> IconMetadata("bed", "Bedroom")
            HaIcon.KITCHEN -> IconMetadata("kitchen", "Kitchen")
            HaIcon.RESTAURANT -> IconMetadata("restaurant", "Dining room")
            HaIcon.LIVING_ROOM -> IconMetadata("weekend", "Living room")
            HaIcon.BASEMENT -> IconMetadata("basement", "Basement")
            HaIcon.PLAYROOM -> IconMetadata("child_care", "Playroom")
            HaIcon.PATIO -> IconMetadata("deck", "Patio")
            HaIcon.MUDROOM -> IconMetadata("door_front", "Mudroom")
            HaIcon.LAUNDRY -> IconMetadata("laundry", "Laundry")
            HaIcon.GARAGE -> IconMetadata("garage", "Garage")

            // Devices
            HaIcon.LIGHTBULB -> IconMetadata("lightbulb", "Light")
            HaIcon.POWER -> IconMetadata("power", "Power outlet")
            HaIcon.ROUTER -> IconMetadata("router", "Router")
            HaIcon.THERMOSTAT -> IconMetadata("thermostat", "Thermostat")
            HaIcon.SWITCH -> IconMetadata("toggle_on", "Switch")

            // Fallback
            HaIcon.DEVICES -> IconMetadata("devices", "Devices")
        }
    }
}



