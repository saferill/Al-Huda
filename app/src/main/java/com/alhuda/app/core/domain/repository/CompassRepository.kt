package com.alhuda.app.core.domain.repository

import com.alhuda.app.core.domain.model.compass.CompassReading
import kotlinx.coroutines.flow.Flow

interface CompassRepository {

    fun headings(
        latitude: Double? = null,
        longitude: Double? = null,
    ): Flow<CompassReading>
}
