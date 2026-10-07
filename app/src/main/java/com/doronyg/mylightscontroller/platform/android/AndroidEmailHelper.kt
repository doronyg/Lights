package com.doronyg.mylightscontroller.platform.android

import android.content.Intent
import androidx.core.content.FileProvider
import com.doronyg.mylightscontroller.platform.common.IPlatformDataExporter
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.utils.MockUtils
import com.doronyg.mylightscontroller.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * Android implementation of [IPlatformDataExporter].
 * Sends files via Android Intent.ACTION_SEND.
 * Context is obtained from AndroidUtils (initialized at app startup).
 */
object AndroidEmailHelper : IPlatformDataExporter {

    override fun exportHaData() = sendHaDataViaEmail()

    fun sendHaDataViaEmail() {
        MockUtils.saveMockData { path ->
            if (path != null) {
                sendFileViaEmail(File(path))
            } else {
                Utils.log("No file generated, skipping email")
            }
        }
    }

    private fun sendFileViaEmail(file: File) {
        val appContext = AndroidUtils.appContext ?: run {
            Utils.log("appContext not available for email")
            return
        }

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




