package com.alhuda.app.adhan

import android.app.NotificationManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.getSystemService
import com.alhuda.app.R
import com.alhuda.app.adhan.AdhanFiringHandler.Companion.DEV_TEST_PRAYER
import com.alhuda.app.alarm.DndSilenceController
import com.alhuda.app.core.data.audio.AudioDurationProbe
import com.alhuda.app.core.data.audio.SoftSoundPlayer
import com.alhuda.app.core.data.audio.toAudioUri
import com.alhuda.app.core.data.locale.LocalizedResources
import com.alhuda.app.core.domain.model.TextResource
import com.alhuda.app.core.domain.model.adhan.AdhanKey
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.adhan.toAdhanKey
import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.alarm.AlarmType
import com.alhuda.app.core.domain.model.alarm.ScheduledAlarm
import com.alhuda.app.core.domain.model.alarm.SkippedAlarm
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import com.alhuda.app.core.domain.model.alarm.upsert
import com.alhuda.app.core.domain.model.notification.AndroidNotificationCategory
import com.alhuda.app.core.domain.model.notification.AndroidNotificationConfig
import com.alhuda.app.core.domain.model.notification.NotificationButton
import com.alhuda.app.core.domain.model.notification.NotificationConfig
import com.alhuda.app.core.domain.model.notification.NotificationPressAction
import com.alhuda.app.core.domain.model.settings.AudioEntry
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.model.settings.isResolvable
import com.alhuda.app.core.domain.repository.AlarmRepository
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.NotificationRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.usecase.EnsureNotificationChannelsUseCase
import com.alhuda.app.core.domain.usecase.GetNextShariaTimesUseCase
import com.alhuda.app.core.domain.usecase.ShariaTimeDetails
import com.alhuda.app.core.domain.util.formatTime
import com.alhuda.app.core.domain.util.toLocalDate
import com.alhuda.app.core.presentation.navigation.Route
import com.alhuda.app.core.util.device.CallStateInspector
import com.alhuda.app.core.util.device.VibrationController
import com.alhuda.app.playback.PlaybackLauncher
import com.alhuda.app.playback.PlaybackRequest
import com.alhuda.app.playback.PlaybackService
import com.alhuda.app.playback.SoftSoundContract
import com.alhuda.app.playback.missedNotificationConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Instant

@Singleton
class AdhanFiringHandler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val getNextShariaTimesUseCase: GetNextShariaTimesUseCase,
    private val notificationRepository: NotificationRepository,
    private val alarmRepository: AlarmRepository,
    private val adhanScheduler: AdhanScheduler,
    private val playbackLauncher: PlaybackLauncher,
    private val audioDurationProbe: AudioDurationProbe,
    private val softSoundPlayer: SoftSoundPlayer,
    private val dndSilenceController: DndSilenceController,
    private val localizedResources: LocalizedResources,
) {
    private companion object {

        val DEV_TEST_PRAYER = Prayer.Fajr
    }

    private val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    suspend fun onAdhanFired(
        prayer: Prayer,
        playSound: Boolean,
        timestamp: Long,
    ) {

        val deliveredForMs = settingsRepository.data.first()
            .deliveredAlarmTimestamps[AdhanContract.ADHAN_NOTIFICATION_ID]
        if (deliveredForMs == timestamp) return

        settingsRepository.markDelivered(AdhanContract.ADHAN_NOTIFICATION_ID, timestamp)

        adhanScheduler.schedule()

        notificationRepository.cancelNotification(AdhanContract.PRE_ADHAN_NOTIFICATION_ID)
        val settings = settingsRepository.data.first()
        val alarmSettings = alarmSettingsRepository.data.first()

        val silencedUntil = settings.silencedUntilMillis ?: 0L
        if (Clock.System.now().toEpochMilliseconds() < silencedUntil) {
            postMissedNotification(prayer, timestamp, settings)
            return
        }

        try {
            val body = buildBody(timestamp, settings, alarmSettings)
            val entry = if (playSound) resolveSound(settings, prayer) else null
            val soundUri = entry?.toAudioUri(context)
            val vibration = alarmSettings.getVibrationSettings(prayer) ?: alarmSettings.vibrationMode

            val intrusive = vibration == VibrationMode.Continuous ||
                (soundUri != null && audioDurationProbe.isIntrusive(entry))
            if (soundUri != null && intrusive) {

                if (CallStateInspector.isCallActive(context)) {
                    val callBody = localizedResources.current.getString(
                        R.string.missed_during_call_body,
                        settings.formatTime(timestamp),
                    )
                    postNotifyOnlyNotification(prayer, settings.formatTime(timestamp), callBody, settings)
                } else {
                    playbackLauncher.launch(
                        PlaybackRequest.from(
                            settings = settings,
                            alarmSettings = alarmSettings,
                            title = localizedResources.current.getString(prayer.stringRes),
                            body = body,
                            timeLabel = settings.formatTime(timestamp),
                            soundUri = soundUri,
                            channelId = adhanChannel(settings),
                            loop = entry.loop,
                            vibration = vibration,
                            prayerName = prayer.name,
                        ),
                    )
                }
            } else {

                postNotifyOnlyNotification(
                    prayer,
                    settings.formatTime(timestamp),
                    body,
                    settings,
                    stoppableSound = soundUri != null,
                )
                if (soundUri != null) {
                    if (vibration != VibrationMode.Off) VibrationController.vibrate(context, VibrationMode.Once)
                    softSoundPlayer.play(soundUri, stopOnVolumeButton = settings.volumeButtonStopsAdhan)
                }
            }
        } finally {
            adhanScheduler.schedule()
        }
    }

    private suspend fun postMissedNotification(
        prayer: Prayer,
        timestamp: Long,
        settings: Settings,
    ) {
        notificationRepository.notify(
            missedNotificationConfig(
                id = "missed_adhan_${prayer.name}",
                title = TextResource.StringResId(prayer.stringRes),
                body = TextResource.StringResIdWithArgs(
                    R.string.missed_during_silence_body,
                    settings.formatTime(timestamp),
                ),
            ),
        )
    }

    private suspend fun buildBody(
        timestamp: Long,
        settings: Settings,
        alarmSettings: AlarmSettings,
    ): String? {
        if (!alarmSettings.showNextPrayerTime) return null
        val next = nextAfter(timestamp, settings, alarmSettings) ?: return null
        return "${localizedResources.current.getString(R.string.next_prayer_label)}: " +
            "${localizedResources.current.getString(next.prayer.stringRes)}, " +
            settings.formatTime(next.prayerTime.toEpochMilliseconds())
    }

    private suspend fun nextAfter(
        timestamp: Long,
        settings: Settings,
        alarmSettings: AlarmSettings,
    ): ShariaTimeDetails? =
        runCatching {
            val calc = calculationSettingsRepository.data.first()
            val params = calc.parameters ?: return null
            val location = favoriteLocationsRepository.data.first()
                .firstOrNull { it.id == calc.locationId }?.locationDetail ?: return null
            getNextShariaTimesUseCase(
                instant = Instant.fromEpochMilliseconds(timestamp + 1_000),
                calculationParameters = params,
                calculationAdjustments = calc.calculationAdjustments,
                arabicCalendar = settings.selectedArabicCalendar,
                locationDetail = location,
                alarmSettings = alarmSettings,
            )
        }.getOrNull()

    suspend fun onDismissAndSilent(minutes: Int) {
        PlaybackService.stop(context)
        notificationRepository.cancelNotification(AdhanContract.ADHAN_NOTIFICATION_ID)
        dndSilenceController.silence(minutes)
        alarmRepository.cancel(AdhanContract.ADHAN_ALARM_ID)
        alarmRepository.cancel(AdhanContract.PRE_ADHAN_ALARM_ID)
        adhanScheduler.schedule()
    }

    fun dismissAndSilentFromUi(minutes: Int) {
        uiScope.launch { onDismissAndSilent(minutes) }
    }

    suspend fun onUnsilence() {
        dndSilenceController.unsilence()
        adhanScheduler.schedule()
    }

    suspend fun onPreAdhanFired(
        prayer: Prayer,
        timestamp: Long,
    ) {

        val deliveredForMs = settingsRepository.data.first()
            .deliveredAlarmTimestamps[AdhanContract.PRE_ADHAN_NOTIFICATION_ID]
        if (deliveredForMs == timestamp) return

        settingsRepository.markDelivered(AdhanContract.PRE_ADHAN_NOTIFICATION_ID, timestamp)
        postUpcomingNotification(prayer, timestamp)
    }

    private suspend fun postUpcomingNotification(
        prayer: Prayer,
        timestamp: Long,
    ) {
        val settings = settingsRepository.data.first()
        val prayerName = localizedResources.current.getString(prayer.stringRes)
        notificationRepository.notify(
            NotificationConfig(
                id = AdhanContract.PRE_ADHAN_NOTIFICATION_ID,
                title = TextResource.StringResId(R.string.upcoming_alarm_title),
                body = TextResource.Literal("$prayerName, ${settings.formatTime(timestamp)}"),
                android = AndroidNotificationConfig(
                    channelId = EnsureNotificationChannelsUseCase.PRE_ADHAN_CHANNEL_ID,
                    category = AndroidNotificationCategory.CATEGORY_ALARM,
                    autoCancel = true,

                    onlyAlertOnce = true,

                    pressAction = NotificationPressAction.Route(Route.Main.UpcomingAlarms),
                    actions = listOf(
                        NotificationButton(
                            title = TextResource.StringResId(R.string.cancel_alarm),
                            pressAction = NotificationPressAction.Broadcast(
                                action = AdhanContract.ACTION_CANCEL_ADHAN,
                                requestCode = AdhanContract.ACTION_CANCEL_ADHAN.hashCode(),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }

    suspend fun onCancelAdhan() {
        val scheduled = alarmRepository.getScheduled()
            .firstOrNull { it.id == AdhanContract.ADHAN_ALARM_ID }
        val prayer = scheduled?.extras?.get(AdhanContract.EXTRA_PRAYER)
            ?.let { runCatching { Prayer.valueOf(it) }.getOrNull() }
        if (scheduled != null && prayer != null) {
            val entry = SkippedAlarm.Adhan(
                prayer = prayer,
                date = Instant.fromEpochMilliseconds(scheduled.triggerAtMillis).toLocalDate(),
            )
            settingsRepository.update { it.copy(skippedOccurrences = it.skippedOccurrences.upsert(entry)) }
        }
        notificationRepository.cancelNotification(AdhanContract.ADHAN_NOTIFICATION_ID)
        notificationRepository.cancelNotification(AdhanContract.PRE_ADHAN_NOTIFICATION_ID)
        PlaybackService.stop(context)
        adhanScheduler.schedule()
        val message = if (prayer != null) {
            localizedResources.current.getString(
                R.string.adhan_cancelled_named_toast,
                localizedResources.current.getString(prayer.stringRes),
            )
        } else {
            localizedResources.current.getString(R.string.adhan_cancelled_toast)
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun onRemindLater(
        prayer: Prayer,
        minutes: Int,
    ) {
        PlaybackService.stop(context)
        notificationRepository.cancelNotification(AdhanContract.ADHAN_NOTIFICATION_ID)
        val fireAt = Clock.System.now().toEpochMilliseconds() + minutes * 60_000L
        alarmRepository.schedule(
            ScheduledAlarm(
                id = AdhanContract.REMIND_ALARM_ID,
                triggerAtMillis = fireAt,
                action = AdhanContract.ACTION_ADHAN_REMIND,
                type = AlarmType.AlarmClock,
                extras = mapOf(
                    AdhanContract.EXTRA_PRAYER to prayer.name,
                    AdhanContract.EXTRA_REMIND_MINUTES to minutes.toString(),
                ),
            ),
        )
    }

    suspend fun onAdhanRemindFired(
        prayer: Prayer,
        minutes: Int,
    ) {
        notificationRepository.notify(
            NotificationConfig(
                id = AdhanContract.REMIND_NOTIFICATION_ID,
                title = TextResource.StringResId(R.string.reminder),
                body = TextResource.StringResIdWithArgs(
                    R.string.adhan_remind_body,
                    TextResource.StringResId(prayer.stringRes),
                    minutes,
                ),
                android = AndroidNotificationConfig(
                    channelId = EnsureNotificationChannelsUseCase.ADHAN_REMIND_CHANNEL_ID,
                    category = AndroidNotificationCategory.CATEGORY_REMINDER,
                    autoCancel = true,
                ),
            ),
        )
    }

    fun onDismiss() {
        PlaybackService.stop(context)
        uiScope.launch { notificationRepository.cancelNotification(AdhanContract.ADHAN_NOTIFICATION_ID) }
    }

    fun dismissFromUi(
        autoSilent: Boolean,
        minutes: Int,
    ) {
        uiScope.launch {
            if (!autoSilent) {
                onDismiss()
                return@launch
            }
            val nm = context.getSystemService<NotificationManager>()
            if (nm != null && nm.isNotificationPolicyAccessGranted) {
                onDismissAndSilent(minutes)
            } else {
                onDismiss()
                notifyDndRevoked()
            }
        }
    }

    private suspend fun notifyDndRevoked() {
        notificationRepository.notify(
            NotificationConfig(
                id = AdhanContract.DND_REVOKED_NOTIFICATION_ID,
                title = TextResource.StringResId(R.string.adhan_dnd_revoked_title),
                body = TextResource.StringResId(R.string.adhan_dnd_revoked_body),
                android = AndroidNotificationConfig(
                    channelId = EnsureNotificationChannelsUseCase.PERMISSION_REVOKED_CHANNEL_ID,
                    autoCancel = true,
                ),
            ),
        )
    }

    fun remindLaterFromUi(
        prayer: Prayer,
        minutes: Int,
    ) {
        uiScope.launch { onRemindLater(prayer, minutes) }
    }

    suspend fun devScheduleAdhan(
        playSound: Boolean,
        delaySeconds: Int,
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        val fireAt = now + delaySeconds * 1000L
        val prayer = DEV_TEST_PRAYER
        alarmRepository.schedule(
            ScheduledAlarm(
                id = AdhanContract.DEV_TEST_ALARM_ID,
                triggerAtMillis = fireAt,
                action = AdhanContract.ACTION_ADHAN,
                type = AlarmType.AlarmClock,
                extras = mapOf(
                    AdhanContract.EXTRA_PRAYER to prayer.name,
                    AdhanContract.EXTRA_PLAY_SOUND to playSound.toString(),
                    AdhanContract.EXTRA_TIMESTAMP to fireAt.toString(),
                ),
            ),
        )
    }

    suspend fun devFireNow() {
        onAdhanFired(DEV_TEST_PRAYER, playSound = true, timestamp = Clock.System.now().toEpochMilliseconds())
    }

    suspend fun devPostUpcoming() {

        postUpcomingNotification(DEV_TEST_PRAYER, Clock.System.now().toEpochMilliseconds())
    }

    private suspend fun postNotifyOnlyNotification(
        prayer: Prayer,
        subtitle: String,
        body: String?,
        settings: Settings,
        stoppableSound: Boolean = false,
    ) {
        notificationRepository.notify(
            NotificationConfig(
                id = AdhanContract.ADHAN_NOTIFICATION_ID,
                title = TextResource.StringResId(prayer.stringRes),
                subtitle = TextResource.Literal(subtitle),
                body = body?.let { TextResource.Literal(it) },
                android = AndroidNotificationConfig(
                    channelId = adhanChannel(settings),
                    category = AndroidNotificationCategory.CATEGORY_ALARM,
                    autoCancel = true,
                    showTimestamp = false,
                    dismissAction = SoftSoundContract
                        .stopAction(AdhanContract.ADHAN_NOTIFICATION_ID)
                        .takeIf { stoppableSound },
                ),
            ),
        )
    }

    private fun adhanChannel(settings: Settings): String =
        if (settings.bypassDnd) {
            EnsureNotificationChannelsUseCase.ADHAN_DND_CHANNEL_ID
        } else {
            EnsureNotificationChannelsUseCase.ADHAN_CHANNEL_ID
        }

    private fun resolveSound(
        settings: Settings,
        prayer: Prayer,
    ): AudioEntry? =
        settings.selectedAdhanEntries[prayer.toAdhanKey()]?.takeIf { it.isResolvable() }
            ?: settings.selectedAdhanEntries[AdhanKey.Default]?.takeIf { it.isResolvable() }
            ?: settings.savedAdhanAudioEntries.firstOrNull()
}
