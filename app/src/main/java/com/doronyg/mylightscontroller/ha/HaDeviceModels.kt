package com.doronyg.mylightscontroller.ha

import com.google.gson.annotations.SerializedName

enum class DeviceType { LIGHT, SWITCH, OTHER }

data class HaAttributes(
    @SerializedName("friendly_name") val friendlyName: String?,
    @SerializedName("area_id") val areaId: String?,
    @SerializedName("supported_features") val supportedFeatures: Int = 0,
    @SerializedName("supported_color_modes") val supportedColorModes: List<String>? = null,
    @SerializedName("brightness") val brightness: Int? = null
) {
    val brightnessPct: Int?
        get() = brightness?.let { it * 100 / 255 } // 0..100%
}

data class HaDevice(
    @SerializedName("entity_id") val entityId: String,
    @SerializedName("state") val state: String,
    @SerializedName("attributes") val attributes: HaAttributes,

) {
    val type: DeviceType
        get() = when {
            entityId.startsWith("light.") -> DeviceType.LIGHT
            entityId.startsWith("switch.") -> DeviceType.SWITCH
            else -> DeviceType.OTHER
        }

    val name: String
        get() = attributes.friendlyName ?: entityId

    val area: String?
        get() = attributes.areaId

    val dimmable: Boolean
        get() {
            // Modern HA (preferred)
            attributes.supportedColorModes?.let { modes ->
                return modes.any { it != "onoff" }
            }
            // Fallback
            return attributes.brightness != null
        }

    val isOn: Boolean
        get() = state == "on"
}


data class HaLightCommand(
    @SerializedName("entity_id") val entityId: List<String>,
    @SerializedName("brightness_pct") val brightnessPct: Int? = null
)
