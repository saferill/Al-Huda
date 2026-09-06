package com.alhuda.app.core.data.repository

import com.alhuda.app.core.data.locale.deviceSupportedLanguageOrEnglish
import com.alhuda.app.core.data.model.RestoreData
import com.alhuda.app.core.domain.model.alarm.PrayerAlarmSettings
import com.alhuda.app.core.domain.model.calculation.withDiyanetFixApplied
import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.AppLocaleManager
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.CounterRepository
import com.alhuda.app.core.domain.repository.CustomWidgetConfigRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.ReminderRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreApplier
@Inject
constructor(
    private val settingsRepository: SettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val counterRepository: CounterRepository,
    private val reminderRepository: ReminderRepository,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val customWidgetConfigRepository: CustomWidgetConfigRepository,
    private val appLocaleManager: AppLocaleManager,
) {
    suspend fun apply(data: RestoreData) {

        val needsDiyanetFix = !data.settings.diyanetFixApplied
        val calculationSettings =
            if (needsDiyanetFix) data.calculationSettings.withDiyanetFixApplied() else data.calculationSettings

        val settings = withResolvedLocale(data.settings).copy(
            diyanetFixApplied = true,
            diyanetChangeNoticePending = data.settings.diyanetChangeNoticePending ||
                calculationSettings != data.calculationSettings,
        )
        settingsRepository.update { settings }
        calculationSettingsRepository.update { calculationSettings }
        alarmSettingsRepository.update { data.alarmSettings }
        counterRepository.update { data.counters }
        reminderRepository.update { healReminders(data.reminders) }
        favoriteLocationsRepository.update { data.favoriteLocations }
        customWidgetConfigRepository.update { data.customWidget }

        appLocaleManager.apply(settings.selectedLocale)
    }

    private fun withResolvedLocale(settings: Settings): Settings {
        if (settings.selectedLocale.isNotBlank()) return settings
        val locale = deviceSupportedLanguageOrEnglish()
        return settings.copy(
            selectedLocale = locale,
            selectedArabicCalendar = if (locale.startsWith("fa")) "islamic-civil" else "islamic",
        )
    }

    private fun healReminders(reminders: List<Reminder>): List<Reminder> =
        reminders.map { reminder ->
            val days = reminder.days
            if (reminder.once != true && days is PrayerAlarmSettings.ByWeekDay && days.selectedDays().isEmpty()) {
                reminder.copy(days = PrayerAlarmSettings.ByWeekDay(PrayerAlarmSettings.ALL_DAYS.associateWith { true }))
            } else {
                reminder
            }
        }
}
