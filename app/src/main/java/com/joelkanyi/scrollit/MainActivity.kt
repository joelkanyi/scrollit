package com.joelkanyi.scrollit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.Wearable
import com.joelkanyi.scrollit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOpenAccessibilitySettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        showAccessibilityStatus()
        showWatchStatus()
    }

    private fun showAccessibilityStatus() {
        val enabled = ScrollAccessibilityService.instance != null
        binding.tvServiceStatus.setStatus(
            text = if (enabled) "Accessibility Service: Enabled" else "Accessibility Service: Disabled",
            ok = enabled,
        )
    }

    private fun showWatchStatus() {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            val watch = nodes.firstOrNull()
            binding.tvWatchStatus.setStatus(
                text = if (watch != null) "Watch: Connected (${watch.displayName})" else "Watch: Not connected",
                ok = watch != null,
            )
        }
    }

    private fun TextView.setStatus(text: String, ok: Boolean) {
        this.text = text
        setTextColor(if (ok) STATUS_OK_COLOR else STATUS_ERROR_COLOR)
    }

    private companion object {
        val STATUS_OK_COLOR = Color.parseColor("#4CAF50")
        val STATUS_ERROR_COLOR = Color.parseColor("#F44336")
    }
}
