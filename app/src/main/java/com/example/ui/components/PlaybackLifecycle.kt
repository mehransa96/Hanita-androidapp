package com.example.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Good-citizen audio for the in-app players:
 *  - asks for audio focus before playing and pauses when another app (or a call) takes it;
 *  - pauses when the app goes to the background, since there is no media notification to stop it;
 *  - releases the player when the screen is left.
 */
class PlaybackFocus internal constructor(context: Context, private val onLoss: () -> Unit) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val listener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) onLoss()
    }
    private val request: AudioFocusRequest? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(MEDIA_ATTRIBUTES)
                .setOnAudioFocusChangeListener(listener)
                .build()
        } else null

    /** True when the player may start. */
    fun request(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(request!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun abandon() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(request!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(listener)
        }
    }

    companion object {
        val MEDIA_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }
}

/** A [MediaPlayer] tied to this screen, plus its audio-focus handle. [onPause] updates the UI state after the player was paused. */
@Composable
fun rememberScreenPlayer(onPause: () -> Unit): Pair<MediaPlayer, PlaybackFocus> {
    val context = LocalContext.current
    val currentOnPause = rememberUpdatedState(onPause)
    val player = remember { MediaPlayer().apply { setAudioAttributes(PlaybackFocus.MEDIA_ATTRIBUTES) } }
    val pause = remember {
        {
            try {
                if (player.isPlaying) player.pause()
            } catch (_: Exception) {
            }
            currentOnPause.value()
        }
    }
    val focus = remember { PlaybackFocus(context.applicationContext) { pause() } }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                if (player.isPlaying) player.stop()
            } catch (_: Exception) {
            }
            player.release()
            focus.abandon()
        }
    }
    return player to focus
}
