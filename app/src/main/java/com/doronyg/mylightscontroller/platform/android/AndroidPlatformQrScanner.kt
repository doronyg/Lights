package com.doronyg.mylightscontroller.platform.android

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.doronyg.mylightscontroller.platform.common.IPlatformQrScanner
import com.doronyg.mylightscontroller.platform.common.Permission
import com.doronyg.mylightscontroller.utils.Utils
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AndroidPlatformQrScanner(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner? = null,
    private val previewView: PreviewView? = null
) : IPlatformQrScanner {

    private val _scannedValue = MutableStateFlow<String?>(null)
    override val scannedValue: StateFlow<String?> = _scannedValue.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var cameraExecutor: ExecutorService? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val barcodeScanner = BarcodeScanning.getClient()

    init {
        lifecycleOwner?.lifecycle?.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_DESTROY -> stopScanning()
                    else -> {}
                }
            }
        })
    }

    override suspend fun startScanning() {
        Utils.log("AndroidPlatformQrScanner.startScanning() called")
        if (!hasCameraPermission()) {
            _errorMessage.value = "Camera permission not granted"
            Utils.log("AndroidPlatformQrScanner: Camera permission denied")
            return
        }

        if (cameraExecutor == null) {
            cameraExecutor = Executors.newSingleThreadExecutor()
        }

        _isScanning.value = true
        _errorMessage.value = null

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        // Note: The blocking get() happens on the main thread via the listener,
        // which is acceptable for CameraX provider initialization per Android docs.
        @Suppress("BlockingMethodInNonBlockingContext")
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                Utils.log("AndroidPlatformQrScanner: Camera provider obtained, previewView=$previewView")
                setupCameraAndAnalysis()
            } catch (e: Exception) {
                Utils.logException(e, "Failed to get camera provider")
                _errorMessage.value = "Failed to initialize camera: ${e.message}"
                _isScanning.value = false
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun setupCameraAndAnalysis() {
        Utils.log("AndroidPlatformQrScanner.setupCameraAndAnalysis() called, previewView=$previewView")
        val provider = cameraProvider ?: return
        val executor = cameraExecutor ?: return
        val owner = lifecycleOwner ?: return

        try {
            val preview = Preview.Builder().build()

            // Bind preview to the PreviewView if provided
            previewView?.let {
                Utils.log("AndroidPlatformQrScanner: Setting surface provider on previewView")
                preview.setSurfaceProvider(it.surfaceProvider)
            } ?: run {
                Utils.log("AndroidPlatformQrScanner: WARNING - No previewView provided!")
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            analysis.setAnalyzer(executor) { imageProxy ->
                processImage(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            provider.unbindAll()
            Utils.log("AndroidPlatformQrScanner: Binding camera to lifecycle")
            provider.bindToLifecycle(
                owner,
                cameraSelector,
                preview,
                analysis
            )
            Utils.log("AndroidPlatformQrScanner: Camera successfully bound")
        } catch (e: Exception) {
            Utils.logException(e, "Failed to setup camera and analysis")
            _errorMessage.value = "Camera setup failed: ${e.message}"
            _isScanning.value = false
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        if (_scannedValue.value != null) {
            // Already scanned, ignore further frames
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                val value = barcodes.firstOrNull()?.rawValue
                if (!value.isNullOrBlank() && _scannedValue.value == null) {
                    Utils.log("AndroidPlatformQrScanner: QR code detected: $value")
                    _scannedValue.value = value
                }
            }
            .addOnFailureListener { e ->
                Utils.logException(e, "Barcode scanning failed")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    override fun stopScanning() {
        _isScanning.value = false
        cameraProvider?.unbindAll()
        cameraExecutor?.shutdown()
        cameraExecutor = null
        cameraProvider = null
    }

    override fun reset() {
        _scannedValue.value = null
        _errorMessage.value = null
    }

    private fun hasCameraPermission(): Boolean {
        return AndroidPlatformPermissions.isGranted(context, Permission.CAMERA)
    }
}
