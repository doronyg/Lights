package com.doronyg.mylightscontroller.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.doronyg.mylightscontroller.utils.Utils

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Utils.log("BootReceiver intent action ${intent?.action}")
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let {
                VoiceControllerService.startService(it, true)
            }
        }
    }
}