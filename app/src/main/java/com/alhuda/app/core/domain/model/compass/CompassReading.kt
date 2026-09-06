package com.alhuda.app.core.domain.model.compass

import androidx.compose.runtime.Immutable

@Immutable
data class CompassReading(
    val headingDegrees: Float,
    val accuracy: CompassAccuracy,
)

enum class CompassAccuracy {

    NO_SENSOR,
    UNRELIABLE,
    LOW,
    MEDIUM,
    HIGH,
}
