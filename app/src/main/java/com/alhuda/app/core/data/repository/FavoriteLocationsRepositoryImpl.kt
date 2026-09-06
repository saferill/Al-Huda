package com.alhuda.app.core.data.repository

import com.alhuda.app.core.domain.model.favorite_location.FavoriteLocation
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.util.storage.MMKVDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FavoriteLocationsRepositoryImpl(
    private val favoriteLocationsDatastore: MMKVDataStore<List<FavoriteLocation>>,
) : FavoriteLocationsRepository {
    override val data: Flow<List<FavoriteLocation>> =
        favoriteLocationsDatastore.data

    override suspend fun fetch(): List<FavoriteLocation> = data.first()

    override suspend fun update(transform: (t: List<FavoriteLocation>) -> List<FavoriteLocation>) {
        favoriteLocationsDatastore.update(transform)
    }
}
