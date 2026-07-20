package com.joelkanyi.scrollit.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.joelkanyi.scrollit.ScrollSender

@Composable
fun WearApp(scrollSender: ScrollSender) {
    var isConnected by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // The rotary crown only delivers events to a focused, focusable component.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(Unit) { isConnected = scrollSender.findPhoneNodeId() != null }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onRotaryScrollEvent { event ->
                    // Anticlockwise scrolls the phone down (forward); clockwise scrolls up (back).
                    // The crown reports the opposite sign, so invert it.
                    scrollSender.sendScroll(-event.verticalScrollPixels)
                    true
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragPixels ->
                        // Dragging up should scroll down, so invert the drag.
                        scrollSender.sendScroll(-dragPixels)
                    }
                }
                .focusRequester(focusRequester)
                .focusable(),
            contentAlignment = Alignment.Center,
        ) {
            ConnectionStatus(isConnected)
        }
    }
}

@Composable
private fun ConnectionStatus(isConnected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "scrollit",
            color = Color.White,
            style = MaterialTheme.typography.title2,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isConnected) "Connected" else "No phone found",
            color = if (isConnected) Color.Green else Color.Red,
            style = MaterialTheme.typography.body2,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Crown or swipe to scroll",
            color = Color.Gray,
            style = MaterialTheme.typography.caption3,
        )
    }
}
