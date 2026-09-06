package com.alhuda.app.main.settings.calculation

import androidx.compose.runtime.Immutable
import io.github.meypod.adhan_kotlin.CalculationParameters

@Immutable
data class CalculationSettingsUiState(
    val calculationParameters: CalculationParameters? = null,
    val selectedCalendar: String? = null,
)
