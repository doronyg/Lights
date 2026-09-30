package com.doronyg.mylightscontroller.platform.common

import kotlinx.coroutines.flow.StateFlow

interface IPlatformQrScanner {
    /**
     * Emits when a QR code is successfully scanned.
     * Null value means no scan yet, empty string means scan failed.
     */
    val scannedValue: StateFlow<String?>

    /**
     * Whether camera is currently active and scanning
     */
    val isScanning: StateFlow<Boolean>

    /**
     * Error message if scanning failed (permission denied, camera unavailable, etc.)
     */
    val errorMessage: StateFlow<String?>

    /**
     * Start scanning for QR codes. Platform-specific implementation handles permissions.
     */
    suspend fun startScanning()

    /**
     * Stop scanning and release camera resources
     */
    fun stopScanning()

    /**
     * Reset the scanner state
     */
    fun reset()
}

