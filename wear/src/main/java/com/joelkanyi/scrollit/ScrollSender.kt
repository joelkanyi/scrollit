package com.joelkanyi.scrollit

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.nio.ByteBuffer
import kotlin.math.abs

// Batches small scrolls over a short window to avoid flooding the connection,
// but sends immediately once enough has accumulated so fast scrolling stays responsive.
class ScrollSender(context: Context) {

    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var pendingScrollPixels = 0f
    private var flushJob: Job? = null
    private var phoneNodeId: String? = null

    suspend fun findPhoneNodeId(): String? {
        phoneNodeId?.let { return it }
        return try {
            phoneNodeId = nodeClient.connectedNodes.await().firstOrNull()?.id
            phoneNodeId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to find phone node", e)
            null
        }
    }

    fun sendScroll(scrollPixels: Float) {
        pendingScrollPixels += scrollPixels
        if (abs(pendingScrollPixels) > IMMEDIATE_SEND_THRESHOLD_PX) {
            flush()
        } else {
            scheduleFlush()
        }
    }

    fun destroy() = coroutineScope.cancel()

    private fun scheduleFlush() {
        if (flushJob?.isActive == true) return
        flushJob = coroutineScope.launch {
            delay(BATCH_WINDOW_MS)
            flush()
        }
    }

    private fun flush() {
        val scrollPixels = pendingScrollPixels
        pendingScrollPixels = 0f
        flushJob?.cancel()
        flushJob = null
        if (scrollPixels == 0f) return

        coroutineScope.launch { sendToPhone(scrollPixels) }
    }

    private suspend fun sendToPhone(scrollPixels: Float) {
        val nodeId = findPhoneNodeId() ?: run {
            Log.w(TAG, "No connected phone, dropping scroll: $scrollPixels")
            return
        }
        val payload = ByteBuffer.allocate(Float.SIZE_BYTES).putFloat(scrollPixels).array()
        try {
            messageClient.sendMessage(nodeId, ScrollProtocol.SCROLL_PATH, payload).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send scroll", e)
            phoneNodeId = null
        }
    }

    private companion object {
        const val TAG = "ScrollSender"
        const val IMMEDIATE_SEND_THRESHOLD_PX = 200f
        const val BATCH_WINDOW_MS = 30L
    }
}
