package com.doronyg.mylightscontroller.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.doronyg.mylightscontroller.utils.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    onQrScanned: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var permissionGranted by remember { mutableStateOf(false) }
    val previewView = remember { PreviewView(context) }
    var hasHandledScan by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Utils.log("QrScanScreen: Camera permission result: $isGranted")
        permissionGranted = isGranted
    }

    val stateHolder = remember {
        QrScannerStateHolder(
            context = context,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            previewView = previewView
        )
    }

    val scannedValue by stateHolder.scannedValue.collectAsState()
    val isScanning by stateHolder.isScanning.collectAsState()
    val errorMessage by stateHolder.errorMessage.collectAsState()

    // Debug: Log state changes
    LaunchedEffect(permissionGranted) {
        Utils.log("QrScanScreen: permissionGranted=$permissionGranted")
    }

    LaunchedEffect(isScanning) {
        Utils.log("QrScanScreen: isScanning=$isScanning")
    }

    LaunchedEffect(scannedValue) {
        Utils.log("QrScanScreen: scannedValue=$scannedValue")
    }

    // Request permission and start scanning when the screen is first displayed
    LaunchedEffect(Unit) {
        Utils.log("QrScanScreen: Launching camera permission request")
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Start scanning once permission is granted
    LaunchedEffect(permissionGranted) {
        if (permissionGranted && !isScanning && !hasHandledScan) {
            Utils.log("QrScanScreen: Starting camera scanning")
            stateHolder.startScanning()
        } else if (permissionGranted) {
            Utils.log("QrScanScreen: Not starting - isScanning=$isScanning, hasHandledScan=$hasHandledScan")
        }
    }

    // Stop scanning and cleanup when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            stateHolder.stopScanning()
        }
    }

    // Handle scanned value - only once and with validation
    LaunchedEffect(scannedValue) {
        if (!hasHandledScan && scannedValue != null && scannedValue!!.isNotBlank()) {
            Utils.log("QrScanScreen: Calling onQrScanned with value: $scannedValue")
            hasHandledScan = true
            onQrScanned(scannedValue!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Setup QR Code") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Camera preview
            if (permissionGranted) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Overlay with status/error messages
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (errorMessage != null) {
                    // Show error state
                    Text(
                        text = "⚠️ Error",
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = errorMessage ?: "Unknown error",
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Please check that:\n• Camera permission is granted\n• Camera is available\n• Try again",
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                } else if (isScanning && permissionGranted) {
                    // Show scanning state with minimal overlay
                    // (don't block camera view too much)
                } else if (!permissionGranted) {
                    // Show waiting for permission
                    Text(
                        text = "Requesting camera permission...",
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Ready to scan
                    Text(
                        text = "Ready to scan",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}




















