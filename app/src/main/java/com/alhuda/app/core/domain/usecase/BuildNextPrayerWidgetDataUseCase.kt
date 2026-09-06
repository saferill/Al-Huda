package com.alhuda.app.core.domain.usecase

import com.alhuda.app.core.domain.model.adhan.SHARIA_TIMES_IN_ORDER
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.domain.model.calculation.CalculationSettings
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.model.widget.NextPrayerWidgetData
import javax.inject.Inject
import kotlin.time.Instant

class BuildNextPrayerWidgetDataUseCase @Inject constructor(
    private val getNextShariaTimesUseCase: GetNextShariaTimesUseCase,
) {
    operator fun invoke(
        instant: Instant,
        settings: Settings,
        calcSettings: CalculationSettings,
        location: CalculationLocationDetail?,
    ): NextPrayerWidgetData? {
        val parameters = calcSettings.parameters ?: return null
        if (location == null) return null

        val selected = settings.countdownWidgetPrayers.toSet()
        if (selected.isEmpty()) return null
        val excluding = SHARIA_TIMES_IN_ORDER.toSet() - selected

        val next = getNextShariaTimesUseCase(
            instant = instant,
            calculationParameters = parameters,
            calculationAdjustments = calcSettings.calculationAdjustments,
            arabicCalendar = settings.selectedArabicCalendar,
            locationDetail = location,
            excluding = excluding,
        ) ?: return null

        val nextMillis = next.prayerTime.toEpochMilliseconds()
        return NextPrayerWidgetData(
            prayer = next.prayer,
            countdownBaseMillis = nextMillis,
            adaptiveTheme = settings.adaptiveWidgets,

            nextUpdateAtMillis = nextMillis.takeIf { it > instant.toEpochMilliseconds() },
            locale = settings.selectedLocale,
        )
    }
}
