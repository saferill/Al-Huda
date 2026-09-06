package com.alhuda.app.core.domain.model.geo

import kotlinx.serialization.Serializable

@Serializable
data class CountryGeoInfo(
    val code: String,

    val names: String,

    val name: String,

    val selectedName: String? = null,
) {
    override fun toString(): String = selectedName ?: name
}
