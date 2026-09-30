package com.doronyg.mylightscontroller.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.doronyg.mylightscontroller.platform.PlatformBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

object Utils {

    var appContext: Context? = null
        set(value) {
            if (field == null) {
                field = value
                mainScope = MainScope()
            }
        }

    var mainScope: CoroutineScope? = null

    fun log(string: String) {
        PlatformBridge.logger.info("LightsLog", string)
    }

    fun logException(e: Throwable, message: String? = null) {
        PlatformBridge.logger.error("LightsLog", "Log exception: ${message ?: ""} - ${e.javaClass.canonicalName} - ${e.message}", e)
    }

    fun post(action: () -> Unit, delay: Long? = null) {
        mainScope?.launch(Dispatchers.Main) {
            delay?.let { delay(it.milliseconds) }
            action()
        }
    }

    fun checkAppPermissions(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

}