package com.doronyg.mylightscontroller

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.doronyg.mylightscontroller.utils.Utils

object Settings {

    private const val PREF_NAME = "app_settings"

    const val WITH_VOICE_CONTROLLER_SERVICE_SHARED_PREF =
        "WITH_VOICE_CONTROLLER_SERVICE_SHARED_PREF"

    private val prefs: SharedPreferences?
        get() = Utils.appContext?.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )

    var withVoiceControllerService: Boolean
        get() = prefs?.getBoolean(
            WITH_VOICE_CONTROLLER_SERVICE_SHARED_PREF,
            false   // default value
        ) ?: false
        set(value) {
            prefs?.edit {
                putBoolean(
                    WITH_VOICE_CONTROLLER_SERVICE_SHARED_PREF,
                    value
                )
            }
        }
}