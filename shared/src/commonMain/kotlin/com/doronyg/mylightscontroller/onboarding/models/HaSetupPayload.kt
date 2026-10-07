package com.doronyg.mylightscontroller.onboarding.models

import com.doronyg.mylightscontroller.utils.Utils
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url

data class HaSetupPayload(
    val appVersion: Int = 1,
    val haUrl: String,
    val haToken: String,
    val porcupineKey: String? = null
) {
    companion object {
        private const val SCHEME = "mylightscontroller"
        private const val HOST = "setup"

        fun buildSetupDeepLink(payload: HaSetupPayload): String {
            return URLBuilder(
                protocol = URLProtocol(SCHEME, 0),
                host = HOST
            ).apply {
                parameters.append("haUrl", payload.haUrl)
                parameters.append("haToken", payload.haToken)
                parameters.append("porcupineKey", payload.porcupineKey ?: "")
            }.buildString()
        }

        fun parseSetupDeepLinkSafe(url: String): HaSetupPayload? {
            return try {
                val uri = Url(url)

                if (uri.protocol.name != SCHEME || uri.host != HOST) {
                    return null
                }

                val haUrl = uri.parameters["haUrl"] ?: return null
                val haToken = uri.parameters["haToken"] ?: return null
                val porcupineKey = uri.parameters["porcupineKey"] ?: return null

                HaSetupPayload(
                    haUrl = haUrl,
                    haToken = haToken,
                    porcupineKey = porcupineKey
                )
            } catch (e: Exception) {
                Utils.logException(e)
                null
            }
        }
    }
}



