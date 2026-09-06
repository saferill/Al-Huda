package com.alhuda.app.main.qibla

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail

@Immutable
data class QiblaCompassUiState(
    val qiblaDegrees: Float? = null,
    val locationLabel: QiblaLocationLabel = QiblaLocationLabel.None,
    val isOrientationLocked: Boolean = false,
)

@Immutable
sealed interface QiblaLocationLabel {

    data object None : QiblaLocationLabel

    data object FromSettings : QiblaLocationLabel

    data class Fetched(val detail: CalculationLocationDetail) : QiblaLocationLabel
}
