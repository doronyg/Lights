package com.doronyg.mylightscontroller.platform.common

/**
 * Platform-agnostic interface for logging.
 * Provides common logging levels (debug, info, warning, error).
 */
interface IPlatformLogger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warning(tag: String, message: String)
    fun error(tag: String, message: String, exception: Throwable? = null)
}

