package com.doronyg.mylightscontroller.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import androidx.core.content.ContextCompat
import com.doronyg.mylightscontroller.platform.PlatformBridge

object Utils {

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
        PlatformBridge.logger.info("LightsLog", string)
    }

    fun logException(e: Throwable, message : String? = null) {
        PlatformBridge.logger.error("LightsLog", "Log exception: ${message ?: ""} - ${e.javaClass.canonicalName} - ${e.message}", e)
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