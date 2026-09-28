package com.doronyg.mylightscontroller.ha

import com.doronyg.mylightscontroller.utils.AppTranslator
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class DeviceType { LIGHT, SWITCH, OTHER }

@Serializable
data class HaAttributes(
    @SerialName("friendly_name") val friendlyName: String? = null,
    @SerialName("area_id") val areaId: String? = null,
    @SerialName("supported_features") val supportedFeatures: Int = 0,
    @SerialName("supported_color_modes") val supportedColorModes: List<String>? = null,
    @SerialName("brightness") val brightness: Int? = null,
    @SerialName("color_mode") val colorMode: String? = null,
    @SerialName("rgb_color") val rgbColor: List<Int>? = null,
    @SerialName("hs_color") val hsColor: List<Float>? = null,
    @SerialName("xy_color") val xyColor: List<Float>? = null,
    @SerialName("color_temp") val colorTemp: Int? = null,
    @SerialName("color_temp_kelvin") val colorTempKelvin: Int? = null
) {
    val brightnessPct: Int?
        get() = brightness?.let { it * 100 / 255 } // 0..100%
}

@Serializable
data class HaDevice(
    @SerialName("entity_id") val entityId: String,
    @SerialName("state") val state: String,
    @SerialName("attributes") val attributes: HaAttributes,

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


@Serializable
data class HaLightCommand(
    @SerialName("entity_id") val entityId: List<String>,
    @SerialName("brightness_pct") val brightnessPct: Int? = null
)
