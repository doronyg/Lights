package com.doronyg.mylightscontroller.utils

interface StringCodec {
    fun encode(value: String): String
    fun decode(value: String): String
}