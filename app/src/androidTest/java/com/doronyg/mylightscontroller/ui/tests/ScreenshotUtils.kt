package com.doronyg.mylightscontroller.ui.tests

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.util.Log
import android.view.View
import java.io.File
import java.io.FileOutputStream

private const val STOP_MARKER = "stop.marker"

/**
 * Utility class for capturing and saving screenshots in tests.
 */
object ScreenshotUtils {
    private const val TAG = "ScreenshotUtils"

    /**
     * Capture a screenshot of the view hierarchy by drawing it to a canvas
     */
    fun captureScreenshot(view: View): Bitmap {
        // Ensure view has proper dimensions
        if (view.width <= 0 || view.height <= 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST)
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        }

        Log.d(TAG, "Capturing screenshot with dimensions: ${view.width}x${view.height}")
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        Log.d(TAG, "Screenshot captured successfully")
        return bitmap
    }

    /**
     * Save a bitmap to external storage (persistent across test runs)
     * Files are saved to /sdcard/Android/data/com.doronyg.mylightscontroller/files/Download/TestOutputs/
     */
    fun saveScreenshot(context: Context, bitmap: Bitmap, filename: String, subdirectory: String = "screenshots"): File {
        // Save to persistent external files directory
        val externalDir = getExternalDir(context)
        val screenshotDir = File(externalDir, subdirectory).apply {
            if (!exists()) mkdirs()
        }

        val screenshotFile = File(screenshotDir, filename)
        Log.d(TAG, "Saving screenshot to: ${screenshotFile.absolutePath}")
        try {
            FileOutputStream(screenshotFile).use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                outStream.flush()
            }
            Log.d(TAG, "Screenshot saved: ${screenshotFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save screenshot: ${e.message}", e)
            throw e
        }

        return screenshotFile
    }

    /**
     * Get the screenshot file path for a given filename
     * Uses persistent external files directory
     */
    fun getScreenshotFile(context: Context, filename: String, subdirectory: String = "screenshots"): File {
        val externalDir = getExternalDir(context)
        return File(File(externalDir, subdirectory), filename)
    }

    /**
     * Get the baseline file path for a given filename
     * Uses persistent external files directory
     */
    fun getBaselineFile(context: Context, filename: String) =  File(File(getExternalDir(context), "baselines"), filename)

    /**
     * Create a marker file to signal that screenshot is ready
     * Uses persistent external files directory
     */
    fun createReadyMarker(context: Context, filename: String): File {
        val externalDir = getExternalDir(context)
        val markerFile = File(externalDir, "ready.marker")
        markerFile.createNewFile()
        Log.d(TAG, "Created ready marker: ${markerFile.absolutePath}")
        return markerFile
    }

    /**
     * Check if a stop signal file exists
     * Uses persistent external files directory
     */
    fun hasStopSignal(context: Context): Boolean {
        val externalDir = getExternalDir(context)
        val stopFile = File(externalDir, STOP_MARKER)
        return stopFile.exists()
    }

    /**
     * Create a stop signal file
     * Uses persistent external files directory
     */
    fun createStopSignal(context: Context): File {
        val externalDir = getExternalDir(context)
        val stopFile = File(externalDir, STOP_MARKER)
        stopFile.createNewFile()
        Log.d(TAG, "Created stop signal: ${stopFile.absolutePath}")
        return stopFile
    }

    /**
     * Clean up stop signal
     */
    fun clearStopSignal(context: Context) {
        val externalDir = getExternalDir(context)
        val stopFile = File(externalDir, STOP_MARKER)
        if (stopFile.exists()) {
            stopFile.delete()
            Log.d(TAG, "Cleared stop signal")
        }
    }

    /**
     * Get the cache directory path for external scripts to monitor
     */
    fun getCacheDirPath(context: Context): String {
        return getExternalDir(context)?.absolutePath ?: ""
    }

    private fun getExternalDir(context: Context): File? =
        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
}

