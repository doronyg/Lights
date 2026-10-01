package com.doronyg.mylightscontroller.ui

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.doronyg.mylightscontroller.platform.android.AndroidPlatformQrScanner
import com.doronyg.mylightscontroller.platform.common.IPlatformQrScanner
import com.doronyg.mylightscontroller.platform.common.Permission
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * State holder for QR scanner functionality.
 *
 * Manages the lifecycle and state of QR code scanning without using ViewModels.
 * Handles camera permissions through the platform bridge, making it KMP-ready.
 * 
 * Note: Creates scanner directly (AndroidPlatformQrScanner). When converting to KMP,
 * this will use expect/actual createQrScanner() function per platform.
 */
class QrScannerStateHolder(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner?,
    private val coroutineScope: CoroutineScope,
    private val previewView: PreviewView? = null
) {
    // Create scanner directly - Android-specific but will become expect/actual in KMP
    private val scanner: IPlatformQrScanner = AndroidPlatformQrScanner(
        context,
        lifecycleOwner,
        previewView
    )

    private val _permissionGranted = MutableStateFlow(false)

    /**
     * Whether camera permission has been granted.
     */
    val permissionGranted: StateFlow<Boolean> = _permissionGranted

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

    init {
        // Observe permission results from the bridge and update local state
        coroutineScope.launch {
            PlatformBridge.permissionResults.collect { results ->
                val cameraPermissionGranted = results[Permission.CAMERA] ?: false
                if (_permissionGranted.value != cameraPermissionGranted) {
                    Utils.log("QrScannerStateHolder: Camera permission changed to $cameraPermissionGranted")
                    _permissionGranted.value = cameraPermissionGranted

                    // Automatically start scanning when permission is granted
                    if (cameraPermissionGranted) {
                        Utils.log("QrScannerStateHolder: Permission granted, starting scanning")
                        startScanning()
                    }
                }
            }
        }
    }

    /**
     * Request camera permission from the platform.
     * Permission result will be delivered via PlatformBridge.permissionResults StateFlow.
     * Call this when the user needs to grant camera permission.
     */
    fun requestCameraPermission() {
        Utils.log("QrScannerStateHolder: Requesting camera permission")
        PlatformBridge.permissions.requestPermission(Permission.CAMERA)
        // Permission result will arrive asynchronously via PlatformBridge.permissionResults StateFlow
    }

    /**
     * Start scanning for QR codes.
     */
    fun startScanning() {
        Utils.log("QrScannerStateHolder: startScanning() called, isPermissionGranted=${_permissionGranted.value}")
        if (!_permissionGranted.value) {
            Utils.log("QrScannerStateHolder: Cannot start scanning - permission not granted")
            return
        }
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
