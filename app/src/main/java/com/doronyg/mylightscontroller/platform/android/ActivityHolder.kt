package com.doronyg.mylightscontroller.platform.android

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.doronyg.mylightscontroller.utils.Utils
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks all resumed Activities without requiring them to be passed around.
 * Uses WeakReferences to avoid memory leaks and handles multiple resumed activities
 * (e.g., in multi-window scenarios where pause/resume order is not guaranteed).
 *
 * Automatically registers and unregisters with the Application lifecycle.
 */
object ActivityHolder {
    // Map of activity id to WeakReference - thread-safe for multi-window scenarios
    private val resumedActivities = ConcurrentHashMap<Int, WeakReference<Activity>>()
    private var lastResumedActivityId = 0
    private var isRegistered = false

    /**
     * Get the last resumed Activity, or null if none are resumed.
     * In multi-window scenarios, returns the most recently resumed activity.
     */
    fun getCurrentActivity(): Activity? {
        // Clean up dead references
        val deadKeys = resumedActivities.filter { it.value.get() == null }.keys
        deadKeys.forEach { resumedActivities.remove(it) }

        // Return the last resumed activity if available
        return if (resumedActivities.isNotEmpty()) {
            resumedActivities[lastResumedActivityId]?.get()
        } else {
            null
        }
    }

    /**
     * Get all currently resumed activities.
     * Useful for multi-window or split-screen scenarios.
     */
    fun getResumedActivities(): List<Activity> {
        // Clean up dead references
        val deadKeys = resumedActivities.filter { it.value.get() == null }.keys
        deadKeys.forEach { resumedActivities.remove(it) }

        return resumedActivities.mapNotNull { it.value.get() }
    }

    /**
     * Register this holder with the Application to track activity lifecycle.
     * Should be called once from MainActivity.onCreate() or similar.
     * Safe to call multiple times - only registers once.
     */
    fun registerWithApplication(application: Application) {
        if (isRegistered) {
            Utils.log("ActivityHolder: Already registered with Application")
            return
        }

        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                val activityId = System.identityHashCode(activity)
                Utils.log("ActivityHolder: Activity resumed: ${activity.javaClass.simpleName} (id=$activityId)")
                resumedActivities[activityId] = WeakReference(activity)
                lastResumedActivityId = activityId
            }

            override fun onActivityPaused(activity: Activity) {
                val activityId = System.identityHashCode(activity)
                Utils.log("ActivityHolder: Activity paused: ${activity.javaClass.simpleName} (id=$activityId)")
                resumedActivities.remove(activityId)
            }

            override fun onActivityDestroyed(activity: Activity) {
                val activityId = System.identityHashCode(activity)
                Utils.log("ActivityHolder: Activity destroyed: ${activity.javaClass.simpleName} (id=$activityId)")
                resumedActivities.remove(activityId)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
        isRegistered = true
        Utils.log("ActivityHolder: Registered with Application")
    }
}


