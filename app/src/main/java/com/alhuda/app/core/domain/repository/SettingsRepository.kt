package com.alhuda.app.core.domain.repository

import androidx.compose.runtime.Stable
import com.alhuda.app.core.domain.model.settings.Settings
import kotlinx.coroutines.flow.Flow

@Stable
interface SettingsRepository {
    val data: Flow<Settings>

    suspend fun fetch(): Settings

    suspend fun update(transform: (t: Settings) -> Settings)

    suspend fun markDelivered(
        notificationId: String,
        timestamp: Long,
    ) = update {
        it.copy(deliveredAlarmTimestamps = it.deliveredAlarmTimestamps + (notificationId to timestamp))
    }

    suspend fun clearDelivered(notificationId: String) =
        update {
            it.copy(deliveredAlarmTimestamps = it.deliveredAlarmTimestamps - notificationId)
        }
}
