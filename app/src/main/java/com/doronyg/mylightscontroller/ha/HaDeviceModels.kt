package com.doronyg.mylightscontroller.ha

import com.doronyg.mylightscontroller.utils.AppTranslator
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
    suspend fun translate() {
        translatedName = attributes.friendlyName?.let { AppTranslator.translate(it ) }
    }

    val type: DeviceType
        get() = when {
            entityId.startsWith("light.") -> DeviceType.LIGHT
            entityId.startsWith("switch.") -> DeviceType.SWITCH
            else -> DeviceType.OTHER
        }

    val name: String
        get() = translatedName ?: attributes.friendlyName ?: entityId

    val englishName: String
        get() = attributes.friendlyName ?: entityId

    var translatedName : String? = null

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

    val unavailable : Boolean
        get() = state == "unavailable"
}


data class HaLightCommand(
    @SerializedName("entity_id") val entityId: List<String>,
    @SerializedName("brightness_pct") val brightnessPct: Int? = null
)
