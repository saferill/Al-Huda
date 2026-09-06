package com.alhuda.app.core.data.repository

import com.alhuda.app.core.domain.model.widget.CustomWidgetConfig
import com.alhuda.app.core.domain.repository.CustomWidgetConfigRepository
import com.alhuda.app.core.util.storage.MMKVDataStore
import kotlinx.coroutines.flow.Flow

class CustomWidgetConfigRepositoryImpl(
    private val customWidgetConfigDatastore: MMKVDataStore<CustomWidgetConfig>,
) : CustomWidgetConfigRepository {
    override val data: Flow<CustomWidgetConfig>
        get() = customWidgetConfigDatastore.data

    override suspend fun fetch(): CustomWidgetConfig = customWidgetConfigDatastore.data.value

    override suspend fun update(transform: (t: CustomWidgetConfig) -> CustomWidgetConfig) {
        customWidgetConfigDatastore.update(transform)
    }
}
