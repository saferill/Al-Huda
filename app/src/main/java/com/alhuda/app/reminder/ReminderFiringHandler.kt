package com.alhuda.app.reminder

import android.content.Context
import android.widget.Toast
import com.alhuda.app.R
import com.alhuda.app.alarm.DndSilenceController
import com.alhuda.app.core.data.audio.AudioDurationProbe
import com.alhuda.app.core.data.audio.SoftSoundPlayer
import com.alhuda.app.core.data.audio.toAudioUri
import com.alhuda.app.core.data.locale.LocalizedResources
import com.alhuda.app.core.domain.model.TextResource
import com.alhuda.app.core.domain.model.adhan.AdhanKey
import com.alhuda.app.core.domain.model.alarm.SkippedAlarm
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import com.alhuda.app.core.domain.model.alarm.upsert
import com.alhuda.app.core.domain.model.notification.AndroidNotificationCategory
import com.alhuda.app.core.domain.model.notification.AndroidNotificationConfig
import com.alhuda.app.core.domain.model.notification.NotificationButton
import com.alhuda.app.core.domain.model.notification.NotificationConfig
import com.alhuda.app.core.domain.model.notification.NotificationPressAction
import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.model.reminder.ReminderAudioEntry
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.model.settings.isResolvable
import com.alhuda.app.core.domain.repository.AlarmRepository
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.NotificationRepository
import com.alhuda.app.core.domain.repository.ReminderRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.usecase.EnsureNotificationChannelsUseCase
import com.alhuda.app.core.domain.util.formatTime
import com.alhuda.app.core.domain.util.toLocalDate
import com.alhuda.app.core.presentation.mapper.displayName
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
class ReminderFiringHandler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val reminderRepository: ReminderRepository,
    private val notificationRepository: NotificationRepository,
    private val alarmRepository: AlarmRepository,
    private val reminderScheduler: ReminderScheduler,
    private val playbackLauncher: PlaybackLauncher,
    private val audioDurationProbe: AudioDurationProbe,
    private val softSoundPlayer: SoftSoundPlayer,
    private val dndSilenceController: DndSilenceController,
    private val localizedResources: LocalizedResources,
) {
    private val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun dismissFromUi() {
        uiScope.launch { PlaybackService.stop(context) }
    }

    fun dismissAndSilentFromUi(minutes: Int) {
        uiScope.launch {
            PlaybackService.stop(context)
            dndSilenceController.silence(minutes)
        }
    }

    suspend fun devFireNow() {
        val settings = settingsRepository.data.first()
        val alarmSettings = alarmSettingsRepository.data.first()
        val title = localizedResources.current.getString(R.string.reminder)
        val timeLabel = settings.formatTime(Clock.System.now().toEpochMilliseconds())
        val soundEntry = settings.selectedAdhanEntries[AdhanKey.Default]?.takeIf { it.isResolvable() }
            ?: settings.savedAdhanAudioEntries.firstOrNull()
        val soundUri = soundEntry?.toAudioUri(context) ?: return
        playbackLauncher.launch(
            PlaybackRequest.from(
                settings = settings,
                alarmSettings = alarmSettings,
                title = title,
                body = timeLabel,
                timeLabel = timeLabel,
                soundUri = soundUri,
                channelId = reminderChannel(settings),
                loop = soundEntry.loop,
                vibration = alarmSettings.vibrationMode,
                header = localizedResources.current.getString(R.string.reminder),
                isReminder = true,
            ),
        )
    }

    suspend fun onReminderFired(
        reminderId: String,
        timestamp: Long,
    ) {

        val deliveredForMs = settingsRepository.data.first()
            .deliveredAlarmTimestamps[ReminderContract.notificationId(reminderId)]
        if (deliveredForMs == timestamp) return

        settingsRepository.markDelivered(ReminderContract.notificationId(reminderId), timestamp)
        notificationRepository.cancelNotification(ReminderContract.preNotificationId(reminderId))
        val reminder = reminderRepository.data.first().firstOrNull { it.id == reminderId }
        if (reminder == null || !reminder.enabled) {
            reminderScheduler.schedule()
            return
        }
        val settings = settingsRepository.data.first()
        val alarmSettings = alarmSettingsRepository.data.first()

        val title = reminder.displayName(localizedResources.current)
        val timeLabel = settings.formatTime(timestamp)

        val silencedUntil = settings.silencedUntilMillis ?: 0L
        if (Clock.System.now().toEpochMilliseconds() < silencedUntil) {
            postMissedNotification(reminderId, title, timeLabel)
        } else {
            val soundEntry = reminder.sound ?: ReminderAudioEntry.DefaultReminderAudioEntry
            val soundUri = soundEntry.toAudioUri(context)
            val vibration = reminder.vibration ?: alarmSettings.vibrationMode

            val intrusive = vibration == VibrationMode.Continuous ||
                (soundUri != null && audioDurationProbe.isIntrusive(soundEntry))
            if (soundUri != null && intrusive) {

                if (CallStateInspector.isCallActive(context)) {
                    val callBody = localizedResources.current.getString(
                        R.string.missed_during_call_body,
                        timeLabel,
                    )
                    postNotifyOnlyNotification(reminderId, title, timeLabel, callBody, settings)
                } else {
                    playbackLauncher.launch(
                        PlaybackRequest.from(
                            settings = settings,
                            alarmSettings = alarmSettings,
                            title = title,
                            body = null,
                            timeLabel = timeLabel,
                            soundUri = soundUri,
                            channelId = reminderChannel(settings),
                            loop = soundEntry.loop,
                            vibration = vibration,
                            header = localizedResources.current.getString(R.string.reminder),
                            isReminder = true,
                        ),
                    )
                }
            } else {

                postNotifyOnlyNotification(
                    reminderId,
                    title,
                    timeLabel,
                    null,
                    settings,
                    stoppableSound = soundUri != null,
                )
                if (soundUri != null) {
                    if (vibration != VibrationMode.Off) VibrationController.vibrate(context, VibrationMode.Once)
                    softSoundPlayer.play(soundUri, stopOnVolumeButton = settings.volumeButtonStopsAdhan)
                }
            }
        }

        if (reminder.once == true) {
            reminderRepository.update { list ->
                list.map { if (it.id == reminderId) it.copy(enabled = false) else it }
            }
        }
        reminderScheduler.schedule()
    }

    suspend fun onPreReminderFired(
        reminderId: String,
        timestamp: Long,
    ) {

        val deliveredForMs = settingsRepository.data.first()
            .deliveredAlarmTimestamps[ReminderContract.preNotificationId(reminderId)]
        if (deliveredForMs == timestamp) return
        val reminder = reminderRepository.data.first().firstOrNull { it.id == reminderId } ?: return
        if (!reminder.enabled) return

        settingsRepository.markDelivered(ReminderContract.preNotificationId(reminderId), timestamp)
        postUpcomingNotification(reminderId, reminder, timestamp)
    }

    private suspend fun postUpcomingNotification(
        reminderId: String,
        reminder: Reminder,
        timestamp: Long,
    ) {
        val settings = settingsRepository.data.first()
        val title = reminder.displayName(localizedResources.current)
        val timeLabel = settings.formatTime(timestamp)
        notificationRepository.notify(
            NotificationConfig(
                id = ReminderContract.preNotificationId(reminderId),
                title = TextResource.StringResId(R.string.upcoming_reminder_title),
                body = TextResource.Literal("$title, $timeLabel"),
                android = AndroidNotificationConfig(
                    channelId = EnsureNotificationChannelsUseCase.PRE_REMINDER_CHANNEL_ID,
                    category = AndroidNotificationCategory.CATEGORY_REMINDER,
                    autoCancel = true,

                    onlyAlertOnce = true,

                    pressAction = NotificationPressAction.Route(Route.Main.UpcomingAlarms),
                    actions = listOf(
                        NotificationButton(
                            title = TextResource.StringResId(R.string.cancel_alarm),
                            pressAction = NotificationPressAction.Broadcast(
                                action = ReminderContract.ACTION_CANCEL_REMINDER,
                                requestCode = ReminderContract.ACTION_CANCEL_REMINDER.hashCode() xor reminderId.hashCode(),
                                extras = mapOf(ReminderContract.EXTRA_REMINDER_ID to reminderId),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }

    suspend fun onCancelReminder(reminderId: String) {
        val scheduledTs = alarmRepository.getScheduled()
            .firstOrNull { it.id == ReminderContract.alarmId(reminderId) }?.triggerAtMillis
        val reminder = reminderRepository.data.first().firstOrNull { it.id == reminderId }
        if (scheduledTs != null) {
            val entry = SkippedAlarm.Reminder(
                reminderId = reminderId,
                date = Instant.fromEpochMilliseconds(scheduledTs).toLocalDate(),
            )
            settingsRepository.update { it.copy(skippedOccurrences = it.skippedOccurrences.upsert(entry)) }
        }
        notificationRepository.cancelNotification(ReminderContract.notificationId(reminderId))
        notificationRepository.cancelNotification(ReminderContract.preNotificationId(reminderId))
        PlaybackService.stop(context)
        reminderScheduler.schedule()

        val name = reminder?.displayName(localizedResources.current) ?: localizedResources.current.getString(R.string.reminder)
        withContext(Dispatchers.Main) {
            Toast.makeText(context, localizedResources.current.getString(R.string.reminder_cancelled_toast, name), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private suspend fun postMissedNotification(
        reminderId: String,
        title: String,
        timeLabel: String,
    ) {
        notificationRepository.notify(
            missedNotificationConfig(
                id = "missed_${ReminderContract.notificationId(reminderId)}",
                title = TextResource.Literal(title),
                body = TextResource.StringResIdWithArgs(R.string.missed_during_silence_body, timeLabel),
            ),
        )
    }

    private suspend fun postNotifyOnlyNotification(
        reminderId: String,
        title: String,
        subtitle: String,
        body: String?,
        settings: Settings,
        stoppableSound: Boolean = false,
    ) {
        val notificationId = ReminderContract.notificationId(reminderId)
        notificationRepository.notify(
            NotificationConfig(
                id = notificationId,
                title = TextResource.Literal(title),
                subtitle = TextResource.Literal(subtitle),
                body = body?.let { TextResource.Literal(it) },
                android = AndroidNotificationConfig(
                    channelId = reminderChannel(settings),
                    category = AndroidNotificationCategory.CATEGORY_ALARM,
                    autoCancel = true,
                    showTimestamp = false,
                    dismissAction = SoftSoundContract.stopAction(notificationId).takeIf { stoppableSound },
                ),
            ),
        )
    }

    private fun reminderChannel(settings: Settings): String =
        if (settings.bypassDnd) {
            EnsureNotificationChannelsUseCase.REMINDER_DND_CHANNEL_ID
        } else {
            EnsureNotificationChannelsUseCase.REMINDER_CHANNEL_ID
        }
}
