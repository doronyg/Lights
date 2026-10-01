package com.doronyg.mylightscontroller.platform.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-agnostic bridge layer.
 *
 * Provides access to platform-specific implementations through common interfaces.
 * Platform implementations must initialize these fields via AndroidPlatformInitializer.initialize()
 * before any platform services are used.
 *
 * For Kotlin Multiplatform migration: these can become expect/actual declarations.
 */
object PlatformBridge {
    // Initialized by platform-specific code during app startup
    lateinit var preferences: IPlatformPreferences
    lateinit var translator: IPlatformTranslator
    lateinit var voiceController: IPlatformVoiceController
    lateinit var defaultConfig: IPlatformDefaultConfig
    lateinit var logger: IPlatformLogger
    lateinit var permissions: IPlatformPermissions

    /**
     * Permission results map - tracks all requested permissions and their grant status.
     * Key: platform-independent Permission enum
     * Value: true if granted, false if denied
     * UI can observe this StateFlow to be notified of permission changes.
     * This is completely platform-independent and lives in the bridge layer.
     */
    private val _permissionResults = MutableStateFlow<Map<Permission, Boolean>>(emptyMap())
    val permissionResults: StateFlow<Map<Permission, Boolean>> = _permissionResults

    /**
     * Called by the activity when a permission request result is received.
     * Updates the permission results map with the platform-independent Permission enum.
     */
    fun onPermissionResult(permission: Permission, granted: Boolean) {
        val currentResults = _permissionResults.value.toMutableMap()
        currentResults[permission] = granted
        _permissionResults.value = currentResults
    }
}