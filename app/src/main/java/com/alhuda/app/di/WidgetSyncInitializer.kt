package com.alhuda.app.di

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.calculation.CalculationAdjustments
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.domain.model.settings.NotificationWidgetLayout
import com.alhuda.app.core.domain.model.settings.NumberingSystem
import com.alhuda.app.core.domain.model.settings.SecondaryCalendar
import com.alhuda.app.core.domain.model.settings.WidgetCityNamePos
import com.alhuda.app.core.domain.model.widget.CustomWidgetConfig
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.CustomWidgetConfigRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.widget.WidgetUpdater
import io.github.meypod.adhan_kotlin.CalculationParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Singleton
class WidgetSyncInitializer @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val customWidgetConfigRepository: CustomWidgetConfigRepository,
    private val widgetUpdater: WidgetUpdater,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @OptIn(ExperimentalAtomicApi::class)
    private val started = AtomicBoolean(false)

    private data class WidgetSyncKey(
        val showWidget: Boolean,
        val notificationWidgetLayout: NotificationWidgetLayout,
        val showWidgetCountdown: Boolean,
        val adaptiveWidgets: Boolean,
        val widgetCityNamePos: WidgetCityNamePos,
        val swapWidgetLayoutDirection: Boolean,
        val hiddenWidgetPrayers: List<Prayer>,
        val countdownWidgetPrayers: List<Prayer>,
        val highlightCurrentPrayerWidget: Boolean,
        val is24HourFormat: Boolean,
        val numberingSystem: NumberingSystem,
        val selectedLocale: String,
        val selectedArabicCalendar: String,
        val selectedLocaleForArabicCalendar: String?,
        val selectedSecondaryCalendar: SecondaryCalendar,
        val parameters: CalculationParameters?,
        val calculationAdjustments: CalculationAdjustments,
        val locationId: String?,
        val locationLat: Double?,
        val locationLong: Double?,
        val locationLabel: String?,

        val customWidget: CustomWidgetConfig,

        val customWidgetLocationDetails: List<CalculationLocationDetail>,
    )

    @OptIn(ExperimentalAtomicApi::class)
    fun start() {
        if (!started.compareAndSet(expectedValue = false, newValue = true)) return

        scope.launch {
            combine(
                settingsRepository.data,
                calculationSettingsRepository.data,
                favoriteLocationsRepository.data,
                customWidgetConfigRepository.data,
            ) { settings, calc, locations, customWidget ->
                val location = locations.firstOrNull { it.id == calc.locationId }?.locationDetail
                val customWidgetLocationDetails = customWidget.locationIds.mapNotNull { id ->
                    locations.firstOrNull { it.id == id }?.locationDetail
                }
                WidgetSyncKey(
                    showWidget = settings.showWidget,
                    notificationWidgetLayout = settings.notificationWidgetLayout,
                    showWidgetCountdown = settings.showWidgetCountdown,
                    adaptiveWidgets = settings.adaptiveWidgets,
                    widgetCityNamePos = settings.widgetCityNamePos,
                    swapWidgetLayoutDirection = settings.swapWidgetLayoutDirection,
                    hiddenWidgetPrayers = settings.hiddenWidgetPrayers,
                    countdownWidgetPrayers = settings.countdownWidgetPrayers,
                    highlightCurrentPrayerWidget = settings.highlightCurrentPrayerWidget,
                    is24HourFormat = settings.is24HourFormat,
                    numberingSystem = settings.numberingSystem,
                    selectedLocale = settings.selectedLocale,
                    selectedArabicCalendar = settings.selectedArabicCalendar,
                    selectedLocaleForArabicCalendar = settings.selectedLocaleForArabicCalendar,
                    selectedSecondaryCalendar = settings.selectedSecondaryCalendar,
                    parameters = calc.parameters,
                    calculationAdjustments = calc.calculationAdjustments,
                    locationId = calc.locationId,
                    locationLat = location?.lat,
                    locationLong = location?.long,
                    locationLabel = location?.label,
                    customWidget = customWidget,
                    customWidgetLocationDetails = customWidgetLocationDetails,
                )
            }
                .distinctUntilChanged()
                .collect { widgetUpdater.update() }
        }

        scope.launch {
            withContext(Dispatchers.Main.immediate) {
                ProcessLifecycleOwner.get().lifecycle.addObserver(
                    object : DefaultLifecycleObserver {
                        override fun onStart(owner: LifecycleOwner) {
                            scope.launch { widgetUpdater.update() }
                        }
                    },
                )
            }
        }
    }
}
