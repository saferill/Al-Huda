package com.alhuda.app.core.domain.repository

import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import kotlinx.coroutines.flow.Flow

interface AlarmSettingsRepository {
    val data: Flow<AlarmSettings>

    suspend fun fetch(): AlarmSettings

    suspend fun update(transform: (t: AlarmSettings) -> AlarmSettings)
}
