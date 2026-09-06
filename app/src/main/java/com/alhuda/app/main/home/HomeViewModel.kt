package com.alhuda.app.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alhuda.app.core.domain.model.adhan.NON_PRAYERS_IN_ORDER
import com.alhuda.app.core.domain.model.adhan.SHARIA_TIMES_IN_ORDER
import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.alarm.SkippedAlarm
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.repository.AlarmRepository
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.repository.SystemChangeRepository
import com.alhuda.app.core.domain.usecase.GetCurrentShariaTimesUseCase
import com.alhuda.app.core.domain.usecase.GetNextShariaTimesUseCase
import com.alhuda.app.core.domain.usecase.GetShariaTimesUseCase
import com.alhuda.app.core.domain.util.addDaysTimeZoneAware
import com.alhuda.app.core.domain.util.formatCountdownToHHmmss
import com.alhuda.app.core.domain.util.getDayBeginning
import com.alhuda.app.core.domain.util.hijriYear
import com.alhuda.app.core.domain.util.isRamadanNoticeDue
import com.alhuda.app.core.domain.util.tickFlow
import com.alhuda.app.core.domain.util.toLocalDate
import com.alhuda.app.core.presentation.dialog.SchedulingPermission
import com.alhuda.app.core.presentation.dialog.isDontAskAgain
import com.alhuda.app.core.presentation.dialog.withDontAskAgain
import com.alhuda.app.core.presentation.navigation.NavigationController
import com.alhuda.app.core.presentation.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@HiltViewModel
class HomeViewModel
@Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val getShariaTimesUseCase: GetShariaTimesUseCase,
    private val getNextShariaTimesUseCase: GetNextShariaTimesUseCase,
    private val getCurrentShariaTimesUseCase: GetCurrentShariaTimesUseCase,
    private val systemChangeRepository: SystemChangeRepository,
    private val alarmRepository: AlarmRepository,
    private val alarmSettingsRepository: AlarmSettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    @Volatile
    private var updateScreenJob: Job? = null

    @Volatile
    private var latestSettings: Settings? = null

    init {
        collectTimeTick()
        collectSystemChange()
        collectCurrentInstant()
        collectViewingInstant()
        viewModelScope.launch { settingsRepository.data.collect { latestSettings = it } }
    }

    suspend fun permissionCheck(): HomePermissionCheck {
        val alarmSettings = alarmSettingsRepository.data.first()
        return HomePermissionCheck(
            adhanScheduled = alarmSettings.hasAnyEnabledSchedule(),
            hasScheduledAlarms = alarmRepository.getScheduled().isNotEmpty(),
            fullScreenRequired = alarmSettings.hasAnySoundSchedule(),

            dndRequired = settingsRepository.data.first().bypassDnd || alarmSettings.autoSilentOnDismiss,
        )
    }

    fun isDontAskAgain(permission: SchedulingPermission): Boolean = latestSettings?.isDontAskAgain(permission) ?: false

    suspend fun shouldShowRamadanNotice(): Boolean {
        val settings = settingsRepository.data.first()
        if (settings.ramadanReminderDontShow) return false
        val now = Clock.System.now()
        val calendar = settings.selectedArabicCalendar
        if (!isRamadanNoticeDue(now, calendar)) return false
        return settings.ramadanRemindedYear != hijriYear(now, calendar)
    }

    fun onRamadanRemindNextYear() {
        viewModelScope.launch {
            settingsRepository.update {
                it.copy(ramadanRemindedYear = hijriYear(Clock.System.now(), it.selectedArabicCalendar))
            }
        }
    }

    fun onRamadanDontShowAgain() {
        viewModelScope.launch { settingsRepository.update { it.copy(ramadanReminderDontShow = true) } }
    }

    fun onPermissionDontAskAgain(permission: SchedulingPermission) {
        viewModelScope.launch { settingsRepository.update { it.withDontAskAgain(permission) } }
    }

    fun rescheduleAlarms() {
        viewModelScope.launch { alarmRepository.rescheduleAll() }
    }

    fun cleanupAlarms() {
        viewModelScope.launch { alarmRepository.cancelAll() }
    }

    private fun AlarmSettings.hasAnyEnabledSchedule(): Boolean =
        SHARIA_TIMES_IN_ORDER.any {
            getNotifSettings(it).selectedDays().isNotEmpty() || getSoundSettings(it).selectedDays().isNotEmpty()
        }

    private fun AlarmSettings.hasAnySoundSchedule(): Boolean =
        SHARIA_TIMES_IN_ORDER.any { getSoundSettings(it).selectedDays().isNotEmpty() }

    fun onAction(action: HomeUiAction) {
        when (action) {
            HomeUiAction.OnCalendarDateClick -> onCalendarDateClick()
            HomeUiAction.OnLocationTextClick -> onLocationTextClick()
            HomeUiAction.OnCalculationLinkClick -> onCalculationLinkClick()
            HomeUiAction.OnNextDayClick -> onNextDayClick()
            HomeUiAction.OnPrevDayClick -> onPrevDayClick()
            HomeUiAction.OnShowTodayClick -> onShowTodayClick()
            HomeUiAction.OnReminderLinkClick -> onReminderLinkClick()
            HomeUiAction.OnQiblaLinkClick -> onQiblaLinkClick()
            HomeUiAction.OnQiblaShortcutClick -> onQiblaShortcutClick()
            HomeUiAction.OnCounterLinkClick -> onCounterLinkClick()
            HomeUiAction.OnQuranLinkClick -> onQuranLinkClick()
            HomeUiAction.OnSettingsLinkClick -> onSettingsLinkClick()
            HomeUiAction.OnUpcomingAlarmsClick -> onUpcomingAlarmsClick()
            HomeUiAction.OnPrayerGuideClick -> onPrayerGuideClick()
            HomeUiAction.OnAboutLinkClick -> onAboutLinkClick()
            HomeUiAction.OnMonthlyViewClick -> onMonthlyViewClick()
            HomeUiAction.OnDeveloperLinkClick -> onDeveloperLinkClick()
        }
    }

    private fun onPrayerGuideClick() {
        NavigationController.navigateTo(Route.Main.PrayerGuide)
    }

    private fun onCalendarDateClick() {
        NavigationController.navigateTo(Route.Main.CalendarView)
    }

    private fun onCalculationLinkClick() {
        NavigationController.navigateTo(Route.Main.Settings.Calculations)
    }

    private fun onMonthlyViewClick() {
        NavigationController.navigateTo(Route.Main.MonthlyView)
    }

    private fun onDeveloperLinkClick() {
        NavigationController.navigateTo(Route.Main.Settings.Developer)
    }

    private fun onLocationTextClick() {
        NavigationController.navigateTo(Route.Main.Location)
    }

    private fun onNextDayClick() {
        _uiState.update { it.copy(viewingInstant = addDaysTimeZoneAware(it.viewingInstant, 1)) }
    }

    private fun onPrevDayClick() {
        _uiState.update { it.copy(viewingInstant = addDaysTimeZoneAware(it.viewingInstant, -1)) }
    }

    private fun onShowTodayClick() {
        _uiState.update { it.copy(viewingInstant = it.currentInstant) }
    }

    private fun onReminderLinkClick() {
        NavigationController.navigateTo(Route.Main.Reminder)
    }

    private fun onQiblaLinkClick() {
        NavigationController.navigateTo(Route.Main.Qibla)
    }

    private fun onQiblaShortcutClick() {
        val understood = latestSettings?.qiblaFinderUnderstood ?: false
        NavigationController.navigateTo(if (understood) Route.Main.QiblaCompass else Route.Main.Qibla)
    }

    private fun onCounterLinkClick() {
        NavigationController.navigateTo(Route.Main.Counter)
    }

    private fun onQuranLinkClick() {
        NavigationController.navigateTo(Route.Main.Quran)
    }

    private fun onSettingsLinkClick() {
        NavigationController.navigateTo(Route.Main.Settings)
    }

    private fun onUpcomingAlarmsClick() {
        NavigationController.navigateTo(Route.Main.UpcomingAlarms)
    }

    private fun onAboutLinkClick() {
        NavigationController.navigateTo(Route.Main.About)
    }

    private fun collectSystemChange() {
        viewModelScope.launch {
            systemChangeRepository.data.collect {
                _uiState.update { it.copy(currentInstant = Clock.System.now()) }
            }
        }
    }

    private fun collectTimeTick() {
        viewModelScope.launch {
            tickFlow().collect { now ->
                val nextShariaTime = uiState.value.nextShariaTime
                if (nextShariaTime != null && now > nextShariaTime.prayerTime) {

                    _uiState.update { it.copy(currentInstant = now) }
                } else if (uiState.value.showNextPrayerCountdown) {
                    _uiState.update {
                        if (it.nextShariaTime != null && now <= it.nextShariaTime.prayerTime) {
                            it.copy(
                                countdownText = formatCountdownToHHmmss(
                                    now,
                                    it.nextShariaTime.prayerTime,
                                    it.numberingSystem,
                                    it.locale,
                                ),
                            )
                        } else {
                            it
                        }
                    }
                }
            }
        }
    }

    private fun collectCurrentInstant() {
        viewModelScope.launch {
            combine(
                uiState.map { it.currentInstant }.distinctUntilChanged(),
                settingsRepository.data,
                calculationSettingsRepository.data,
                favoriteLocationsRepository.data,
            ) {
                    currentInstant,
                    settings,
                    calcSettings,
                    locations,
                ->
                _uiState.update {
                    val location = locations.firstOrNull { loc -> loc.id == calcSettings.locationId }
                    val hiddenPrayers = settings.hiddenPrayers.toSet()
                    val nextExcluded = if (settings.countdownSkipNonPrayers) {
                        hiddenPrayers + NON_PRAYERS_IN_ORDER
                    } else {
                        hiddenPrayers
                    }
                    val nextShariaTime = if (calcSettings.parameters != null && location != null) {
                        getNextShariaTimesUseCase(
                            instant = currentInstant,
                            calculationParameters = calcSettings.parameters,
                            calculationAdjustments = calcSettings.calculationAdjustments,
                            arabicCalendar = settings.selectedArabicCalendar,
                            locationDetail = location.locationDetail,
                            excluding = nextExcluded,
                        )
                    } else {
                        null
                    }
                    val highlightedShariaTime =
                        if (settings.highlightCurrentPrayer && calcSettings.parameters != null && location != null) {
                            getCurrentShariaTimesUseCase(
                                instant = currentInstant,
                                calculationParameters = calcSettings.parameters,
                                calculationAdjustments = calcSettings.calculationAdjustments,
                                arabicCalendar = settings.selectedArabicCalendar,
                                locationDetail = location.locationDetail,
                                excluding = hiddenPrayers,
                            )
                        } else {
                            nextShariaTime
                        }
                    val countdownText = if (settings.showHomeNextPrayerCountdown && nextShariaTime != null &&
                        currentInstant <= nextShariaTime.prayerTime
                    ) {
                        formatCountdownToHHmmss(
                            currentInstant,
                            nextShariaTime.prayerTime,
                            settings.numberingSystem,
                            settings.selectedLocale,
                        )
                    } else {
                        it.countdownText
                    }
                    updateScreenJob?.cancel()
                    updateScreenJob = viewModelScope.launch {
                        launch {
                            nextShariaTime?.prayerTime?.let { upcoming ->
                                val updateAfter = (upcoming - Clock.System.now()).plus(0.5.toDuration(DurationUnit.SECONDS))
                                delay(updateAfter)
                                _uiState.update { state -> state.copy(currentInstant = Clock.System.now()) }
                            }
                        }
                        launch {
                            val updateAfter = getDayBeginning(addDaysTimeZoneAware(Clock.System.now(), 1)) - Clock.System.now()
                            delay(updateAfter)
                            _uiState.update { state -> state.copy(currentInstant = Clock.System.now()) }
                        }
                    }
                    it.copy(
                        themeColor = settings.themeColor,
                        arabicCalendar = settings.selectedArabicCalendar,
                        arabicCalendarLocale = settings.selectedLocaleForArabicCalendar ?: settings.selectedLocale,
                        hijriDateAdjustment = calcSettings.calculationAdjustments.hijriDate,
                        calendar = settings.selectedSecondaryCalendar.value,
                        locale = settings.selectedLocale,
                        numberingSystem = settings.numberingSystem,
                        location = location,
                        isCalculationConfigured = calcSettings.parameters != null,
                        showNextPrayerCountdown = settings.showHomeNextPrayerCountdown,
                        nextShariaTime = nextShariaTime,
                        countdownText = countdownText,
                        highlightedShariaTime = highlightedShariaTime,
                        is24Hour = settings.is24HourFormat,
                        hiddenPrayers = settings.hiddenPrayers,
                        isDeveloper = settings.devMode,
                        homeShortcuts = settings.homeShortcuts,
                        hideToolbarCalendar = settings.hideToolbarCalendar,
                        swapHomeCalendars = settings.swapHomeCalendars,
                    )
                }
            }.collect()
        }
    }

    private fun collectViewingInstant() {
        viewModelScope.launch {
            combine(
                uiState.map { it.viewingInstant }.distinctUntilChanged(),
                settingsRepository.data,
                calculationSettingsRepository.data,
                favoriteLocationsRepository.data,
            ) {
                    viewingInstant,
                    settings,
                    calcSettings,
                    locations,
                ->
                _uiState.update {
                    val location = locations.firstOrNull { loc -> loc.id == calcSettings.locationId }
                    val shariaTimes = if (calcSettings.parameters != null && location != null) {
                        getShariaTimesUseCase(
                            instant = viewingInstant,
                            calculationParameters = calcSettings.parameters,
                            calculationAdjustments = calcSettings.calculationAdjustments,
                            arabicCalendar = settings.selectedArabicCalendar,
                            locationDetail = location.locationDetail,
                        )
                    } else {
                        null
                    }
                    val viewingDate = viewingInstant.toLocalDate()
                    val skippedPrayers = settings.skippedOccurrences
                        .filterIsInstance<SkippedAlarm.Adhan>()
                        .filter { skip -> skip.date == viewingDate }
                        .map { skip -> skip.prayer }
                        .toSet()
                    it.copy(
                        shariaTimes = shariaTimes,
                        skippedPrayers = skippedPrayers,
                    )
                }
            }.collect()
        }
    }
}
