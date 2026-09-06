package com.alhuda.app.core.domain.usecase

import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.adhan.SHARIA_TIMES_IN_ORDER
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.domain.model.calculation.CalculationSettings
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.model.settings.WidgetCityNamePos
import com.alhuda.app.core.domain.model.widget.WidgetCountdown
import com.alhuda.app.core.domain.model.widget.WidgetData
import com.alhuda.app.core.domain.model.widget.WidgetPrayerRow
import com.alhuda.app.core.domain.util.maghribHijriDayShift
import javax.inject.Inject
import kotlin.time.Instant

class BuildWidgetDataUseCase @Inject constructor(
    private val getShariaTimesUseCase: GetShariaTimesUseCase,
    private val getNextShariaTimesUseCase: GetNextShariaTimesUseCase,
    private val formatter: WidgetFormatter,
) {
    private companion object {

        val HIDDEN_CURRENT_FALLBACK = mapOf(
            Prayer.Isha to Prayer.Maghrib,
            Prayer.Asr to Prayer.Dhuhr,
        )
    }

    operator fun invoke(
        instant: Instant,
        settings: Settings,
        calcSettings: CalculationSettings,
        location: CalculationLocationDetail?,
    ): WidgetData? {
        val parameters = calcSettings.parameters ?: return null
        if (location == null) return null

        val arabicCalendarLocale = settings.selectedLocaleForArabicCalendar ?: settings.selectedLocale
        val hidden = settings.hiddenWidgetPrayers.toSet()

        val shariaTimes = getShariaTimesUseCase(
            instant = instant,
            calculationParameters = parameters,
            calculationAdjustments = calcSettings.calculationAdjustments,
            arabicCalendar = settings.selectedArabicCalendar,
            locationDetail = location,
        )

        val nextShariaTime = getNextShariaTimesUseCase(
            instant = instant,
            calculationParameters = parameters,
            calculationAdjustments = calcSettings.calculationAdjustments,
            arabicCalendar = settings.selectedArabicCalendar,
            locationDetail = location,
            excluding = hidden,
        )

        val activePrayer = if (settings.highlightCurrentPrayerWidget) {

            val current = shariaTimes.currentPrayer(instant)
            when {
                current == null -> null
                current !in hidden -> current
                else -> HIDDEN_CURRENT_FALLBACK[current]?.takeUnless { it in hidden }
            }
        } else {

            nextShariaTime?.takeIf { formatter.isSameDay(it.forInstant, instant) }?.prayer
        }

        val rows = SHARIA_TIMES_IN_ORDER
            .filter { it !in hidden }
            .map { prayer ->
                WidgetPrayerRow(
                    prayer = prayer,
                    timeText = formatter.formatPrayerTime(
                        instant = shariaTimes.forPrayer(prayer),
                        is24Hour = settings.is24HourFormat,
                        numberingSystem = settings.numberingSystem,
                        locale = settings.selectedLocale,
                    ),
                    isActive = prayer == activePrayer,
                )
            }

        val maghribShift = maghribHijriDayShift(
            now = instant,
            maghrib = shariaTimes.maghrib,
            enabled = settings.widgetHijriDayStartsAtMaghrib,
        )
        val lunarText = formatter.formatDate(
            instant = formatter.adjustDays(instant, calcSettings.calculationAdjustments.hijriDate + maghribShift),
            locale = arabicCalendarLocale,
            calendar = settings.selectedArabicCalendar,
            numberingSystem = settings.numberingSystem,
        )
        val secondaryText = formatter.formatDate(
            instant = instant,
            locale = settings.selectedLocale,
            calendar = settings.selectedSecondaryCalendar.value,
            numberingSystem = settings.numberingSystem,
        )
        val cityName = location.toDisplayString()
        val topStartText: String
        val topEndText: String
        when (settings.widgetCityNamePos) {
            WidgetCityNamePos.None -> {
                topStartText = lunarText
                topEndText = secondaryText
            }

            WidgetCityNamePos.TopStart -> {
                topStartText = cityName
                topEndText = secondaryText
            }

            WidgetCityNamePos.TopEnd -> {
                topStartText = lunarText
                topEndText = cityName
            }
        }

        val countdown = if (settings.showWidgetCountdown && nextShariaTime != null) {
            WidgetCountdown(nextShariaTime.prayer, nextShariaTime.prayerTime.toEpochMilliseconds())
        } else {
            null
        }

        val nowMillis = instant.toEpochMilliseconds()
        val nextDayBeginning = formatter.nextDayBeginningMillis(instant)
        val nextPrayerMillis = nextShariaTime?.prayerTime?.toEpochMilliseconds()

        val maghribMillis = if (settings.widgetHijriDayStartsAtMaghrib) {
            shariaTimes.maghrib.toEpochMilliseconds()
        } else {
            null
        }
        val nextUpdateAtMillis =
            listOfNotNull(nextPrayerMillis, nextDayBeginning, maghribMillis).filter { it > nowMillis }.minOrNull()

        return WidgetData(
            rows = rows,
            topStartText = topStartText,
            topEndText = topEndText,
            countdown = countdown,
            adaptiveTheme = settings.adaptiveWidgets,
            showCountdown = settings.showWidgetCountdown,
            showNotification = settings.showWidget,
            notificationLayout = settings.notificationWidgetLayout,
            swapLayoutDirection = settings.swapWidgetLayoutDirection,
            nextUpdateAtMillis = nextUpdateAtMillis,
            locale = settings.selectedLocale,
        )
    }
}
