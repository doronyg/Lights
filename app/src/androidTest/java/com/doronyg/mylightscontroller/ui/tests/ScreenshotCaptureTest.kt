package com.doronyg.mylightscontroller.ui.tests

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.doronyg.mylightscontroller.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

private const val TAG = "ScreenshotCaptureTest"
private const val WAIT_TIMEOUT_MILLIS = 60000L // 60 seconds timeout

/**
 * Test class for capturing screenshots.
 *
 * This test:
 * 1. Launches the main activity
 * 2. Waits for activity to reach RESUMED state
 * 3. Captures a screenshot of the UI
 * 4. Saves the screenshot to cache directory
 * 5. Creates a ready marker file
 * 6. Waits for an external signal (adb) to stop
 *
 * An external script (run_capture_test.sh) will:
 * - Monitor for the ready marker file
 * - When found, pull the screenshot
 * - Send an adb signal to stop the test
 */
@RunWith(AndroidJUnit4::class)
class ScreenshotCaptureTest {

    private var activityContext: Context? = null

    private val context: Context
        get() = activityContext ?: InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setupTest() {
        // Set app context FIRST so Settings can access SharedPreferences
        Utils.appContext = InstrumentationRegistry.getInstrumentation().targetContext

        Log.d(TAG, "Test setup: Utils.appContext set")
        Log.d(TAG, "App context package: ${Utils.appContext?.packageName}")

        // Clear any previous stop signal
        ScreenshotUtils.clearStopSignal(context)

        // Log cache directory for external scripts
        Log.d(TAG, "Cache directory: ${ScreenshotUtils.getCacheDirPath(context)}")
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
    private suspend fun getCurrentActivity(): Activity? = withContext(Dispatchers.Main) {
        ActivityLifecycleMonitorRegistry.getInstance()
            .getActivitiesInStage(Stage.RESUMED)
            .firstOrNull()
    }

    /**
     * Wait for an external stop signal with timeout
     */
    private suspend fun waitForStopSignal(timeoutMillis: Long = WAIT_TIMEOUT_MILLIS) {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "Waiting for external stop signal (timeout: ${timeoutMillis}ms)...")

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (ScreenshotUtils.hasStopSignal(context)) {
                Log.d(TAG, "Stop signal received!")
                return
            }
            delay(500) // Check every 500ms
        }

        Log.w(TAG, "Timeout waiting for stop signal after ${timeoutMillis}ms")
        // Don't throw - just log warning and continue
    }

    @Test
    fun test_CaptureMainScreenshot() {
        // Launch the test version of MainActivity
        ActivityScenario.launch(VisualTestMainActivity::class.java).use { scenario ->
            runBlocking {
                Log.d(TAG, "=== Starting Screenshot Capture Test ===")

                try {
                    // Wait for activity to reach RESUMED state
                    waitForActivityResumed()

                    // Wait additional time for Compose layout to complete
                    Log.d(TAG, "Waiting for Compose layout (3 seconds)...")
                    delay(3000)

                    // Get the activity and capture its rendered view
                    val activity = getCurrentActivity()
                    requireNotNull(activity) { "Activity should be resumed at this point" }

                    // Set activity context so screenshots are saved to main app's directory
                    activityContext = activity.applicationContext

                    val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)
                    requireNotNull(rootView) { "Root view (android.R.id.content) should not be null" }

                    // Verify view has valid dimensions
                    if (rootView.width <= 0 || rootView.height <= 0) {
                        Log.w(TAG, "Root view has invalid dimensions: ${rootView.width}x${rootView.height}, attempting to measure...")
                    }

                    // Capture actual screenshot of rendered UI
                    Log.d(TAG, "Capturing screenshot...")
                    val screenshot = ScreenshotUtils.captureScreenshot(rootView)

                    // Verify screenshot was captured
                    requireNotNull(screenshot) { "Screenshot bitmap should not be null" }
                    if (screenshot.width <= 0 || screenshot.height <= 0) {
                        throw IllegalStateException("Screenshot has invalid dimensions: ${screenshot.width}x${screenshot.height}")
                    }

                    // Save screenshot
                    Log.d(TAG, "Saving screenshot...")
                    ScreenshotUtils.saveScreenshot(context, screenshot, "main_screen.png")

                    // Create ready marker to signal that screenshot is ready for pickup
                    Log.d(TAG, "Creating ready marker...")
                    ScreenshotUtils.createReadyMarker(context, "main_screen")

                    Log.d(TAG, "Screenshot ready. Waiting for external signal...")
                    // Wait for external script to send stop signal via adb
                    waitForStopSignal()

                    Log.d(TAG, "=== Screenshot Capture Test Complete ===")
                } catch (e: Exception) {
                    Log.e(TAG, "Test failed: ${e.message}", e)
                    throw e
                }
            }
        }
    }
}

