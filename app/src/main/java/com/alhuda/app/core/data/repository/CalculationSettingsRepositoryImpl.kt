package com.alhuda.app.core.data.repository

import com.alhuda.app.core.domain.model.calculation.CalculationSettings
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.util.storage.MMKVDataStore
import kotlinx.coroutines.flow.Flow

class CalculationSettingsRepositoryImpl(
    private val calcSettingsDatastore: MMKVDataStore<CalculationSettings>,
) : CalculationSettingsRepository {
    override val data: Flow<CalculationSettings>
        get() = calcSettingsDatastore.data

    override suspend fun fetch(): CalculationSettings = calcSettingsDatastore.data.value

    override suspend fun update(transform: (t: CalculationSettings) -> CalculationSettings) {
        calcSettingsDatastore.update(transform)
    }
}
