package com.alhuda.app.main.qibla

import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail

sealed interface QiblaCompassUiAction {
    data object OnToggleOrientationLock : QiblaCompassUiAction
    data class OnLocationFetched(val detail: CalculationLocationDetail) : QiblaCompassUiAction
}
