package com.doronyg.mylightscontroller.utils

import kotlin.io.encoding.Base64

object Base64Codec : StringCodec {

    override fun encode(value: String): String {
        return Base64.UrlSafe.encode(value.encodeToByteArray())
    }

    override fun decode(value: String): String {
        return try {
            Base64.UrlSafe.decode(value).decodeToString()
        } catch (e: Exception) {
            Utils.logException(e, "Base64Codec failed")
            value // fallback (or throw, but safer for prefs)
        }
    }
}