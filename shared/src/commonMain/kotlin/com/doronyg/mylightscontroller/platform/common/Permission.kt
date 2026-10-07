package com.doronyg.mylightscontroller.platform.common

/**
 * Platform-independent permission enum.
 * Each platform (Android, WASM, etc.) maps these to its own permission strings.
 */
enum class Permission {
    CAMERA,
    MICROPHONE,
    LOCATION
}

