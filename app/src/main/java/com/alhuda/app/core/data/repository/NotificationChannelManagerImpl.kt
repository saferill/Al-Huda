package com.alhuda.app.core.data.repository

import android.app.NotificationChannel
import android.content.Context
import android.content.res.Resources
import android.media.AudioAttributes
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.alhuda.app.R
import com.alhuda.app.core.data.locale.LocalizedResources
import com.alhuda.app.core.data.mapping.asString
import com.alhuda.app.core.domain.model.notification.NotificationChannelConfig
import com.alhuda.app.core.domain.model.notification.toNotificationManagerCompat
import com.alhuda.app.core.domain.repository.NotificationChannelManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationChannelManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val localizedResources: LocalizedResources,
) : NotificationChannelManager {
    private val notificationManager = NotificationManagerCompat.from(context)

    private val silenceUri = "android.resource://${context.packageName}/${R.raw.silence}".toUri()

    private val alarmAudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    override fun ensureChannelsExist(configs: List<NotificationChannelConfig>) {

        val localized = localizedResources.current
        configs.forEach { config ->
            createOrUpdateChannel(config, localized)
        }
    }

    override fun deleteChannel(channelId: String) {
        notificationManager.deleteNotificationChannel(channelId)
    }

    private fun createOrUpdateChannel(
        config: NotificationChannelConfig,
        localized: Resources,
    ) {
        val channel = NotificationChannel(
            config.id,
            config.name.asString(localized),
            config.importanceLevel.toNotificationManagerCompat(),
        ).apply {
            description = config.description.asString(localized)
            setShowBadge(config.showBadge)
            enableVibration(config.vibrationEnabled)
            config.vibrationPattern?.let { vibrationPattern = it.toLongArray() }
            when {

                !config.soundEnabled -> setSound(null, null)

                config.soundHandledExternally -> setSound(silenceUri, alarmAudioAttributes)

                config.soundUri != null -> setSound(
                    config.soundUri.toUri(),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                        .build(),
                )
            }
            if (config.canBypassDnd) setBypassDnd(true)
        }
        notificationManager.createNotificationChannel(channel)
    }
}
