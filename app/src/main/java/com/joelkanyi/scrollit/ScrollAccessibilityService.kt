package com.joelkanyi.scrollit

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import kotlin.math.abs

// Injecting touches into other apps requires an enabled AccessibilityService,
// so this must be turned on in Settings before scrolling works.
class ScrollAccessibilityService : AccessibilityService() {

    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private val stateLock = Any()
    private var pendingScrollPixels = 0f
    private var gestureInFlight = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    fun dispatchScroll(scrollPixels: Float) {
        synchronized(stateLock) {
            pendingScrollPixels += scrollPixels * SENSITIVITY
            if (gestureInFlight) return
        }
        dispatchPendingScroll()
    }

    // Runs one swipe at a time; scrolls that arrive mid-gesture accumulate and are
    // flushed by the completion callback so gestures never overlap.
    private fun dispatchPendingScroll() {
        val scrollPixels: Float
        synchronized(stateLock) {
            scrollPixels = pendingScrollPixels
            pendingScrollPixels = 0f
            if (scrollPixels == 0f) {
                gestureInFlight = false
                return
            }
            gestureInFlight = true
        }

        val swipe = buildSwipeGesture(scrollPixels)
        if (swipe == null) {
            synchronized(stateLock) {
                pendingScrollPixels += scrollPixels
                gestureInFlight = false
            }
            return
        }
        dispatchGesture(swipe, gestureCallback, mainThreadHandler)
    }

    private fun buildSwipeGesture(scrollPixels: Float): GestureDescription? {
        val metrics = resources.displayMetrics
        val centerX = metrics.widthPixels / 2f
        val centerY = metrics.heightPixels / 2f
        val maxSwipePixels = metrics.heightPixels / MAX_SWIPE_SCREEN_FRACTION

        val swipePixels = scrollPixels.coerceIn(-maxSwipePixels, maxSwipePixels)
        if (abs(swipePixels) < MIN_SWIPE_DISTANCE_PX) return null

        val path = Path().apply {
            moveTo(centerX, centerY)
            lineTo(centerX, centerY + swipePixels)
        }
        return GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, GESTURE_DURATION_MS))
            .build()
    }

    private val gestureCallback = object : GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) = dispatchPendingScroll()
        override fun onCancelled(gestureDescription: GestureDescription?) {
            synchronized(stateLock) { gestureInFlight = false }
        }
    }

    companion object {
        @Volatile
        var instance: ScrollAccessibilityService? = null
            private set

        private const val SENSITIVITY = 2.0f
        private const val MIN_SWIPE_DISTANCE_PX = 30f
        private const val MAX_SWIPE_SCREEN_FRACTION = 3f

        // Slow enough that the system reads the gesture as a scroll, not a tap.
        private const val GESTURE_DURATION_MS = 150L
    }
}
