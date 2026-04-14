package com.doronyg.mylightscontroller.services

import HomeAssistantRepository
import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineManager
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.doronyg.mylightscontroller.Config
import com.doronyg.mylightscontroller.R
import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.commands.CommandParser
import com.doronyg.mylightscontroller.commands.CommandType
import com.doronyg.mylightscontroller.commands.ParsedVoiceOutput
import com.doronyg.mylightscontroller.commands.VoiceCommand
import com.doronyg.mylightscontroller.ha.DeviceType
import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ui.MainActivity
import com.doronyg.mylightscontroller.utils.Utils

class VoiceControllerService : Service() {

    companion object {
        private const val CHANNEL_ID = "voice_service_channel"
        private const val NOTIFICATION_ID = 1

        private var createdByBoot : Boolean? = null

        fun startService(context: Context, boot: Boolean) {
            if (Utils.checkAppPermissions(context) && Settings.withVoiceControllerService) {
                createdByBoot = this.createdByBoot ?: boot
                val serviceIntent = Intent(context, VoiceControllerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, VoiceControllerService::class.java)
            context.stopService(intent)
        }
    }

    private var porcupineManager: PorcupineManager? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private var haLights : List<HaDevice> = emptyList()

    private var wakeLock : PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        Utils.appContext = applicationContext
        Utils.post({createDependencies()}, if (createdByBoot == true) 30000 else 0)
        Utils.log( "VoiceControllerService created")
    }

    private fun createDependencies() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        initPorcupine()
        acquireWakeLock()

        HomeAssistantRepository.instance.fetchAllLights { lights ->
            haLights = lights
            haLights.forEach { Utils.log("light: $it") }
        }
        Utils.log( "VoiceControllerService createDependencies() done")
    }

    /**
     * Acquires a partial WakeLock to ensure continuous hotword detection while the device is asleep.
     *
     * Even though this service runs in the foreground, many Android devices (especially older ones)
     * will suspend or throttle microphone capture and background threads once the screen has been off
     * for some time. Holding a PARTIAL_WAKE_LOCK keeps the CPU running so that the voice recognition
     * and command processing remain reliable while the phone is locked or in deep sleep.
     *
     * This is necessary for the core use case of the app: always-on, hands-free voice control of
     * Home Assistant devices.
     */
    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "LightsController:Hotword"
            )
            wakeLock?.acquire()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Utils.log("VoiceControllerService onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
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
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Controller Service")
            .setContentText("Listening for hotword...")
            .setSmallIcon(R.mipmap.ic_bee_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun initPorcupine() {
        try {
            porcupineManager = porcupineManager ?: PorcupineManager.Builder()
                .setAccessKey(Config.porcupineApiKey)
                .setKeyword(Porcupine.BuiltInKeyword.BUMBLEBEE)
                .setSensitivity(0.5f)
                .setErrorCallback { error -> Utils.log("PorcupineException: ${error?.message}") }
                .build(applicationContext) { keywordIndex ->
                    Utils.log( "Hotword detected! Index: $keywordIndex")
                    onHotwordDetected()
                }

            porcupineManager?.start()
            Utils.log( "Porcupine initialized and started")
        } catch (e: Exception) {
            Utils.log("Failed to initialize Porcupine: ${e.message}")
        }
    }

    private fun onHotwordDetected() {
        Utils.log( "porcupineManager?.stop()")
        porcupineManager?.stop()
        Utils.post({
            Utils.log( "startListeningForCommand()")
            startListeningForCommand()
        }, 800)
    }

    private fun startListeningForCommand() {
        if (speechRecognizer != null) {
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Utils.log( "SpeechRecognizer.isRecognitionAvailable() returns false")
            return
        }

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
                    initPorcupine()
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
                HomeAssistantRepository.instance.turnOnLights(listOf(device), brightnessPct = command.brightness)
            }

            is VoiceCommand.TurnOff -> {
                val device = findDeviceByName(command.deviceName) ?: return
                if (device.type == DeviceType.LIGHT || device.type == DeviceType.SWITCH) {
                    HomeAssistantRepository.instance.turnOffLights(listOf(device)) // use same method for switches
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