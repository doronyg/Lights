package com.doronyg.mylightscontroller.utils

import kotlinx.serialization.json.Json

object JsonUtils {
    // Shared Json configuration for all serialization needs
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }
}
