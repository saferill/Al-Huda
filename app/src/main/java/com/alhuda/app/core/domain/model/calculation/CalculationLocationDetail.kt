package com.alhuda.app.core.domain.model.calculation

import com.alhuda.app.core.domain.model.geo.CityGeoInfo
import com.alhuda.app.core.domain.model.geo.CountryGeoInfo
import io.github.meypod.adhan_kotlin.Coordinates
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class CalculationLocationDetail(
    val lat: Double,
    val long: Double,
    val city: CityGeoInfo? = null,
    val country: CountryGeoInfo? = null,

    val label: String? = null,
) {

    val hasValidCoordinates: Boolean
        get() = lat in LATITUDE_RANGE && long in LONGITUDE_RANGE

    fun toNamed(): String? =
        if (!label.isNullOrBlank()) {
            label
        } else if (city != null) {
            if (country != null) {
                "$city, $country"
            } else {
                "$city"
            }
        } else {
            null
        }

    fun toDisplayString(): String = toNamed() ?: toCoordsString()

    fun toCoordsString(): String {
        val latDir = if (lat >= 0) "N" else "S"
        val longDir = if (long >= 0) "E" else "W"
        return "${
            String.format(
                Locale.ENGLISH,
                "%.4f",
                kotlin.math.abs(lat),
            )
        }°$latDir, ${
            String.format(
                Locale.ENGLISH,
                "%.4f",
                kotlin.math.abs(long),
            )
        }°$longDir"
    }

    companion object {
        val LATITUDE_RANGE = -90.0..90.0
        val LONGITUDE_RANGE = -180.0..180.0
    }
}

fun CalculationLocationDetail.toCoordinates(): Coordinates =
    Coordinates(
        latitude = lat.orZeroIfNotFinite().coerceIn(CalculationLocationDetail.LATITUDE_RANGE),
        longitude = long.orZeroIfNotFinite().coerceIn(CalculationLocationDetail.LONGITUDE_RANGE),
    )

private fun Double.orZeroIfNotFinite(): Double = if (isFinite()) this else 0.0
