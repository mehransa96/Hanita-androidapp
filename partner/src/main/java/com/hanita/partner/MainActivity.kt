package com.hanita.partner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.hanita.partner.ui.PartnerApp
import com.hanita.partner.ui.PartnerTheme

class MainActivity : ComponentActivity() {

    /** Text received from a link or a "share" (an update from Hanita), handled by the UI once. */
    private val incoming = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) incoming.value = textFrom(intent)
        Reminders.reschedule(this)
        setContent {
            PartnerTheme {
                PartnerApp(
                    incomingText = incoming.value,
                    onIncomingHandled = { incoming.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        textFrom(intent)?.let { incoming.value = it }
    }

    private fun textFrom(intent: Intent?): String? = when (intent?.action) {
        Intent.ACTION_VIEW -> intent.dataString
        Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
        else -> null
    }
}
