package com.alhuda.app.core.domain.model.geo

import kotlinx.serialization.Serializable

@Serializable
data class CityGeoInfo(

    val name: String,
    val names: String,
    val lat: Double,
    val long: Double,
    val country: String,

    val selectedName: String? = null,
) {

    override fun toString(): String = selectedName ?: name
}
