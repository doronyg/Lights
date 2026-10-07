package com.doronyg.mylightscontroller.commands

sealed class VoiceCommand {
    data class TurnOn(val deviceName: String, val brightness: Int? = null) : VoiceCommand()
    data class TurnOff(val deviceName: String) : VoiceCommand()
}