package com.alhuda.app.core.data.audio

import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import androidx.core.content.getSystemService
import com.alhuda.app.R
import com.alhuda.app.core.data.locale.LocalizedResources
import com.alhuda.app.core.domain.audio.AudioPreviewPlayer
import com.alhuda.app.core.domain.model.reminder.ReminderAudioEntry
import com.alhuda.app.core.domain.model.settings.AudioEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioPreviewPlayerImpl(
    private val context: Context,

    private val localizedResources: LocalizedResources,
) : AudioPreviewPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val playingId: StateFlow<String?> = AdhanPreviewPlaybackService.playingId

    override fun play(
        entry: AudioEntry,
        volumePercent: Int,
    ) {
        scope.launch {
            val uri = entry.toAudioUri(context) ?: return@launch
            AdhanPreviewPlaybackService.play(context, uri, entry.id, entry.label(), entry.loop, volumePercent)

            if (volumePercent !in 0..100) warnIfMediaMuted()
        }
    }

    override fun play(entry: ReminderAudioEntry) {
        scope.launch {
            val uri = entry.toAudioUri(context) ?: return@launch
            AdhanPreviewPlaybackService.play(context, uri, entry.previewId(), entry.previewLabel(), entry.loop)
            warnIfMediaMuted()
        }
    }

    override fun setVolume(volumePercent: Int) {
        if (playingId.value == null) return
        AdhanPreviewPlaybackService.setVolume(context, volumePercent)
    }

    override fun stop() = AdhanPreviewPlaybackService.stop(context)

    private suspend fun warnIfMediaMuted() {
        val audioManager = context.getSystemService<AudioManager>() ?: return
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0) return
        withContext(Dispatchers.Main) {
            Toast.makeText(context, localizedResources.current.getString(R.string.preview_muted_hint), Toast.LENGTH_SHORT).show()
        }
    }

    override fun release() {
        scope.cancel()
        stop()
    }

    private fun AudioEntry.label(): String =
        when (this) {
            is AudioEntry.ResourceAudioEntry -> localizedResources.current.getString(labelResId)
            is AudioEntry.ExternalAudioEntry -> label
        }

    private fun ReminderAudioEntry.previewId(): String =
        when (this) {
            is ReminderAudioEntry.ResourceReminderAudioEntry -> id
            is ReminderAudioEntry.ExternalReminderAudioEntry -> id
            ReminderAudioEntry.DefaultReminderAudioEntry -> ReminderAudioEntry.DefaultReminderAudioEntry.id
        }

    private fun ReminderAudioEntry.previewLabel(): String =
        when (this) {
            is ReminderAudioEntry.ResourceReminderAudioEntry -> label
            is ReminderAudioEntry.ExternalReminderAudioEntry -> label
            ReminderAudioEntry.DefaultReminderAudioEntry -> localizedResources.current.getString(R.string.reminder_default_sound)
        }
}
