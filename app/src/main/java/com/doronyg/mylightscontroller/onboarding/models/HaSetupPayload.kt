package com.doronyg.mylightscontroller.onboarding.models

import android.net.Uri
import androidx.core.net.toUri
import com.doronyg.mylightscontroller.utils.Utils

data class HaSetupPayload(
    val appVersion: Int = 1,
    val haUrl: String,
    val haToken: String,
    val porcupineKey: String? = null
) {
    companion object {
        fun buildSetupDeepLink(payload: HaSetupPayload): String {
            return Uri.Builder()
                .scheme("mylightscontroller")
                .authority("setup")
                .appendQueryParameter("haUrl", payload.haUrl)
                .appendQueryParameter("haToken", payload.haToken)
                .appendQueryParameter("porcupineKey", payload.porcupineKey)
                .build()
                .toString()
        }

        fun parseSetupDeepLinkSafe(url: String): HaSetupPayload? {
            return try {
                val uri = url.toUri()

                if (uri.scheme != "mylightscontroller" || uri.authority != "setup") {
                    return null
                }

                val haUrl = uri.getQueryParameter("haUrl") ?: return null
                val haToken = uri.getQueryParameter("haToken") ?: return null
                val porcupineKey = uri.getQueryParameter("porcupineKey") ?: return null

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



