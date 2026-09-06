package com.alhuda.app.di

import com.alhuda.app.core.domain.model.calculation.withDiyanetFixApplied
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import io.github.meypod.adhan_kotlin.CalculationMethod
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiyanetParamsMigrationRunner
@Inject
constructor(
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun run() {
        if (settingsRepository.fetch().diyanetFixApplied) return

        val current = calculationSettingsRepository.fetch()
        val fixed = current.withDiyanetFixApplied()

        if (fixed != current) calculationSettingsRepository.update { fixed }
        settingsRepository.update {
            it.copy(
                diyanetFixApplied = true,

                diyanetChangeNoticePending = it.diyanetChangeNoticePending || fixed != current,
            )
        }
    }
}
