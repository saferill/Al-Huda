package com.alhuda.app.di

import com.alhuda.app.core.data.locale.LocalizedResources
import com.alhuda.app.core.domain.model.alarm.SkippedAlarm
import com.alhuda.app.core.domain.model.calculation.CalculationAdjustments
import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.ReminderRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.util.formatRescheduleWhen
import com.alhuda.app.core.presentation.feedback.ScheduleFeedback
import com.alhuda.app.core.presentation.feedback.ScheduleFeedbackInfo
import com.alhuda.app.reminder.ReminderScheduler
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
class ReminderSyncInitializer @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val scheduleFeedback: ScheduleFeedback,
    private val localizedResources: LocalizedResources,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @OptIn(ExperimentalAtomicApi::class)
    private val started = AtomicBoolean(false)

    private data class ReminderSyncKey(
        val reminders: List<Reminder>,
        val parameters: CalculationParameters?,
        val calculationAdjustments: CalculationAdjustments,
        val locationId: String?,
        val locationLat: Double?,
        val locationLong: Double?,
        val arabicCalendar: String,
        val useDifferentAlarmType: Boolean,

        val skippedReminders: List<SkippedAlarm.Reminder>,
    )

    @OptIn(ExperimentalAtomicApi::class)
    fun start() {
        if (!started.compareAndSet(expectedValue = false, newValue = true)) return

        scope.launch {
            combine(
                settingsRepository.data,
                calculationSettingsRepository.data,
                favoriteLocationsRepository.data,
                reminderRepository.data,
            ) { settings, calc, locations, reminders ->
                val location = locations.firstOrNull { it.id == calc.locationId }?.locationDetail
                ReminderSyncKey(
                    reminders = reminders,
                    parameters = calc.parameters,
                    calculationAdjustments = calc.calculationAdjustments,
                    locationId = calc.locationId,
                    locationLat = location?.lat,
                    locationLong = location?.long,
                    arabicCalendar = settings.selectedArabicCalendar,
                    useDifferentAlarmType = settings.useDifferentAlarmType,
                    skippedReminders = settings.skippedOccurrences.filterIsInstance<SkippedAlarm.Reminder>(),
                )
            }
                .distinctUntilChanged()
                .collectIndexed { index, _ ->
                    val outcomes = reminderScheduler.schedule()

                    if (index == 0) return@collectIndexed
                    val changed = outcomes.filter { it.changed }
                    when (changed.size) {
                        0 -> Unit

                        1 -> {
                            val outcome = changed.single()
                            val now = Clock.System.now().toEpochMilliseconds()
                            val settings = settingsRepository.data.first()
                            scheduleFeedback.notify(
                                ScheduleFeedbackInfo.Reminder(
                                    label = outcome.label,
                                    prayer = outcome.prayer,
                                    duration = outcome.duration,
                                    durationModifier = outcome.durationModifier,
                                    formattedTime = settings.formatRescheduleWhen(outcome.fireTimeMs, now, localizedResources.current),
                                ),
                            )
                        }

                        else -> scheduleFeedback.notify(ScheduleFeedbackInfo.ReminderBatch(changed.size))
                    }
                }
        }
    }
}
