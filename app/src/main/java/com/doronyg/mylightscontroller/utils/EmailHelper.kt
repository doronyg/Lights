package com.doronyg.mylightscontroller.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

object EmailHelper {

    fun sendHaDataViaEmail(appContext: Context) {
        MockUtils.saveMockData(appContext) { file ->
            if (file != null) {
                sendFileViaEmail(appContext, file)
            } else {
                Utils.log("No file generated, skipping email")
            }
        }
    }

    private fun sendFileViaEmail(appContext: Context, file: File) {
        CoroutineScope(PlatformBridge.dispatchers.uiDispatcher()).launch {
            try {
                val uri = FileProvider.getUriForFile(
                    appContext,
                    "${appContext.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Home Assistant Lights Export")
                    putExtra(Intent.EXTRA_TEXT, "Attached is the exported HA lights JSON.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(
                    intent,
                    "Send export via email"
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                appContext.startActivity(chooser)

            } catch (e: Exception) {
                Utils.logException(e, "Failed to send email intent")
            }
        }
    }
}