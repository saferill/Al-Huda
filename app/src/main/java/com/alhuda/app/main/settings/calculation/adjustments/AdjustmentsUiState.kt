package com.alhuda.app.main.settings.calculation.adjustments

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.calculation.CalculationAdjustments

@Immutable
data class AdjustmentsUiState(
    val adjustments: CalculationAdjustments = CalculationAdjustments(),
)
