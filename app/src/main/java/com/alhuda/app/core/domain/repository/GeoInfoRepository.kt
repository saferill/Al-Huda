package com.alhuda.app.core.domain.repository

import com.alhuda.app.core.domain.model.geo.CityGeoInfo
import com.alhuda.app.core.domain.model.geo.CountryGeoInfo

interface GeoInfoRepository {
    suspend fun getCountries(): List<CountryGeoInfo>

    suspend fun getCities(): List<CityGeoInfo>

    suspend fun getCities(countryCode: String): List<CityGeoInfo>
}
