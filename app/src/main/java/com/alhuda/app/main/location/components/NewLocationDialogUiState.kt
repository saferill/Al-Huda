package com.alhuda.app.main.location.components

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.geo.CityGeoInfo
import com.alhuda.app.core.domain.model.geo.CountryGeoInfo

@Immutable
data class NewLocationDialogUiState(
    val selectedCountry: CountryGeoInfo? = null,
    val selectedCity: CityGeoInfo? = null,
    val latitude: String = "",
    val longitude: String = "",
    val label: String = "",
    val fetchingLocation: Boolean = false,
)
