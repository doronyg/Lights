package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.utils.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    onQrScanned: (String) -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val platformScanner = rememberPlatformQrScanner()

    var hasHandledScan by remember { mutableStateOf(false) }

    val stateHolder = remember {
        QrScannerStateHolder(
            scanner = platformScanner.scanner,
            coroutineScope = coroutineScope
        )
    }

    val scannedValue by stateHolder.scannedValue.collectAsState()
    val isScanning by stateHolder.isScanning.collectAsState()
    val errorMessage by stateHolder.errorMessage.collectAsState()
    val permissionGranted by stateHolder.permissionGranted.collectAsState()

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

    // Request permission on screen load
    LaunchedEffect(Unit) {
        Utils.log("QrScanScreen: Requesting camera permission on load")
        stateHolder.requestCameraPermission()
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
                platformScanner.preview(Modifier.fillMaxSize())
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
                    IconButton(onClick = {
                        stateHolder.requestCameraPermission()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Retry"
                        )
                    }
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




















