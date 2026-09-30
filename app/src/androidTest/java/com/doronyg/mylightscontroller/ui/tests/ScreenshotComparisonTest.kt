package com.doronyg.mylightscontroller.ui.tests

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "ScreenshotComparisonTest"
private const val PIXEL_TOLERANCE = 0.05f // 5% pixel tolerance
private const val WAIT_TIMEOUT_MILLIS = 90000L // 90 seconds timeout

/**
 * Test class for comparing screenshots against baselines with tolerance.
 *
 * This test:
 * 1. Waits for baseline file to be pushed to device
 * 2. Loads baseline from device storage
 * 3. Loads captured screenshot from device storage
 * 4. Compares pixel-by-pixel with 5% tolerance
 * 5. Fails if pixel comparison fails
 *
 * Run AFTER ScreenshotCaptureTest captures the screenshot.
 */
@RunWith(AndroidJUnit4::class)
class ScreenshotComparisonTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

     /**
      * Wait for baseline file to be pushed with timeout
      */
     private suspend fun waitForBaselineFile(baselineFile: String, timeoutMillis: Long = WAIT_TIMEOUT_MILLIS) : File {

         val startTime = System.currentTimeMillis()

         while (System.currentTimeMillis() - startTime < timeoutMillis) {
             val baselineFile = ScreenshotUtils.getBaselineFile(context, baselineFile)
             Log.d(TAG, "Waiting for baseline file: ${baselineFile.absolutePath}")
             if (baselineFile.exists()) {
                 Log.d(TAG, "Baseline file found! (${baselineFile.length()} bytes)")
                 return baselineFile
             }
             delay(500.milliseconds)
         }

         throw AssertionError("Timeout waiting for baseline file after ${timeoutMillis}ms")
     }

     /**
      * Load bitmap from file
      */
     private fun loadBitmapFromFile(file: File): Bitmap? {
         return try {
             if (!file.exists()) {
                 Log.w(TAG, "File does not exist: ${file.absolutePath}")
                 return null
             }
             BitmapFactory.decodeFile(file.absolutePath)
         } catch (e: Exception) {
             Log.e(TAG, "Failed to load bitmap: ${e.message}", e)
             null
         }
     }

    /**
     * Compare two bitmaps pixel-by-pixel with tolerance
     * Returns percentage of pixels that differ
     */
    private fun comparePixels(baseline: Bitmap, actual: Bitmap): Float {
        if (baseline.width != actual.width || baseline.height != actual.height) {
            Log.w(TAG, "Bitmap dimensions don't match: " +
                    "${baseline.width}x${baseline.height} vs ${actual.width}x${actual.height}")
            return 1.0f // 100% different
        }

        val pixelCount = baseline.width * baseline.height
        val pixels1 = IntArray(pixelCount)
        val pixels2 = IntArray(pixelCount)

        baseline.getPixels(pixels1, 0, baseline.width, 0, 0, baseline.width, baseline.height)
        actual.getPixels(pixels2, 0, actual.width, 0, 0, actual.width, actual.height)

        var diffPixels = 0
        for (i in 0 until pixelCount) {
            if (pixels1[i] != pixels2[i]) {
                diffPixels++
            }
        }

        val diffPercentage = diffPixels.toFloat() / pixelCount
        Log.d(TAG, "Pixel comparison: $diffPixels / $pixelCount pixels differ (${String.format("%.2f", diffPercentage * 100)}%)")
        return diffPercentage
    }



    /**
     * Compare main screen screenshot against baseline
     */
    @Test
    fun test_CompareMainScreenshot() {
        // Launch the test version of MainActivity
        ActivityScenario.launch(VisualTestMainActivity::class.java).use { scenario ->
            runBlocking {
                Log.d(TAG, "=== Starting Screenshot Comparison Test ===")

                val screenshotFileName = "main_screen.png"

                try {
                    // Signal that we're ready for the baseline file
                    Log.d(TAG, "Creating ready marker to signal we're waiting for baseline...")
                    ScreenshotUtils.createReadyMarker(context, "baseline")

                    // Load baseline from device storage
                    val baseline = loadBitmapFromFile(waitForBaselineFile(screenshotFileName))
                    if (baseline == null) {
                        Log.e(TAG, "Failed to load baseline after file was found")
                        throw AssertionError("Failed to load baseline bitmap")
                    }
                    Log.d(TAG, "Baseline loaded from device (${baseline.width}x${baseline.height})")

                    // Wait for activity to reach RESUMED state
                    waitForActivityResumed()

                    // Wait additional time for Compose layout to complete
                    Log.d(TAG, "Waiting for Compose layout (3 seconds)...")
                    delay(3000)

                    // Get the activity and capture its rendered view
                    val activity = getCurrentActivity()
                    requireNotNull(activity) { "Activity should be resumed at this point" }

                    val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)
                    requireNotNull(rootView) { "Root view (android.R.id.content) should not be null" }

                    // Verify view has valid dimensions
                    if (rootView.width <= 0 || rootView.height <= 0) {
                        Log.w(TAG, "Root view has invalid dimensions: ${rootView.width}x${rootView.height}, attempting to measure...")
                    }

                    // Capture actual screenshot of rendered UI
                    Log.d(TAG, "Capturing screenshot...")
                    val actual = ScreenshotUtils.captureScreenshot(rootView)

                    // Verify screenshot was captured
                    requireNotNull(actual) { "Screenshot bitmap should not be null" }
                    if (actual.width <= 0 || actual.height <= 0) {
                        throw IllegalStateException("Screenshot has invalid dimensions: ${actual.width}x${actual.height}")
                    }

                    Log.d(TAG, "Actual screenshot captured (${actual.width}x${actual.height})")

                    // ===== PIXEL COMPARISON =====
                    Log.d(TAG, "Starting pixel comparison (tolerance: ${PIXEL_TOLERANCE * 100}%)...")
                    val pixelDiffPercentage = comparePixels(baseline, actual)
                    val pixelCheckPassed = pixelDiffPercentage <= PIXEL_TOLERANCE

                    Log.d(TAG, "Pixel check: ${if (pixelCheckPassed) "PASSED" else "FAILED"} " +
                            "(${String.format("%.2f", pixelDiffPercentage * 100)}% vs ${PIXEL_TOLERANCE * 100}% threshold)")

                    // ===== FINAL VERDICT =====
                    Log.d(TAG, "")
                    if (pixelCheckPassed) {
                        Log.d(TAG, "✓ VISUAL REGRESSION TEST PASSED")
                        Log.d(TAG, "  - Pixel difference: ${String.format("%.2f", pixelDiffPercentage * 100)}% (OK)")
                        Log.d(TAG, "=== Screenshot Comparison Test Complete ===")
                    } else {
                        Log.e(TAG, "✗ VISUAL REGRESSION TEST FAILED")
                        Log.e(TAG, "  - Pixel difference: ${String.format("%.2f", pixelDiffPercentage * 100)}% (EXCEEDS ${PIXEL_TOLERANCE * 100}%)")
                        Log.e(TAG, "Visual regression detected. Review and update baseline if changes are intentional.")

                        throw AssertionError("Visual regression detected")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Test failed: ${e.message}", e)
                    throw e
                }
            }
        }
    }

    /**
     * Wait for activity to reach RESUMED state (must be called on main thread)
     */
    private suspend fun waitForActivityResumed(timeoutMillis: Long = 10000) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            val activity = getCurrentActivity()
            if (activity != null) {
                Log.d(TAG, "Activity resumed: ${activity.javaClass.simpleName}")
                return
            }
            delay(100)
        }
        throw RuntimeException("Activity did not resume within ${timeoutMillis}ms")
    }

    /**
     * Get the currently active activity in RESUMED state (must be called on main thread)
     */
    private suspend fun getCurrentActivity(): android.app.Activity? = withContext(Dispatchers.Main) {
        ActivityLifecycleMonitorRegistry.getInstance()
            .getActivitiesInStage(Stage.RESUMED)
            .firstOrNull()
    }
}
