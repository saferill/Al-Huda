package com.alhuda.app.main.upcoming_alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alhuda.app.adhan.AdhanContract
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.alarm.ScheduledAlarm
import com.alhuda.app.core.domain.model.alarm.SkippedAlarm
import com.alhuda.app.core.domain.model.alarm.upsert
import com.alhuda.app.core.domain.model.alarm.without
import com.alhuda.app.core.domain.model.calculation.CalculationSettings
import com.alhuda.app.core.domain.model.favorite_location.FavoriteLocation
import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.repository.AlarmRepository
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.NotificationRepository
import com.alhuda.app.core.domain.repository.ReminderRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.util.toLocalDate
import com.alhuda.app.reminder.ReminderContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

@HiltViewModel
class UpcomingAlarmsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val reminderRepository: ReminderRepository,
    private val notificationRepository: NotificationRepository,
    private val alarmRepository: AlarmRepository,
    private val getUpcomingIntrusiveAlarms: GetUpcomingIntrusiveAlarmsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UpcomingAlarmsUiState())
    val uiState = _uiState.asStateFlow()

    private data class Inputs(
        val settings: Settings,
        val alarmSettings: AlarmSettings,
        val calc: CalculationSettings,
        val favorites: List<FavoriteLocation>,
        val reminders: List<Reminder>,
    )

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.data,
                alarmSettingsRepository.data,
                calculationSettingsRepository.data,
                favoriteLocationsRepository.data,
                reminderRepository.data,
            ) { settings, alarmSettings, calc, favorites, reminders ->
                Inputs(settings, alarmSettings, calc, favorites, reminders)
            }.collectLatest { input ->
                val now = Clock.System.now().toEpochMilliseconds()
                val location = input.favorites.firstOrNull { it.id == input.calc.locationId }?.locationDetail

                val occurrences = withContext(Dispatchers.Default) {
                    getUpcomingIntrusiveAlarms(
                        nowMs = now,
                        settings = input.settings,
                        alarmSettings = input.alarmSettings,
                        calc = input.calc,
                        location = location,
                        reminders = input.reminders,
                    )
                }
                _uiState.update {
                    it.copy(
                        alarms = occurrences.map { occ -> occ.toUi() },
                        loading = false,
                        locale = input.settings.selectedLocale,
                        is24Hour = input.settings.is24HourFormat,
                        numberingSystem = input.settings.numberingSystem,
                        nowMs = now,
                        helpDismissed = input.settings.upcomingAlarmsHelpDismissed,
                    )
                }
            }
        }
    }

    fun onAction(action: UpcomingAlarmsUiAction) {
        when (action) {
            is UpcomingAlarmsUiAction.OnSkip -> onSkip(action.occurrence)
            is UpcomingAlarmsUiAction.OnReschedule -> onReschedule(action.occurrence)
            is UpcomingAlarmsUiAction.OnDismissHelp -> setHelpDismissed(true)
            is UpcomingAlarmsUiAction.OnShowHelp -> setHelpDismissed(false)
        }
    }

    private fun setHelpDismissed(dismissed: Boolean) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(upcomingAlarmsHelpDismissed = dismissed) }
        }
    }

    private fun onSkip(occurrence: SkippedAlarm) {
        viewModelScope.launch {

            dismissUpcomingNotificationIfArmed(occurrence)
            settingsRepository.update { it.copy(skippedOccurrences = it.skippedOccurrences.upsert(occurrence)) }
        }
    }

    private fun onReschedule(occurrence: SkippedAlarm) {
        viewModelScope.launch {

            val preNotificationId = when (occurrence) {
                is SkippedAlarm.Adhan -> AdhanContract.PRE_ADHAN_NOTIFICATION_ID
                is SkippedAlarm.Reminder -> ReminderContract.preNotificationId(occurrence.reminderId)
            }
            settingsRepository.update {
                val deliveredForDate = it.deliveredAlarmTimestamps[preNotificationId]
                    ?.let { ms -> Instant.fromEpochMilliseconds(ms).toLocalDate() == occurrence.date }
                    ?: false
                it.copy(
                    skippedOccurrences = it.skippedOccurrences.without(occurrence),
                    deliveredAlarmTimestamps =
                        if (deliveredForDate) {
                            it.deliveredAlarmTimestamps - preNotificationId
                        } else {
                            it.deliveredAlarmTimestamps
                        },
                )
            }
        }
    }

    private suspend fun dismissUpcomingNotificationIfArmed(occurrence: SkippedAlarm) {
        val armed = alarmRepository.getScheduled()
        when (occurrence) {
            is SkippedAlarm.Adhan -> {
                val alarm = armed.firstOrNull { it.id == AdhanContract.ADHAN_ALARM_ID } ?: return
                val prayer = alarm.extras[AdhanContract.EXTRA_PRAYER]?.let(::prayerOrNull)
                if (prayer == occurrence.prayer && alarm.dateMatches(occurrence)) {
                    notificationRepository.cancelNotification(AdhanContract.PRE_ADHAN_NOTIFICATION_ID)
                }
            }

            is SkippedAlarm.Reminder -> {
                val alarm = armed.firstOrNull { it.id == ReminderContract.alarmId(occurrence.reminderId) } ?: return
                if (alarm.dateMatches(occurrence)) {
                    notificationRepository.cancelNotification(ReminderContract.preNotificationId(occurrence.reminderId))
                }
            }
        }
    }

    private fun ScheduledAlarm.dateMatches(occurrence: SkippedAlarm): Boolean =
        Instant.fromEpochMilliseconds(triggerAtMillis).toLocalDate() == occurrence.date

    private fun prayerOrNull(name: String): Prayer? = runCatching { Prayer.valueOf(name) }.getOrNull()

    private fun UpcomingOccurrence.toUi(): UpcomingAlarmUi =
        UpcomingAlarmUi(
            occurrence = occurrence,
            isAdhan = isAdhan,
            prayer = prayer,
            reminderLabel = reminder?.label,
            fireTimeMs = fireTimeMs,
            skipped = skipped,
            reminderDuration = reminder?.duration ?: 0,
            reminderDurationModifier = reminder?.durationModifier ?: 0,
        )
}
