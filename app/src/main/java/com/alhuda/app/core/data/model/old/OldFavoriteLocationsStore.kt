package com.alhuda.app.core.data.model.old

import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.domain.model.favorite_location.FavoriteLocation
import com.alhuda.app.core.domain.model.favorite_location.StaticFavoriteLocation
import com.alhuda.app.core.util.serialization.EmptyStringAsNullSerializer
import kotlinx.serialization.Serializable

@Serializable
data class OldFavoriteLocationsStore(
    val state: OldFavoriteLocationsStoreState,
    val version: Int,
)

@Serializable
data class OldFavoriteLocationsStoreState(
    val locations: List<OldFavoriteLocation> = emptyList(),
)

@Serializable
data class OldFavoriteLocation(
    val id: String,
    val lat: Double? = null,
    val long: Double? = null,
    val city: OldCityInfo? = null,
    val country: OldCountryInfo? = null,
    @Serializable(with = EmptyStringAsNullSerializer::class) val label: String? = null,
)

fun OldFavoriteLocation.toFavoriteLocation(): FavoriteLocation? =
    if (this.lat != null && this.long
        != null
    ) {
        StaticFavoriteLocation(
            this.id,
            CalculationLocationDetail(
                lat = this.lat,
                long = this.long,
                city = this.city?.toCityGeoInfo(),
                country = this.country?.toCountryGeoInfo(),
                label = this.label,
            ),
        )
    } else {
        null
    }
