package com.alhuda.app.core.domain.usecase

import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.calculation.CalculationAdjustments
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import io.github.meypod.adhan_kotlin.CalculationParameters
import javax.inject.Inject
import kotlin.time.Instant

class GetCurrentShariaTimesUseCase @Inject constructor(
    private val getShariaTimesUseCase: GetShariaTimesUseCase,
) {
    operator fun invoke(
        instant: Instant,
        calculationParameters: CalculationParameters,
        calculationAdjustments: CalculationAdjustments,
        arabicCalendar: String,
        locationDetail: CalculationLocationDetail,
        excluding: Set<Prayer> = emptySet(),
    ): ShariaTimeDetails? {
        val shariaTimes = getShariaTimesUseCase(
            instant,
            calculationParameters,
            calculationAdjustments,
            arabicCalendar,
            locationDetail,
        )
        val currentPrayer = shariaTimes.currentPrayer(instant, excluding) ?: return null
        return ShariaTimeDetails(
            forInstant = instant,
            forDate = shariaTimes.forDate,
            prayer = currentPrayer,
            prayerTime = shariaTimes.forPrayer(currentPrayer),
            notify = false,
            sound = false,
        )
    }
}
