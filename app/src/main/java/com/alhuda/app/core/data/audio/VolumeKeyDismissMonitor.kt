package com.alhuda.app.core.data.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaMetadata
import android.media.VolumeProvider
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

class VolumeKeyDismissMonitor(
    private val context: Context,
    private val nowPlaying: NowPlaying? = null,
    private val onDismiss: () -> Unit,
) {

    data class NowPlaying(
        val title: String,
        val subtitle: String,
    )

    private val mainHandler = Handler(Looper.getMainLooper())
    private var session: MediaSession? = null
    private var receiver: BroadcastReceiver? = null

    fun start() {
        session = createSession()

        if (session == null) registerVolumeReceiver()
    }

    fun stop() {
        session?.let {
            runCatching { it.isActive = false }
            runCatching { it.release() }
        }
        session = null
        receiver?.let { runCatching { context.unregisterReceiver(it) } }
        receiver = null
    }

    private fun dismiss() {
        stop()
        onDismiss()
    }

    private fun createSession(): MediaSession? {
        val session = runCatching { MediaSession(context, SESSION_TAG) }.getOrNull() ?: return null
        val provider = object : VolumeProvider(VOLUME_CONTROL_RELATIVE, VOLUME_PROVIDER_MAX, VOLUME_PROVIDER_MAX / 2) {
            override fun onAdjustVolume(direction: Int) {
                if (direction != 0) dismiss()
            }

            override fun onSetVolumeTo(volume: Int) = dismiss()
        }
        val armed = runCatching {

            session.setCallback(
                object : MediaSession.Callback() {
                    override fun onStop() = dismiss()
                },
                mainHandler,
            )
            nowPlaying?.let {
                session.setMetadata(
                    MediaMetadata.Builder()
                        .putString(MediaMetadata.METADATA_KEY_TITLE, it.title)
                        .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, it.title)
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, it.subtitle)
                        .putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, it.subtitle)
                        .build(),
                )
            }
            session.setPlaybackToRemote(provider)

            session.setPlaybackState(
                PlaybackState.Builder()
                    .setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f)
                    .build(),
            )
            session.isActive = true
        }.isSuccess
        if (!armed) {
            runCatching { session.release() }
            return null
        }
        return session
    }

    private fun registerVolumeReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(
                c: Context?,
                i: Intent?,
            ) {
                if (i != null) dismiss()
            }
        }
        this.receiver = receiver
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(ACTION_VOLUME_CHANGED),

            ContextCompat.RECEIVER_EXPORTED,
        )
    }

    companion object {

        const val ACTION_VOLUME_CHANGED = "android.media.VOLUME_CHANGED_ACTION"

        private const val SESSION_TAG = "alarm-volume"

        private const val VOLUME_PROVIDER_MAX = 100
    }
}
