package com.doronyg.mylightscontroller.platform.android

import android.content.SharedPreferences
import androidx.core.content.edit
import com.doronyg.mylightscontroller.platform.common.IPlatformPreferences
import com.doronyg.mylightscontroller.utils.StringCodec
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Android implementation of platform-specific preference storage.
 * Uses Android SharedPreferences for persistent key-value storage.
 *
 * Future: This will become the `actual` implementation when converting to Kotlin Multiplatform.
 */
object AndroidPlatformPreferences : IPlatformPreferences {

    private const val APP_PREF_NAME = "app_settings"
    private const val TRANSLATION_PREF_NAME = "translations"

    private val appPrefs: SharedPreferences?
        get() = AndroidUtils.appContext?.getSharedPreferences(APP_PREF_NAME, android.content.Context.MODE_PRIVATE)

    private val translationPrefs: SharedPreferences?
        get() = AndroidUtils.appContext?.getSharedPreferences(TRANSLATION_PREF_NAME, android.content.Context.MODE_PRIVATE)

    /**
     * Get SharedPreferences for app settings.
     * Used internally by preference delegates.
     */
    internal fun getAppPreferences(): SharedPreferences? = appPrefs

    /**
     * Get SharedPreferences for translations.
     * Can be used by other platform-specific components.
     */
    internal fun getTranslationPreferences(): SharedPreferences? = translationPrefs

    override fun booleanPref(key: String, default: Boolean) =
        object : ReadWriteProperty<Any, Boolean> {
            override fun getValue(thisRef: Any, property: KProperty<*>): Boolean =
                appPrefs?.getBoolean(key, default) ?: default

            override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
                appPrefs?.edit { putBoolean(key, value) }
            }
        }

    override fun intPref(key: String, default: Int) =
        object : ReadWriteProperty<Any, Int> {
            override fun getValue(thisRef: Any, property: KProperty<*>): Int =
                appPrefs?.getInt(key, default) ?: default

            override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
                appPrefs?.edit { putInt(key, value) }
            }
        }

    override fun stringPref(
        key: String,
        default: String,
        codec: StringCodec
    ) = object : ReadWriteProperty<Any, String> {

        override fun getValue(thisRef: Any, property: KProperty<*>): String {
            val raw = appPrefs?.getString(key, null) ?: return default
            return runCatching { codec.decode(raw) }.getOrDefault(default)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
            val encoded = runCatching { codec.encode(value) }
                .getOrDefault(value)

            appPrefs?.edit { putString(key, encoded) }
        }
    }
}


