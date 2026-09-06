package com.alhuda.app.core.domain.repository

import androidx.compose.runtime.Stable
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.model.system.SystemChange
import kotlinx.coroutines.flow.Flow

@Stable
interface SystemChangeRepository {
    val data: Flow<SystemChange>

    fun tryEmit(type: SystemChange): Boolean
}
