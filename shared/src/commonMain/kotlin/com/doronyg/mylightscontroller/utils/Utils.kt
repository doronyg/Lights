package com.doronyg.mylightscontroller.utils

import com.doronyg.mylightscontroller.platform.common.Permission
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Multiplatform utilities.
 * Platform-independent logging, permissions, and UI dispatch.
 */
object Utils {

    fun log(string: String) {
        PlatformBridge.logger.info("LightsLog", string)
    }

    fun logException(e: Throwable, message: String? = null) {
        PlatformBridge.logger.error("LightsLog", "Log exception: ${message ?: ""} - ${e::class.simpleName} - ${e.message}", e)
    }

    /**
     * Post an action to the UI thread with optional delay.
     * Uses PlatformBridge dispatchers (works on all platforms).
     */
    fun post(action: () -> Unit, delay: Long? = null) {
        CoroutineScope(PlatformBridge.dispatchers.uiDispatcher()).launch {
            delay?.let { delay(it.milliseconds) }
            action()
        }
    }

    /**
     * Check if microphone permission is granted via the platform bridge.
     * Uses Permission.MICROPHONE enum (platform-independent).
     */
    suspend fun checkAppPermissions(): Boolean {
        return PlatformBridge.permissions.hasPermission(Permission.MICROPHONE)
    }

    /**
     * Request microphone permission via the platform bridge.
     * Uses Permission.MICROPHONE enum (platform-independent).
     * Permission result will be delivered via PlatformBridge.permissionResults.
     */
    fun requestAppPermissions() {
        PlatformBridge.permissions.requestPermission(Permission.MICROPHONE)
    }

}