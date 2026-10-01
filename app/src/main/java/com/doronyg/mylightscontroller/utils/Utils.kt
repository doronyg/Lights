package com.doronyg.mylightscontroller.utils

import android.content.Context
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.platform.common.Permission
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

    /**
     * Check if microphone permission is granted via the platform bridge.
     * Uses Permission.MICROPHONE enum (platform-independent).
     */
    fun checkAppPermissions(): Boolean {
        return PlatformBridge.permissions.hasPermission(Permission.MICROPHONE)
    }

    /**
     * Request microphone permission via the platform bridge.
     * Uses Permission.MICROPHONE enum (platform-independent).
     * Permission result will be delivered via MainActivity.permissionResult StateFlow.
     */
    fun requestAppPermissions() {
        PlatformBridge.permissions.requestPermission(Permission.MICROPHONE)
    }

}