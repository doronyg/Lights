package com.doronyg.mylightscontroller.services

import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.app.NotificationCompat
import com.doronyg.mylightscontroller.Config
import com.doronyg.mylightscontroller.R
import com.doronyg.mylightscontroller.commands.VoiceCommand
import com.doronyg.mylightscontroller.ha.DeviceType
import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HomeAssistantApi
import com.doronyg.mylightscontroller.utils.Utils

class VoiceControllerService : Service() {

    companion object {
        private const val TAG = "VoiceControllerService"
        private const val CHANNEL_ID = "voice_service_channel"
        private const val NOTIFICATION_ID = 1
    }

    private var porcupineManager: PorcupineManager? = null
    private var speechRecognizer: SpeechRecognizer? = null
//    private var nluModel : VoiceNluModel? = null
    private var haLights : List<HaDevice> = emptyList()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        initPorcupine()
//        nluModel = VoiceNluModel(applicationContext)
        HomeAssistantApi.fetchAllLights { lights ->
            haLights = lights
            haLights.forEach { Utils.log("light: $it") }
        }
        Utils.log( "VoiceControllerService created")
    }

    override fun onDestroy() {
        super.onDestroy()
        porcupineManager?.stop()
        porcupineManager?.delete()
        stopSpeechRecognizer(false)
        Utils.log("VoiceControllerService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Controller Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Controller Service")
            .setContentText("Listening for hotword...")
            .setSmallIcon(R.mipmap.ic_bee_launcher)
            .setOngoing(true)
            .build()
    }

    private fun initPorcupine() {
        try {
            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(Config.PORCUPINE_API_KEY)
                .setKeyword(Porcupine.BuiltInKeyword.BUMBLEBEE)
                .setSensitivity(0.5f)
                .build(applicationContext) { keywordIndex ->
                    Utils.log( "Hotword detected! Index: $keywordIndex")
                    onHotwordDetected()
                }

            porcupineManager?.start()
            Utils.log( "Porcupine initialized and started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Porcupine: ${e.message}")
        }
    }

    private fun onHotwordDetected() {
        Utils.log( "porcupineManager?.stop()")
        porcupineManager?.stop()
        Utils.post({
            Utils.log( "startListeningForCommand()")
            startListeningForCommand()
        }, 1000)
    }

    private fun startListeningForCommand() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                stopSpeechRecognizer()
            }

            override fun onError(error: Int) {
                Utils.log( "STT error: $error")
                stopSpeechRecognizer()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { processCommand(it) }
                stopSpeechRecognizer()
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer?.setRecognitionListener(recognitionListener)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        speechRecognizer?.startListening(intent)

    }

    private fun stopSpeechRecognizer(restartPorcupine : Boolean = true) {
        Utils.post({
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            if (restartPorcupine) {
                Utils.post({
                    Utils.log("STT stopped and destroyed, ready for next hotword")
                    porcupineManager?.start()
                }, 1000)
            }
        }, 500)
    }

    fun processCommand(command: String) {
        val commandLog = "Got command: $command"
        Utils.log(commandLog)
        Toast.makeText(applicationContext, commandLog, LENGTH_LONG).show()
        // ---- Phase 1: Run TFLite model ----
        val parsed: ParsedVoiceOutput? = CommandParser.parseText(command, haLightsNames())

        // ---- Phase 2: Convert ML output → VoiceCommand ----
        val voiceCommand : VoiceCommand? = when (parsed?.commandType) {
            CommandType.TURN_ON, CommandType.DIM -> {
                parsed.deviceText?.let {
                    VoiceCommand.TurnOn(
                        deviceName = it,
                        brightness = parsed.brightness
                    )
                }
            }

            CommandType.TURN_OFF -> {
                parsed.deviceText?.let {
                    VoiceCommand.TurnOff(it)
                }
            }

            else -> {
                Utils.log("Could not parse command: $command")
                null
            }
        }
        // ---- Phase 3: execute command ----
        voiceCommand?.let { executeVoiceCommand(voiceCommand) }
    }

    fun executeVoiceCommand(command: VoiceCommand) {
        when (command) {
            is VoiceCommand.TurnOn -> {
                val device = findDeviceByName(command.deviceName) ?: return
                HomeAssistantApi.turnOnLights(listOf(device), brightnessPct = command.brightness)
            }

            is VoiceCommand.TurnOff -> {
                val device = findDeviceByName(command.deviceName) ?: return
                if (device.type == DeviceType.LIGHT || device.type == DeviceType.SWITCH) {
                    HomeAssistantApi.turnOffLights(listOf(device)) // use same method for switches
                }
            }
        }
    }

    private fun findDeviceByName(deviceName: String): HaDevice? =
        haLights.find {
            it.attributes.friendlyName.equals(deviceName, true)
        }.also { haDevice -> Utils.log("Found device $haDevice") }

    private fun haLightsNames() =
        haLights.mapNotNull { it.attributes.friendlyName }

}