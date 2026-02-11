package com.doronyg.mylightscontroller.utils

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkUtils {
    object HttpMethod {
        const val GET = "GET"
        const val POST = "POST"
        const val PUT = "PUT"
        const val DELETE = "DELETE"
    }

    // Single OkHttpClient instance for the whole app
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

}