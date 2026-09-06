package com.alhuda.app.alarm

import com.alhuda.app.R
import com.alhuda.app.adhan.AdhanContract
import com.alhuda.app.core.data.locale.LocalizedResources
import com.alhuda.app.core.domain.model.TextResource
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.repository.AlarmRepository
import com.alhuda.app.core.domain.repository.NotificationRepository
import com.alhuda.app.core.domain.repository.ReminderRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.util.formatMissedWhen
import com.alhuda.app.core.presentation.mapper.displayName
import com.alhuda.app.playback.missedNotificationConfig
import com.alhuda.app.reminder.ReminderContract
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class MissedAlarmCatchUp @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderRepository: ReminderRepository,
    private val notificationRepository: NotificationRepository,
    private val localizedResources: LocalizedResources,
) {
    private companion object {
        const val MISSED_WINDOW_NOTICE_ID = "missed_window_notice"

        const val LONG_OUTAGE_THRESHOLD_MS = 4 * 60 * 60 * 1000L
    }

    suspend fun catchUpMissed() {
        val now = Clock.System.now().toEpochMilliseconds()
        val settings = settingsRepository.data.first()
        val delivered = settings.deliveredAlarmTimestamps

        var oldestMissed: Long? = null
        alarmRepository.getScheduled()
            .filter { it.triggerAtMillis <= now }
            .forEach { alarm ->
                when (alarm.action) {
                    AdhanContract.ACTION_ADHAN -> {

                        if ((delivered[AdhanContract.ADHAN_NOTIFICATION_ID] ?: 0L) >= alarm.triggerAtMillis) return@forEach
                        val prayer = alarm.extras[AdhanContract.EXTRA_PRAYER]
                            ?.let { runCatching { Prayer.valueOf(it) }.getOrNull() } ?: return@forEach
                        notificationRepository.notify(
                            missedNotificationConfig(
                                id = "missed_adhan_${prayer.name}",
                                title = TextResource.StringResId(prayer.stringRes),
                                body = TextResource.StringResIdWithArgs(
                                    R.string.missed_while_off_body,
                                    settings.formatMissedWhen(alarm.triggerAtMillis, now),
                                ),
                            ),
                        )
                        oldestMissed = minOf(oldestMissed ?: Long.MAX_VALUE, alarm.triggerAtMillis)

                        settingsRepository.markDelivered(AdhanContract.ADHAN_NOTIFICATION_ID, alarm.triggerAtMillis)
                    }

                    ReminderContract.ACTION_REMINDER -> {
                        val reminderId = alarm.extras[ReminderContract.EXTRA_REMINDER_ID] ?: return@forEach
                        val key = ReminderContract.notificationId(reminderId)
                        if ((delivered[key] ?: 0L) >= alarm.triggerAtMillis) return@forEach
                        val reminder = reminderRepository.data.first().firstOrNull { it.id == reminderId }
                        val title = reminder?.displayName(localizedResources.current)
                            ?: localizedResources.current.getString(R.string.reminder)
                        notificationRepository.notify(
                            missedNotificationConfig(
                                id = "missed_$key",
                                title = TextResource.Literal(title),
                                body = TextResource.StringResIdWithArgs(
                                    R.string.missed_while_off_body,
                                    settings.formatMissedWhen(alarm.triggerAtMillis, now),
                                ),
                            ),
                        )
                        oldestMissed = minOf(oldestMissed ?: Long.MAX_VALUE, alarm.triggerAtMillis)
                        settingsRepository.markDelivered(key, alarm.triggerAtMillis)
                    }
                }
            }

        val oldest = oldestMissed
        if (oldest != null && now - oldest > LONG_OUTAGE_THRESHOLD_MS) {
            notificationRepository.notify(
                missedNotificationConfig(
                    id = MISSED_WINDOW_NOTICE_ID,
                    title = TextResource.StringResId(R.string.missed_window_title),
                    body = TextResource.StringResId(R.string.missed_window_body),
                ),
            )
        }
    }
}
