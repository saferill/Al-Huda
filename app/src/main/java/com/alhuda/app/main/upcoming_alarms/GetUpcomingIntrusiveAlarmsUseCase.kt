package com.alhuda.app.main.upcoming_alarms

import com.alhuda.app.core.data.audio.AudioDurationProbe
import com.alhuda.app.core.domain.model.adhan.AdhanKey
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.adhan.SHARIA_TIMES_IN_ORDER
import com.alhuda.app.core.domain.model.adhan.toAdhanKey
import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.alarm.SkippedAlarm
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import com.alhuda.app.core.domain.model.alarm.isAdhanSkipped
import com.alhuda.app.core.domain.model.alarm.isReminderSkipped
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.domain.model.calculation.CalculationSettings
import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.model.reminder.ReminderAudioEntry
import com.alhuda.app.core.domain.model.settings.AudioEntry
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.usecase.GetShariaTimesUseCase
import com.alhuda.app.core.domain.util.addDaysTimeZoneAware
import com.alhuda.app.core.domain.util.toLocalDate
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration

data class UpcomingOccurrence(

    val occurrence: SkippedAlarm,
    val isAdhan: Boolean,
    val prayer: Prayer,

    val reminder: Reminder?,
    val fireTimeMs: Long,
    val skipped: Boolean,
)

class GetUpcomingIntrusiveAlarmsUseCase @Inject constructor(
    private val getShariaTimesUseCase: GetShariaTimesUseCase,
    private val audioDurationProbe: AudioDurationProbe,
) {
    suspend operator fun invoke(
        nowMs: Long,
        settings: Settings,
        alarmSettings: AlarmSettings,
        calc: CalculationSettings,
        location: CalculationLocationDetail?,
        reminders: List<Reminder>,
    ): List<UpcomingOccurrence> {
        val parameters = calc.parameters ?: return emptyList()
        if (location == null) return emptyList()

        val now = Instant.fromEpochMilliseconds(nowMs)
        val skipped = settings.skippedOccurrences

        val adhanProbe = HashMap<AudioEntry, Boolean>()
        val reminderProbe = HashMap<ReminderAudioEntry, Boolean>()
        suspend fun isIntrusiveAudio(entry: AudioEntry) = adhanProbe.getOrPut(entry) { audioDurationProbe.isIntrusive(entry) }
        suspend fun isIntrusiveAudio(entry: ReminderAudioEntry) = reminderProbe.getOrPut(entry) { audioDurationProbe.isIntrusive(entry) }

        val result = mutableListOf<UpcomingOccurrence>()
        val seen = HashSet<SkippedAlarm>()

        for (dayOffset in -1..1) {
            val dayInstant = addDaysTimeZoneAware(now, dayOffset)
            val times = getShariaTimesUseCase(
                instant = dayInstant,
                calculationParameters = parameters,
                calculationAdjustments = calc.calculationAdjustments,
                arabicCalendar = settings.selectedArabicCalendar,
                locationDetail = location,
            )

            for (prayer in SHARIA_TIMES_IN_ORDER) {
                val fire = times.forPrayer(prayer)
                if (fire < now) continue
                if (!alarmSettings.getNotifSettings(prayer).shouldFireFor(fire)) continue
                if (!alarmSettings.getSoundSettings(prayer).shouldFireFor(fire)) continue
                val soundEntry = settings.selectedAdhanEntries[prayer.toAdhanKey()]
                    ?: settings.selectedAdhanEntries[AdhanKey.Default]
                    ?: settings.savedAdhanAudioEntries.firstOrNull()
                val vibration = alarmSettings.getVibrationSettings(prayer) ?: alarmSettings.vibrationMode
                val intrusive = vibration == VibrationMode.Continuous || (soundEntry != null && isIntrusiveAudio(soundEntry))
                if (!intrusive) continue

                val key = SkippedAlarm.Adhan(prayer, fire.toLocalDate())
                if (!seen.add(key)) continue
                result += UpcomingOccurrence(
                    occurrence = key,
                    isAdhan = true,
                    prayer = prayer,
                    reminder = null,
                    fireTimeMs = fire.toEpochMilliseconds(),
                    skipped = skipped.isAdhanSkipped(prayer, key.date),
                )
            }

            for (reminder in reminders) {
                if (!reminder.enabled) continue
                val offset = (reminder.duration * reminder.durationModifier).toDuration(DurationUnit.MINUTES)
                val fire = times.forPrayer(reminder.prayer) + offset
                if (fire < now) continue
                if (reminder.days?.shouldFireFor(fire) == false) continue
                val soundEntry = reminder.sound ?: ReminderAudioEntry.DefaultReminderAudioEntry
                val vibration = reminder.vibration ?: alarmSettings.vibrationMode
                val intrusive = vibration == VibrationMode.Continuous || isIntrusiveAudio(soundEntry)
                if (!intrusive) continue

                val key = SkippedAlarm.Reminder(reminder.id, fire.toLocalDate())
                if (!seen.add(key)) continue
                result += UpcomingOccurrence(
                    occurrence = key,
                    isAdhan = false,
                    prayer = reminder.prayer,
                    reminder = reminder,
                    fireTimeMs = fire.toEpochMilliseconds(),
                    skipped = skipped.isReminderSkipped(reminder.id, key.date),
                )
            }
        }

        return result.sortedBy { it.fireTimeMs }
    }
}
