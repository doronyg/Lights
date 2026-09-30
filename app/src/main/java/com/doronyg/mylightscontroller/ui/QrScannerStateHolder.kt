package com.doronyg.mylightscontroller.ui

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.doronyg.mylightscontroller.platform.PlatformBridge
import com.doronyg.mylightscontroller.platform.common.IPlatformQrScanner
import com.doronyg.mylightscontroller.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * State holder for QR scanner functionality.
 *
 * Manages the lifecycle and state of QR code scanning without using ViewModels.
 * This keeps the scanning logic decoupled from the UI layer and makes it testable.
 */
class QrScannerStateHolder(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner?,
    private val coroutineScope: CoroutineScope,
    private val previewView: PreviewView? = null
) {
    private val scanner: IPlatformQrScanner = PlatformBridge.createQrScanner(
        context,
        lifecycleOwner,
        previewView
    )

    /**
     * The scanned QR code value, or null if not yet scanned.
     */
    val scannedValue: StateFlow<String?> = scanner.scannedValue

    /**
     * Whether the camera is currently active and scanning.
     */
    val isScanning: StateFlow<Boolean> = scanner.isScanning

    /**
     * Error message if something went wrong (e.g., camera permission denied).
     */
    val errorMessage: StateFlow<String?> = scanner.errorMessage

    /**
     * Start scanning for QR codes.
     */
    fun startScanning() {
        Utils.log("QrScannerStateHolder: startScanning() called, previewView=$previewView")
        coroutineScope.launch {
            Utils.log("QrScannerStateHolder: About to call scanner.startScanning()")
            scanner.startScanning()
        }
    }

    /**
     * Stop scanning and release camera resources.
     */
    fun stopScanning() {
        Utils.log("QrScannerStateHolder: stopScanning() called")
        scanner.stopScanning()
    }

    /**
     * Reset scanner state to allow scanning again.
     */
    fun reset() {
        scanner.reset()
    }
}




