package com.doronyg.mylightscontroller.utils

import android.content.Context
import android.os.Handler
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType

object Utils {
    const val APPLICATION_JSON = "application/json"
    val contentType = APPLICATION_JSON.toMediaType()

    var appContext: Context? = null
        set(value) {
            field = value
            value?.let {
                handler = Handler(it.mainLooper)
            }
        }

    var handler : Handler? = null

    fun log(string: String) {
        Log.i("VoiceControl", string)
    }

    fun post(runnable: Runnable, delay : Long? = null) {
        handler?.apply {
            if (delay == null) {
                post (runnable)
            } else {
                postDelayed(runnable, delay)
            }
        }
    }
}