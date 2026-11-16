package com.hanita.partner

import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import com.hanita.partner.ui.PartnerApp
import com.hanita.partner.ui.PartnerTheme
import com.hanita.partner.ui.ThemeController

class MainActivity : ComponentActivity() {

    /** Text received from a link or a "share" (an update from Hanita), handled by the UI once. */
    private val incoming = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) incoming.value = textFrom(intent)
        Reminders.reschedule(this)
        val store = PartnerStore(this)
        val themes = ThemeController(store.themeId, store.themeMode) { id, mode ->
            store.themeId = id
            store.themeMode = mode
        }
        setContent {
            // Status and navigation bar icons follow the chosen mode, not only the system one.
            val dark = themes.isDark()
            DisposableEffect(dark) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            PartnerTheme(themes) {
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
