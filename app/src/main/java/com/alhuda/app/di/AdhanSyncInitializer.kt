package com.alhuda.app.di

import com.alhuda.app.adhan.AdhanScheduler
import com.alhuda.app.core.data.locale.LocalizedResources
import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.alarm.SkippedAlarm
import com.alhuda.app.core.domain.model.calculation.CalculationAdjustments
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.util.formatRescheduleWhen
import com.alhuda.app.core.presentation.feedback.ScheduleFeedback
import com.alhuda.app.core.presentation.feedback.ScheduleFeedbackInfo
import io.github.meypod.adhan_kotlin.CalculationParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock

@Singleton
class AdhanSyncInitializer @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val adhanScheduler: AdhanScheduler,
    private val scheduleFeedback: ScheduleFeedback,
    private val localizedResources: LocalizedResources,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @OptIn(ExperimentalAtomicApi::class)
    private val started = AtomicBoolean(false)

    private data class AdhanSyncKey(
        val alarmSettings: AlarmSettings,
        val parameters: CalculationParameters?,
        val calculationAdjustments: CalculationAdjustments,
        val locationId: String?,
        val locationLat: Double?,
        val locationLong: Double?,
        val arabicCalendar: String,
        val useDifferentAlarmType: Boolean,

        val skippedAdhans: List<SkippedAlarm.Adhan>,
    )

    @OptIn(ExperimentalAtomicApi::class)
    fun start() {
        if (!started.compareAndSet(expectedValue = false, newValue = true)) return

        scope.launch {
            combine(
                settingsRepository.data,
                alarmSettingsRepository.data,
                calculationSettingsRepository.data,
                favoriteLocationsRepository.data,
            ) { settings, alarmSettings, calc, locations ->
                val location = locations.firstOrNull { it.id == calc.locationId }?.locationDetail
                AdhanSyncKey(
                    alarmSettings = alarmSettings,
                    parameters = calc.parameters,
                    calculationAdjustments = calc.calculationAdjustments,
                    locationId = calc.locationId,
                    locationLat = location?.lat,
                    locationLong = location?.long,
                    arabicCalendar = settings.selectedArabicCalendar,
                    useDifferentAlarmType = settings.useDifferentAlarmType,
                    skippedAdhans = settings.skippedOccurrences.filterIsInstance<SkippedAlarm.Adhan>(),
                )
            }
                .distinctUntilChanged()
                .collectIndexed { index, _ ->
                    val outcome = adhanScheduler.schedule()

                    if (index > 0 && outcome != null && outcome.changed) {
                        val now = Clock.System.now().toEpochMilliseconds()
                        val time = settingsRepository.data.first().formatRescheduleWhen(
                            outcome.next.prayerTime.toEpochMilliseconds(),
                            now,
                            localizedResources.current,
                        )
                        scheduleFeedback.notify(ScheduleFeedbackInfo.Adhan(outcome.next.prayer, time))
                    }
                }
        }
    }
}
