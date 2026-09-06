package com.alhuda.app.core.data.repository

import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.util.storage.MMKVDataStore
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val settingsDatastore: MMKVDataStore<Settings>,
) : SettingsRepository {
    override val data: Flow<Settings>
        get() = settingsDatastore.data

    override suspend fun fetch(): Settings = settingsDatastore.data.value

    override suspend fun update(transform: (t: Settings) -> Settings) {
        settingsDatastore.update(transform)
    }
}
