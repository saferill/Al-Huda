package com.alhuda.app.reminder

import android.util.Log
import com.alhuda.app.core.data.audio.AudioDurationProbe
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.alarm.AlarmSchedulingDefaults
import com.alhuda.app.core.domain.model.alarm.ScheduledAlarm
import com.alhuda.app.core.domain.model.alarm.SkippedAlarm
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import com.alhuda.app.core.domain.model.alarm.isReminderSkipped
import com.alhuda.app.core.domain.model.alarm.prunePastDays
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.domain.model.calculation.CalculationSettings
import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.model.reminder.ReminderAudioEntry
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.repository.AlarmRepository
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.NotificationRepository
import com.alhuda.app.core.domain.repository.ReminderRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.usecase.GetShariaTimesUseCase
import com.alhuda.app.core.domain.util.toLocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration

@Singleton
class ReminderScheduler @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val reminderRepository: ReminderRepository,
    private val getShariaTimesUseCase: GetShariaTimesUseCase,
    private val alarmRepository: AlarmRepository,
    private val notificationRepository: NotificationRepository,
    private val audioDurationProbe: AudioDurationProbe,
) {
    private val mutex = Mutex()

    private val lastSignatures = mutableMapOf<String, Long>()

    private companion object {
        const val TAG = "ReminderScheduler"
    }

    data class Outcome(
        val id: String,
        val label: String,
        val prayer: Prayer,
        val duration: Int,
        val durationModifier: Int,
        val fireTimeMs: Long,
        val changed: Boolean,
    )

    suspend fun schedule(): List<Outcome> =
        mutex.withLock {
            val settings = settingsRepository.data.first()
            val alarmSettings = alarmSettingsRepository.data.first()
            val calc = calculationSettingsRepository.data.first()
            val parameters = calc.parameters
            val location = favoriteLocationsRepository.data.first()
                .firstOrNull { it.id == calc.locationId }?.locationDetail
            val reminders = reminderRepository.data.first()

            val nowMs = Clock.System.now().toEpochMilliseconds()
            val today = Instant.fromEpochMilliseconds(nowMs).toLocalDate()
            val livePruned = settings.skippedOccurrences.prunePastDays<SkippedAlarm.Reminder>(today)
            if (livePruned.size != settings.skippedOccurrences.size) {
                settingsRepository.update { it.copy(skippedOccurrences = livePruned) }
            }

            val enabledIds = reminders.filter { it.enabled }.map { it.id }.toSet()
            alarmRepository.getScheduled()
                .map { it.id }
                .mapNotNull { id ->
                    when {
                        id.startsWith(ReminderContract.PRE_ALARM_ID_PREFIX) ->
                            id to id.removePrefix(ReminderContract.PRE_ALARM_ID_PREFIX)

                        id.startsWith(ReminderContract.ALARM_ID_PREFIX) ->
                            id to id.removePrefix(ReminderContract.ALARM_ID_PREFIX)

                        else -> null
                    }
                }
                .filter { (_, reminderId) -> reminderId !in enabledIds }
                .forEach { (id, _) -> alarmRepository.cancel(id) }

            if (parameters == null || location == null) {
                lastSignatures.clear()
                return@withLock emptyList()
            }

            val alarmType = AlarmSchedulingDefaults.alarmType(settings.useDifferentAlarmType)
            val outcomes = mutableListOf<Outcome>()
            val newSignatures = mutableMapOf<String, Long>()

            for (reminder in reminders) {
                if (!reminder.enabled) continue
                val deliveredMs = settings.deliveredAlarmTimestamps[ReminderContract.notificationId(reminder.id)] ?: 0L
                val fromMs = maxOf(nowMs, deliveredMs + AlarmSchedulingDefaults.REFIRE_GUARD_MS)

                val triggerMs = nextTriggerMs(reminder, fromMs, calc, settings, location, livePruned) ?: continue

                val changed = lastSignatures[reminder.id] != triggerMs
                newSignatures[reminder.id] = triggerMs
                outcomes += Outcome(
                    id = reminder.id,
                    label = reminder.label,
                    prayer = reminder.prayer,
                    duration = reminder.duration,
                    durationModifier = reminder.durationModifier,
                    fireTimeMs = triggerMs,
                    changed = changed,
                )

                val soundEntry = reminder.sound ?: ReminderAudioEntry.DefaultReminderAudioEntry
                val vibration = reminder.vibration ?: alarmSettings.vibrationMode
                val intrusive = vibration == VibrationMode.Continuous || audioDurationProbe.isIntrusive(soundEntry)

                Log.i(TAG, "Reminder ${reminder.id} (${reminder.prayer}) in ${(triggerMs - fromMs) / 1000}s")
                alarmRepository.schedule(
                    ScheduledAlarm(
                        id = ReminderContract.alarmId(reminder.id),
                        triggerAtMillis = triggerMs,
                        action = ReminderContract.ACTION_REMINDER,
                        type = alarmType,
                        extras = mapOf(
                            ReminderContract.EXTRA_REMINDER_ID to reminder.id,
                            ReminderContract.EXTRA_TIMESTAMP to triggerMs.toString(),
                            ReminderContract.EXTRA_INTRUSIVE to intrusive.toString(),
                        ),
                    ),
                )

                val preDeliveredForMs = settings.deliveredAlarmTimestamps[ReminderContract.preNotificationId(reminder.id)]
                if (!intrusive || alarmSettings.dontNotifyUpcoming) {

                    alarmRepository.cancel(ReminderContract.preAlarmId(reminder.id))
                    notificationRepository.cancelNotification(ReminderContract.preNotificationId(reminder.id))
                    if (preDeliveredForMs != null) settingsRepository.clearDelivered(ReminderContract.preNotificationId(reminder.id))
                } else if (preDeliveredForMs != triggerMs) {
                    val preMs = (triggerMs - alarmSettings.preAlarmMinutesBefore * 60_000L)
                        .coerceAtLeast(nowMs)
                    alarmRepository.schedule(
                        ScheduledAlarm(
                            id = ReminderContract.preAlarmId(reminder.id),
                            triggerAtMillis = preMs,
                            action = ReminderContract.ACTION_PRE_REMINDER,
                            type = alarmType,
                            extras = mapOf(
                                ReminderContract.EXTRA_REMINDER_ID to reminder.id,
                                ReminderContract.EXTRA_TIMESTAMP to triggerMs.toString(),
                            ),
                        ),
                    )
                }

            }

            lastSignatures.clear()
            lastSignatures.putAll(newSignatures)

            outcomes
        }

    private fun nextTriggerMs(
        reminder: Reminder,
        fromMs: Long,
        calc: CalculationSettings,
        settings: Settings,
        location: CalculationLocationDetail,
        skipped: List<SkippedAlarm>,
    ): Long? {
        val parameters = calc.parameters ?: return null
        val offsetMinutes = (reminder.duration * reminder.durationModifier).toDuration(DurationUnit.MINUTES)
        for (dayOffset in 0..AlarmSchedulingDefaults.SEARCH_DAYS) {
            val dayInstant = Instant.fromEpochMilliseconds(fromMs) + dayOffset.toDuration(DurationUnit.DAYS)
            val times = getShariaTimesUseCase(
                instant = dayInstant,
                calculationParameters = parameters,
                calculationAdjustments = calc.calculationAdjustments,
                arabicCalendar = settings.selectedArabicCalendar,
                locationDetail = location,
            )
            val trigger = times.forPrayer(reminder.prayer) + offsetMinutes
            val matchesDay = reminder.days?.shouldFireFor(trigger) ?: true
            val isSkipped = skipped.isReminderSkipped(reminder.id, trigger.toLocalDate())
            if (matchesDay && !isSkipped && trigger.toEpochMilliseconds() >= fromMs) {
                return trigger.toEpochMilliseconds()
            }
        }
        return null
    }
}
