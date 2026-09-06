package com.alhuda.app.core.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class SoftSoundPlayer @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val current = AtomicReference<Playback?>(null)

    suspend fun play(
        uri: Uri,
        stopOnVolumeButton: Boolean = false,
    ) {

        withContext(Dispatchers.IO) {
            withTimeoutOrNull(TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    val playback = Playback(cont)

                    current.getAndSet(playback)?.finish()
                    playback.start(uri, stopOnVolumeButton)
                }
            }
        }
    }

    fun stop() {
        current.get()?.finish()
    }

    private inner class Playback(
        private val cont: CancellableContinuation<Unit>,
    ) {
        private val player = MediaPlayer()
        private val done = AtomicBoolean(false)
        private var volumeKeyMonitor: VolumeKeyDismissMonitor? = null

        fun start(
            uri: Uri,
            stopOnVolumeButton: Boolean,
        ) {
            cont.invokeOnCancellation { finish() }

            val started = !done.get() && runCatching {
                player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                player.setOnPreparedListener { runCatching { it.start() } }
                player.setOnCompletionListener { finish() }
                player.setOnErrorListener { _, _, _ ->
                    finish()
                    true
                }
                if (stopOnVolumeButton) {
                    volumeKeyMonitor = VolumeKeyDismissMonitor(context) { finish() }.also { it.start() }
                }
                player.setDataSource(context, uri)
                player.prepareAsync()
            }.isSuccess
            if (!started) finish()
        }

        fun finish() {
            if (!done.compareAndSet(false, true)) return
            volumeKeyMonitor?.stop()
            volumeKeyMonitor = null
            runCatching { player.release() }
            current.compareAndSet(this, null)
            if (cont.isActive) cont.resume(Unit)
        }
    }

    private companion object {

        const val TIMEOUT_MS = 8_000L
    }
}
