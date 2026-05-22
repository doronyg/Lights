package com.doronyg.mylightscontroller.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaType

object Utils {
    const val APPLICATION_JSON = "application/json"
    val contentType = APPLICATION_JSON.toMediaType()

    var appContext: Context? = null
        set(value) {
            if (field == null) {
                field = value
                value?.let {
                    handler = Handler(it.mainLooper)
                }
            }
        }

    var handler : Handler? = null

    fun log(string: String) {
        Log.i("LightsLog", string)
    }

    fun logException(e: Throwable, message : String? = null) {
        log("Log exception: ${message ?: ""} - ${e.javaClass.canonicalName} - ${e.message}")
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

    fun checkAppPermissions(context: Context): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

}