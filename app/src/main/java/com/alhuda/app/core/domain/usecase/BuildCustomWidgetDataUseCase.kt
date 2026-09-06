package com.alhuda.app.core.domain.usecase

import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.adhan.ShariaTimes
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.domain.model.calculation.CalculationSettings
import com.alhuda.app.core.domain.model.favorite_location.FavoriteLocation
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.model.widget.CustomWidgetConfig
import com.alhuda.app.core.domain.model.widget.CustomWidgetData
import com.alhuda.app.core.domain.model.widget.CustomWidgetLocationPage
import com.alhuda.app.core.domain.model.widget.CustomWidgetPrayerCell
import com.alhuda.app.core.domain.model.widget.HeaderBlock
import com.alhuda.app.core.domain.model.widget.WidgetCountdown
import com.alhuda.app.core.domain.model.widget.icuCalendar
import com.alhuda.app.core.domain.util.maghribHijriDayShift
import javax.inject.Inject
import kotlin.time.Instant

class BuildCustomWidgetDataUseCase @Inject constructor(
    private val getShariaTimesUseCase: GetShariaTimesUseCase,
    private val getNextShariaTimesUseCase: GetNextShariaTimesUseCase,
    private val formatter: WidgetFormatter,
) {
    operator fun invoke(
        instant: Instant,
        settings: Settings,
        calcSettings: CalculationSettings,
        location: CalculationLocationDetail?,
        config: CustomWidgetConfig,
        favoriteLocations: List<FavoriteLocation> = emptyList(),
    ): CustomWidgetData? {
        val parameters = calcSettings.parameters ?: return null
        val locations = resolveLocations(config, location, favoriteLocations)
        if (locations.isEmpty()) return null

        val adjustments = calcSettings.calculationAdjustments
        val arabicCalendar = settings.selectedArabicCalendar
        val arabicCalendarLocale = settings.selectedLocaleForArabicCalendar ?: settings.selectedLocale

        val notPlaced = Prayer.entries.toSet() - config.rows.flatten().toSet()

        fun shariaTimesFor(loc: CalculationLocationDetail) = getShariaTimesUseCase(instant, parameters, adjustments, arabicCalendar, loc)

        fun nextFor(loc: CalculationLocationDetail) =
            getNextShariaTimesUseCase(instant, parameters, adjustments, arabicCalendar, loc, excluding = notPlaced)

        val primary = locations.first()
        val primaryTimes = shariaTimesFor(primary)
        val primaryNext = nextFor(primary)
        val maghribShift = maghribHijriDayShift(
            now = instant,
            maghrib = primaryTimes.maghrib,
            enabled = settings.widgetHijriDayStartsAtMaghrib,
        )

        fun resolveHeaderFor(
            loc: CalculationLocationDetail,
            block: HeaderBlock?,
        ): String? =
            when (block) {
                null -> null

                is HeaderBlock.LocationName -> loc.toDisplayString()

                is HeaderBlock.Date -> {
                    val icu = block.calendar.icuCalendar
                    if (icu == null) {

                        formatter.formatDate(
                            instant = formatter.adjustDays(instant, adjustments.hijriDate + maghribShift),
                            locale = arabicCalendarLocale,
                            calendar = arabicCalendar,
                            numberingSystem = settings.numberingSystem,
                            withDayName = block.withDayName,
                        )
                    } else {
                        formatter.formatDate(
                            instant = instant,
                            locale = settings.selectedLocale,
                            calendar = icu,
                            numberingSystem = settings.numberingSystem,
                            withDayName = block.withDayName,
                        )
                    }
                }
            }

        val pages = locations.map { loc ->
            val shariaTimes = if (loc == primary) primaryTimes else shariaTimesFor(loc)
            val active = activePrayer(shariaTimes, nextFor(loc), settings, instant)

            val prayerRows = config.rows
                .map { row ->
                    row.map { prayer ->
                        CustomWidgetPrayerCell(
                            prayer = prayer,
                            timeText = formatter.formatPrayerTime(
                                instant = shariaTimes.forPrayer(prayer),
                                is24Hour = settings.is24HourFormat,
                                numberingSystem = settings.numberingSystem,
                                locale = settings.selectedLocale,
                            ),
                            isActive = prayer == active,
                        )
                    }
                }
                .filter { it.isNotEmpty() }
            CustomWidgetLocationPage(
                name = loc.toDisplayString(),
                prayerRows = prayerRows,
                topStartText = resolveHeaderFor(loc, config.topStart),
                topEndText = resolveHeaderFor(loc, config.topEnd),
            )
        }
        val primaryPage = pages.first()

        val countdown = if (config.showCountdown && primaryNext != null) {
            WidgetCountdown(primaryNext.prayer, primaryNext.prayerTime.toEpochMilliseconds())
        } else {
            null
        }

        val nowMillis = instant.toEpochMilliseconds()
        val nextDayBeginning = formatter.nextDayBeginningMillis(instant)
        val nextPrayerMillis = primaryNext?.prayerTime?.toEpochMilliseconds()
        val maghribMillis = if (settings.widgetHijriDayStartsAtMaghrib) {
            primaryTimes.maghrib.toEpochMilliseconds()
        } else {
            null
        }
        val nextUpdateAtMillis =
            listOfNotNull(nextPrayerMillis, nextDayBeginning, maghribMillis).filter { it > nowMillis }.minOrNull()

        return CustomWidgetData(
            bgColor = config.bgColor,
            textColor = config.textColor,
            highlightColor = config.highlightColor,
            topStartText = primaryPage.topStartText,
            topEndText = primaryPage.topEndText,
            prayerRows = primaryPage.prayerRows,
            pages = if (locations.size > 1) pages else emptyList(),
            countdown = countdown,
            showCountdown = config.showCountdown,
            countdownColor = config.countdownColor,
            headerFontScale = config.headerFontScale,
            prayerFontScale = config.prayerFontScale,
            countdownFontScale = config.countdownFontScale,
            nextUpdateAtMillis = nextUpdateAtMillis,
            locale = settings.selectedLocale,
        )
    }

    private fun resolveLocations(
        config: CustomWidgetConfig,
        fallback: CalculationLocationDetail?,
        favoriteLocations: List<FavoriteLocation>,
    ): List<CalculationLocationDetail> =
        when {
            config.locationIds.isNotEmpty() ->
                config.locationIds.mapNotNull { id -> favoriteLocations.firstOrNull { it.id == id }?.locationDetail }

            fallback != null -> listOf(fallback)

            else -> emptyList()
        }

    private fun activePrayer(
        shariaTimes: ShariaTimes,
        next: ShariaTimeDetails?,
        settings: Settings,
        instant: Instant,
    ): Prayer? =
        if (settings.highlightCurrentPrayerWidget) {
            shariaTimes.currentPrayer(instant)
        } else {
            next?.takeIf { formatter.isSameDay(it.forInstant, instant) }?.prayer
        }
}
