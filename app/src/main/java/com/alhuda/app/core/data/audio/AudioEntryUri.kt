package com.alhuda.app.core.data.audio

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.net.toUri
import com.alhuda.app.core.domain.model.settings.AudioEntry
import com.alhuda.app.core.domain.model.settings.NOTIFICATION_AUDIO_ID

fun AudioEntry.toAudioUri(context: Context): Uri? =
    when (this) {
        is AudioEntry.ResourceAudioEntry ->
            when (id) {
                NOTIFICATION_AUDIO_ID ->

                    RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                else -> resId?.let { "android.resource://${context.packageName}/$it".toUri() }
            }

        is AudioEntry.ExternalAudioEntry -> filepath?.toUri()
    }
