package com.alhuda.app.core.data.audio

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.net.toUri
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.reminder.ReminderAudioEntry
import com.alhuda.app.core.domain.model.settings.mapAdhanIdToEntryOrNull

fun ReminderAudioEntry.toAudioUri(context: Context): Uri? =
    when (this) {
        is ReminderAudioEntry.ResourceReminderAudioEntry -> {

            val resId = when (id) {
                ReminderAudioEntry.SILENT_ID -> R.raw.silence
                else -> mapAdhanIdToEntryOrNull(id)?.resId ?: resourceId
            }
            "android.resource://${context.packageName}/$resId".toUri()
        }

        is ReminderAudioEntry.ExternalReminderAudioEntry -> filepath.toUri()

        ReminderAudioEntry.DefaultReminderAudioEntry ->
            RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }
