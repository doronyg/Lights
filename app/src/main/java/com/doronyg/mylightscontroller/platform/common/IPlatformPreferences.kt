package com.doronyg.mylightscontroller.platform.common

import kotlin.properties.ReadWriteProperty

/**
 * Common interface for platform-specific preference storage.
 *
 * This defines the contract that will be implemented per-platform.
 * Future: will become `expect object` when converting to Kotlin Multiplatform.
 */
interface IPlatformPreferences {
    fun booleanPref(key: String, default: Boolean = false): ReadWriteProperty<Any, Boolean>
    fun intPref(key: String, default: Int = 0): ReadWriteProperty<Any, Int>
    fun stringPref(
        key: String,
        default: String = "",
        codec: com.doronyg.mylightscontroller.utils.StringCodec = com.doronyg.mylightscontroller.utils.Base64Codec
    ): ReadWriteProperty<Any, String>
}

