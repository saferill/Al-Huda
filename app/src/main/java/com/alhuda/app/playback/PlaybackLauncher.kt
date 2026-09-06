package com.alhuda.app.playback

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.alhuda.app.adhan.AdhanContract
import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.util.device.AudioDeviceInspector
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class PlaybackRequest(
    val title: String,
    val body: String?,
    val timeLabel: String,
    val soundUri: Uri,
    val channelId: String,
    val loop: Boolean,
    val volumePercent: Int,

    val fadeInVolume: Boolean,
    val fullScreen: Boolean,

    val forceLaunchActivity: Boolean,
    val vibration: VibrationMode,
    val volumeButtonStops: Boolean,

    val preferExternalAudioDevice: Boolean,

    val prayerName: String? = null,

    val header: String? = null,
    val isReminder: Boolean = false,

    val languageTags: String = "",
) {
    companion object {

        fun from(
            settings: Settings,
            alarmSettings: AlarmSettings,
            title: String,
            body: String?,
            timeLabel: String,
            soundUri: Uri,
            channelId: String,
            loop: Boolean,
            vibration: VibrationMode,
            prayerName: String? = null,
            header: String? = null,
            isReminder: Boolean = false,
        ) = PlaybackRequest(
            title = title,
            body = body,
            timeLabel = timeLabel,
            soundUri = soundUri,
            channelId = channelId,
            loop = loop,
            volumePercent = settings.alarmVolume ?: -1,
            fadeInVolume = settings.gradualAlarmVolume,
            fullScreen = !alarmSettings.dontTurnOnScreen,
            forceLaunchActivity = settings.forceLaunchAlarmActivity,
            vibration = vibration,
            volumeButtonStops = settings.volumeButtonStopsAdhan,
            preferExternalAudioDevice = settings.preferExternalAudioDevice,
            prayerName = prayerName,
            header = header,
            isReminder = isReminder,
            languageTags = settings.selectedLocale,
        )
    }
}

@Singleton
class PlaybackLauncher @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun launch(request: PlaybackRequest) {

        val useMediaUsage = request.preferExternalAudioDevice &&
            AudioDeviceInspector.isExternalDeviceConnected(context)
        val extras = Bundle().apply {
            putString(AdhanContract.EXTRA_PRAYER, request.prayerName)
            putString(PlaybackService.EXTRA_TITLE, request.title)
            putString(PlaybackService.EXTRA_HEADER, request.header)
            putBoolean(PlaybackService.EXTRA_IS_REMINDER, request.isReminder)
            putString(PlaybackService.EXTRA_BODY, request.body)
            putString(PlaybackService.EXTRA_TIME_LABEL, request.timeLabel)
            putString(PlaybackService.EXTRA_SOUND_URI, request.soundUri.toString())
            putBoolean(PlaybackService.EXTRA_LOOP, request.loop)
            putString(PlaybackService.EXTRA_CHANNEL_ID, request.channelId)
            putInt(PlaybackService.EXTRA_VOLUME_PERCENT, request.volumePercent)
            putBoolean(PlaybackService.EXTRA_FADE_IN_VOLUME, request.fadeInVolume)
            putBoolean(PlaybackService.EXTRA_USE_MEDIA_USAGE, useMediaUsage)
            putBoolean(PlaybackService.EXTRA_FULL_SCREEN, request.fullScreen)
            putBoolean(PlaybackService.EXTRA_FORCE_LAUNCH_ACTIVITY, request.forceLaunchActivity)
            putString(PlaybackService.EXTRA_VIBRATION, request.vibration.name)
            putBoolean(PlaybackService.EXTRA_VOLUME_BUTTON_STOPS, request.volumeButtonStops)
            putString(PlaybackService.EXTRA_LANGUAGE_TAGS, request.languageTags)
        }
        PlaybackService.start(context, extras)
    }
}
