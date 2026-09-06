package com.alhuda.app.appfunctions

import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import com.alhuda.app.core.domain.model.adhan.SHARIA_TIMES_IN_ORDER
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.usecase.GetShariaTimesUseCase
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@AppFunctionSerializable(isDescribedByKDoc = true)
data class PrayerTimeEntry(

    val name: String,

    val time: String,

    val isoTimestamp: String,
)

@AppFunctionSerializable(isDescribedByKDoc = true)
data class PrayerTimesResult(

    val date: String,

    val location: String,

    val timeZone: String,

    val prayerTimes: List<PrayerTimeEntry>,
)

class PrayerTimesAppFunctions @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val getShariaTimesUseCase: GetShariaTimesUseCase,
) {

    @AppFunction(isDescribedByKDoc = true)
    suspend fun getPrayerTimes(
        appFunctionContext: AppFunctionContext,
        date: String,
    ): PrayerTimesResult {
        val localDate = try {
            LocalDate.parse(date)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Invalid date '$date'. Expected ISO format yyyy-MM-dd.", e)
        }

        val calcSettings = calculationSettingsRepository.fetch()
        val parameters = calcSettings.parameters
            ?: throw IllegalStateException("No calculation method configured in the app yet.")
        val location = favoriteLocationsRepository.fetch()
            .firstOrNull { it.id == calcSettings.locationId }
            ?: throw IllegalStateException("No location configured in the app yet.")
        val settings = settingsRepository.fetch()

        val zone = ZoneId.systemDefault()

        val instant = localDate.atTime(12, 0).atZone(zone).toInstant().toKotlinInstant()

        val shariaTimes = getShariaTimesUseCase(
            instant = instant,
            calculationParameters = parameters,
            calculationAdjustments = calcSettings.calculationAdjustments,
            arabicCalendar = settings.selectedArabicCalendar,
            locationDetail = location.locationDetail,
        )

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
        val entries = SHARIA_TIMES_IN_ORDER.map { prayer ->
            val time = shariaTimes.forPrayer(prayer)
            PrayerTimeEntry(
                name = prayer.name,
                time = time.toJavaInstant().atZone(zone).format(timeFormatter),
                isoTimestamp = time.toString(),
            )
        }

        return PrayerTimesResult(
            date = localDate.toString(),
            location = location.locationDetail.toDisplayString(),
            timeZone = zone.id,
            prayerTimes = entries,
        )
    }
}
