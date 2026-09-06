package com.alhuda.app.core.data.repository

import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.calculation.CalculationSettings
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.util.storage.MMKVDataStore
import kotlinx.coroutines.flow.Flow

class AlarmSettingsRepositoryImpl(
    private val alarmSettingsDatastore: MMKVDataStore<AlarmSettings>,
) : AlarmSettingsRepository {
    override val data: Flow<AlarmSettings>
        get() = alarmSettingsDatastore.data

    override suspend fun fetch(): AlarmSettings = alarmSettingsDatastore.data.value

    override suspend fun update(transform: (t: AlarmSettings) -> AlarmSettings) {
        alarmSettingsDatastore.update(transform)
    }
}
