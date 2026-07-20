package com.joelkanyi.scrollit

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.nio.ByteBuffer

class ScrollReceiverService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != ScrollProtocol.SCROLL_PATH) return

        val scrollPixels = messageEvent.data.toScrollPixels() ?: return
        ScrollAccessibilityService.instance?.dispatchScroll(scrollPixels)
    }

    private fun ByteArray.toScrollPixels(): Float? =
        if (size < Float.SIZE_BYTES) null else ByteBuffer.wrap(this).float
}
