package com.doronyg.mylightscontroller.utils

import android.util.Base64

object Base64Codec : StringCodec {

    override fun encode(value: String): String {
        return Base64.encodeToString(
            value.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP
        )
    }

    override fun decode(value: String): String {
        return try {
            String(
                Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP),
                Charsets.UTF_8
            )
        } catch (e: Exception) {
            Utils.logException(e, "Base64Codec failed")
            value // fallback (or throw, but safer for prefs)
        }
    }
}