package com.doronyg.mylightscontroller.platform.common

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Platform-agnostic interface for coroutine dispatchers.
 * Implementations provide platform-specific dispatcher behavior.
 */
interface IPlatformDispatchers {
    /**
     * Dispatcher for UI/main thread operations.
     * Android: Dispatchers.Main
     * WASM: Event loop dispatcher
     */
    fun uiDispatcher(): CoroutineDispatcher

    /**
     * Dispatcher for I/O operations (network, file).
     * Android: Dispatchers.IO
     * WASM: Background/microtask dispatcher
     */
    fun ioDispatcher(): CoroutineDispatcher

    /**
     * Dispatcher for CPU-intensive operations.
     * Android: Dispatchers.Default
     * WASM: Background/microtask dispatcher
     */
    fun defaultDispatcher(): CoroutineDispatcher
}

