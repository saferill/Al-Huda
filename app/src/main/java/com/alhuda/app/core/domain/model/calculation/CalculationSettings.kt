package com.alhuda.app.core.domain.model.calculation

import io.github.meypod.adhan_kotlin.CalculationMethod
import io.github.meypod.adhan_kotlin.CalculationParameters
import io.github.meypod.adhan_kotlin.MidnightMethod
import kotlinx.serialization.Serializable

@Serializable
data class CalculationSettings(
    val locationId: String? = null,
    val parameters: CalculationParameters? = null,
    val calculationAdjustments: CalculationAdjustments = CalculationAdjustments(),
    val midnightMethod: MidnightMethod = MidnightMethod.SunsetToFajr,
)

fun CalculationSettings.withDiyanetFixApplied(): CalculationSettings {
    val current = parameters ?: return this
    if (current.method != CalculationMethod.TURKEY && current.method != CalculationMethod.TURKEY_EUROPE) return this
    return copy(
        parameters = current.method.parameters.copy(madhab = current.madhab),
        calculationAdjustments = CalculationAdjustments(hijriDate = calculationAdjustments.hijriDate),
    )
}
