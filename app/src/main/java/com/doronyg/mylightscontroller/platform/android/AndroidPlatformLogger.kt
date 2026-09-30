package com.doronyg.mylightscontroller.platform.android

import android.util.Log
import com.doronyg.mylightscontroller.platform.common.IPlatformLogger

object AndroidPlatformLogger : IPlatformLogger {
    override fun debug(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun info(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun warning(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun error(tag: String, message: String, exception: Throwable?) {
        if (exception != null) {
            Log.e(tag, message, exception)
        } else {
            Log.e(tag, message)
        }
    }
}

