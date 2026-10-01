package com.doronyg.mylightscontroller.platform.android

import com.doronyg.mylightscontroller.platform.common.IPlatformDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Android implementation of platform dispatchers.
 * Uses standard Android coroutine dispatchers.
 */
class AndroidPlatformDispatchers : IPlatformDispatchers {
    override fun uiDispatcher(): CoroutineDispatcher = Dispatchers.Main

    override fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    override fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

