package com.alhuda.app.core.data.repository

import com.alhuda.app.core.domain.model.counter.Counter
import com.alhuda.app.core.domain.repository.CounterRepository
import com.alhuda.app.core.util.storage.MMKVDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CounterRepositoryImpl(
    private val counterStoreDatastore: MMKVDataStore<List<Counter>>,
) : CounterRepository {
    override val data: Flow<List<Counter>> =
        counterStoreDatastore.data

    override suspend fun fetch(): List<Counter> = data.first()

    override suspend fun update(transform: (t: List<Counter>) -> List<Counter>) {
        counterStoreDatastore.update(transform)
    }
}
