package com.doronyg.mylightscontroller.utils

import kotlin.io.encoding.Base64

object Base64Codec : StringCodec {

    override fun encode(value: String): String {
        return Base64.UrlSafe.encode(value.toByteArray(Charsets.UTF_8))
    }

    override fun decode(value: String): String {
        return try {
            String(
                Base64.UrlSafe.decode(value),
                Charsets.UTF_8
            )
        } catch (e: Exception) {
            Utils.logException(e, "Base64Codec failed")
            value // fallback (or throw, but safer for prefs)
        }
    }
}