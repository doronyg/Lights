package com.doronyg.mylightscontroller.utils

import android.content.Context
import android.os.Handler
import android.util.Log
import com.doronyg.mylightscontroller.Config
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object Utils {
    const val APPLICATION_JSON = "application/json"
    val contentType = APPLICATION_JSON.toMediaType()

    var appContext: Context? = null
        set(value) {
            field = value
            value?.let {
                handler = Handler(it.mainLooper)
            }
        }

    var handler : Handler? = null

    object HttpMethod {
        const val GET = "GET"
        const val POST = "POST"
        const val PUT = "PUT"
        const val DELETE = "DELETE"
    }

    fun log(string: String) {
        Log.i("Log", string)
    }

    // Single OkHttpClient instance for the whole app
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // Helper to build a request with HA headers
    fun buildHaRequest(
        endpoint: String,
        method: String = HttpMethod.GET,
        bodyJson: String? = null
    ): Request {
        val builder = Request.Builder()
            .url("${Config.HOME_ASSISTANT_URL}/api/$endpoint")
            .addHeader("Authorization", "Bearer ${Config.HOME_ASSISTANT_TOKEN}")
            .addHeader("Content-Type", APPLICATION_JSON)

        if (method.uppercase() == HttpMethod.POST && bodyJson != null) {
            val requestBody = bodyJson.toRequestBody(contentType)
            builder.post(requestBody)
        }

        return builder.build()
    }


    fun post(runnable: Runnable, delay : Long? = null) {
        handler?.apply {
            if (delay == null) {
                post (runnable)
            } else {
                postDelayed(runnable, delay)
            }
        }
    }
}