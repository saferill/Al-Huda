package com.alhuda.app.core.domain.usecase

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.adhan.ShariaTimes
import com.alhuda.app.core.domain.model.alarm.AlarmSchedulingDefaults
import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.calculation.CalculationAdjustments
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.domain.util.addDaysTimeZoneAware
import com.alhuda.app.core.domain.util.getDayBeginning
import io.github.meypod.adhan_kotlin.CalculationParameters
import io.github.meypod.adhan_kotlin.data.DateComponents
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Instant

@Immutable
data class ShariaTimeDetails(
    val forInstant: Instant,
    val forDate: DateComponents,
    val prayer: Prayer,
    val prayerTime: Instant,
    val notify: Boolean,
    val sound: Boolean,
)

private val possiblePrevDayPrayers = listOf(Prayer.Midnight, Prayer.Tahajjud)

class GetNextShariaTimesUseCase @Inject constructor(
    private val getShariaTimesUseCase: GetShariaTimesUseCase,
) {

    operator fun invoke(
        instant: Instant,
        calculationParameters: CalculationParameters,
        calculationAdjustments: CalculationAdjustments,
        arabicCalendar: String,
        locationDetail: CalculationLocationDetail,
        alarmSettings: AlarmSettings? = null,
        excluding: Set<Prayer> = emptySet(),
        isSkipped: (Prayer, Instant) -> Boolean = { _, _ -> false },
    ): ShariaTimeDetails? {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        if (localDateTime.hour <= 6 && (alarmSettings?.shouldNotifyFor(instant, possiblePrevDayPrayers) ?: true)) {
            val prevDayInstant = addDaysTimeZoneAware(instant, -1)
            val prevDayShariahTimes = getShariaTimesUseCase(
                prevDayInstant,
                calculationParameters,
                calculationAdjustments,
                arabicCalendar,
                locationDetail,
            )
            val nextPrayer = prevDayShariahTimes.nextPrayerForAlarm(instant, alarmSettings, excluding, isSkipped)
            if (nextPrayer != null) {
                return ShariaTimeDetails(
                    forInstant = prevDayInstant,
                    forDate = DateComponents.from(prevDayInstant),
                    prayer = nextPrayer,
                    prayerTime = prevDayShariahTimes.forPrayer(nextPrayer),
                    notify = alarmSettings?.getNotifSettings(nextPrayer)?.shouldFireFor(instant) ?: false,
                    sound = alarmSettings?.getSoundSettings(nextPrayer)?.shouldFireFor(instant) ?: false,
                )
            }
        }

        var instantToCheck = instant
        var shariahTimes: ShariaTimes? = null
        var nextPrayer: Prayer? = null

        for (dayOffset in 0..AlarmSchedulingDefaults.SEARCH_DAYS) {
            shariahTimes =
                getShariaTimesUseCase(instantToCheck, calculationParameters, calculationAdjustments, arabicCalendar, locationDetail)

            val reference = if (dayOffset == 0) instant else getDayBeginning(instantToCheck)
            nextPrayer = shariahTimes.nextPrayerForAlarm(reference, alarmSettings, excluding, isSkipped)
            if (nextPrayer == null) {
                instantToCheck = addDaysTimeZoneAware(instantToCheck, 1)
            } else {
                break
            }
        }

        if (nextPrayer == null || shariahTimes == null) {
            return null
        }

        return ShariaTimeDetails(
            forInstant = instantToCheck,
            forDate = DateComponents.from(instantToCheck),
            prayer = nextPrayer,
            prayerTime = shariahTimes.forPrayer(nextPrayer),
            notify = alarmSettings?.getNotifSettings(nextPrayer)?.shouldFireFor(instantToCheck) ?: false,
            sound = alarmSettings?.getSoundSettings(nextPrayer)?.shouldFireFor(instantToCheck) ?: false,
        )
    }
}
