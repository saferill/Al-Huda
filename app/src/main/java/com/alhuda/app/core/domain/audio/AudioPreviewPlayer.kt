package com.alhuda.app.core.domain.audio

import com.alhuda.app.core.domain.model.reminder.ReminderAudioEntry
import com.alhuda.app.core.domain.model.settings.AudioEntry
import kotlinx.coroutines.flow.StateFlow

interface AudioPreviewPlayer {
    val playingId: StateFlow<String?>

    fun play(
        entry: AudioEntry,
        volumePercent: Int = -1,
    )

    fun play(entry: ReminderAudioEntry)

    fun setVolume(volumePercent: Int)

    fun stop()

    fun release()
}
