package com.alhuda.app.adhan

import android.util.Log
import com.alhuda.app.core.data.audio.AudioDurationProbe
import com.alhuda.app.core.domain.model.adhan.AdhanKey
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.adhan.SHARIA_TIMES_IN_ORDER
import com.alhuda.app.core.domain.model.adhan.toAdhanKey
import com.alhuda.app.core.domain.model.alarm.AlarmSchedulingDefaults
import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.alarm.ScheduledAlarm
import com.alhuda.app.core.domain.model.alarm.SkippedAlarm
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import com.alhuda.app.core.domain.model.alarm.isAdhanSkipped
import com.alhuda.app.core.domain.model.alarm.prunePastDays
import com.alhuda.app.core.domain.repository.AlarmRepository
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.NotificationRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.usecase.GetNextShariaTimesUseCase
import com.alhuda.app.core.domain.usecase.ShariaTimeDetails
import com.alhuda.app.core.domain.util.toLocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Instant

@Singleton
class AdhanScheduler @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val getNextShariaTimesUseCase: GetNextShariaTimesUseCase,
    private val alarmRepository: AlarmRepository,
    private val notificationRepository: NotificationRepository,
    private val audioDurationProbe: AudioDurationProbe,
) {
    private val mutex = Mutex()

    private var lastSignature: Pair<Prayer, Long>? = null

    private companion object {
        const val TAG = "AdhanScheduler"
    }

    data class Outcome(
        val next: ShariaTimeDetails,
        val changed: Boolean,
    )

    suspend fun schedule(): Outcome? =
        mutex.withLock {
            val settings = settingsRepository.data.first()
            val alarmSettings = alarmSettingsRepository.data.first()
            val calc = calculationSettingsRepository.data.first()
            val parameters = calc.parameters
            val location = favoriteLocationsRepository.data.first()
                .firstOrNull { it.id == calc.locationId }?.locationDetail

            if (parameters == null || location == null || !alarmSettings.hasAnyNotification()) {
                cancelAll()
                lastSignature = null
                return@withLock null
            }

            val nowMs = Clock.System.now().toEpochMilliseconds()
            val deliveredMs = settings.deliveredAlarmTimestamps[AdhanContract.ADHAN_NOTIFICATION_ID] ?: 0L
            val silencedUntilMs = settings.silencedUntilMillis ?: 0L

            val today = Instant.fromEpochMilliseconds(nowMs).toLocalDate()
            val livePruned = settings.skippedOccurrences.prunePastDays<SkippedAlarm.Adhan>(today)
            if (livePruned.size != settings.skippedOccurrences.size) {
                settingsRepository.update { it.copy(skippedOccurrences = livePruned) }
            }
            val fromMs = maxOf(nowMs, deliveredMs + AlarmSchedulingDefaults.REFIRE_GUARD_MS, silencedUntilMs)

            val notifyOnSkip = alarmSettings.notifyOnSkippedAdhan
            val next = getNextShariaTimesUseCase(
                instant = Instant.fromEpochMilliseconds(fromMs),
                calculationParameters = parameters,
                calculationAdjustments = calc.calculationAdjustments,
                arabicCalendar = settings.selectedArabicCalendar,
                locationDetail = location,
                alarmSettings = alarmSettings,
                isSkipped = { prayer, prayerTime ->
                    !notifyOnSkip && livePruned.isAdhanSkipped(prayer, prayerTime.toLocalDate())
                },
            )
            if (next == null) {
                cancelAll()
                lastSignature = null
                return@withLock null
            }

            val prayerTimeMs = next.prayerTime.toEpochMilliseconds()
            val signature = next.prayer to prayerTimeMs
            val changed = signature != lastSignature
            lastSignature = signature
            val alarmType = AlarmSchedulingDefaults.alarmType(settings.useDifferentAlarmType)

            val silentSkip = notifyOnSkip && livePruned.isAdhanSkipped(next.prayer, next.prayerTime.toLocalDate())
            val playSound = next.sound && !silentSkip
            Log.i(TAG, "Next adhan ${next.prayer} in ${(prayerTimeMs - nowMs) / 1000}s (sound=$playSound)")

            val soundEntry = settings.selectedAdhanEntries[next.prayer.toAdhanKey()]
                ?: settings.selectedAdhanEntries[AdhanKey.Default]
                ?: settings.savedAdhanAudioEntries.firstOrNull()
            val vibration = alarmSettings.getVibrationSettings(next.prayer) ?: alarmSettings.vibrationMode
            val intrusive = playSound && (
                vibration == VibrationMode.Continuous ||
                    (soundEntry != null && audioDurationProbe.isIntrusive(soundEntry))
                )

            alarmRepository.schedule(
                ScheduledAlarm(
                    id = AdhanContract.ADHAN_ALARM_ID,
                    triggerAtMillis = prayerTimeMs,
                    action = AdhanContract.ACTION_ADHAN,
                    type = alarmType,
                    extras = mapOf(
                        AdhanContract.EXTRA_PRAYER to next.prayer.name,
                        AdhanContract.EXTRA_PLAY_SOUND to playSound.toString(),
                        AdhanContract.EXTRA_TIMESTAMP to prayerTimeMs.toString(),
                        AdhanContract.EXTRA_INTRUSIVE to intrusive.toString(),
                    ),
                ),
            )

            val preDeliveredForMs = settings.deliveredAlarmTimestamps[AdhanContract.PRE_ADHAN_NOTIFICATION_ID]
            if (!intrusive || alarmSettings.dontNotifyUpcoming) {

                alarmRepository.cancel(AdhanContract.PRE_ADHAN_ALARM_ID)
                notificationRepository.cancelNotification(AdhanContract.PRE_ADHAN_NOTIFICATION_ID)
                if (preDeliveredForMs != null) settingsRepository.clearDelivered(AdhanContract.PRE_ADHAN_NOTIFICATION_ID)
            } else if (preDeliveredForMs != prayerTimeMs) {
                val preMs = (prayerTimeMs - alarmSettings.preAlarmMinutesBefore * 60_000L)
                    .coerceAtLeast(nowMs)
                alarmRepository.schedule(
                    ScheduledAlarm(
                        id = AdhanContract.PRE_ADHAN_ALARM_ID,
                        triggerAtMillis = preMs,
                        action = AdhanContract.ACTION_PRE_ADHAN,
                        type = alarmType,
                        extras = mapOf(
                            AdhanContract.EXTRA_PRAYER to next.prayer.name,
                            AdhanContract.EXTRA_TIMESTAMP to prayerTimeMs.toString(),
                        ),
                    ),
                )
            }

            Outcome(next, changed)
        }

    private suspend fun cancelAll() {
        alarmRepository.cancel(AdhanContract.ADHAN_ALARM_ID)
        alarmRepository.cancel(AdhanContract.PRE_ADHAN_ALARM_ID)
    }

    private fun AlarmSettings.hasAnyNotification(): Boolean = SHARIA_TIMES_IN_ORDER.any { getNotifSettings(it).selectedDays().isNotEmpty() }
}
