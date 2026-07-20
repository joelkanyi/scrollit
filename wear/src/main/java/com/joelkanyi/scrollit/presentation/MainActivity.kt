package com.joelkanyi.scrollit.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.joelkanyi.scrollit.ScrollSender

class MainActivity : ComponentActivity() {

    private lateinit var scrollSender: ScrollSender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scrollSender = ScrollSender(applicationContext)

        setContent {
            WearApp(scrollSender)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scrollSender.destroy()
    }
}
