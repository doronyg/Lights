package com.doronyg.mylightscontroller

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.doronyg.mylightscontroller.utils.Utils
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object Settings {

    private const val PREF_NAME = "app_settings"

    private val prefs: SharedPreferences?
        get() = Utils.appContext?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // --- Generic Boolean delegate ---
    private fun booleanPref(key: String, default: Boolean = false) =
        object : ReadWriteProperty<Any, Boolean> {
            override fun getValue(thisRef: Any, property: KProperty<*>): Boolean =
                prefs?.getBoolean(key, default) ?: default

            override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
                prefs?.edit { putBoolean(key, value) }
            }
        }

    // --- Settings ---
    var withVoiceControllerService by booleanPref("WITH_VOICE_CONTROLLER_SERVICE_SHARED_PREF")
    var saveMockHaData by booleanPref("SAVE_MOCK_HA_DATA")
    var useMockHaData by booleanPref("USE_MOCK_HA_DATA")
}