package com.alhuda.app.core.data.repository

import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.repository.ReminderRepository
import com.alhuda.app.core.util.storage.MMKVDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ReminderRepositoryImpl(
    private val reminderStoreDatastore: MMKVDataStore<List<Reminder>>,
) : ReminderRepository {
    override val data: Flow<List<Reminder>>
        get() = reminderStoreDatastore.data

    override suspend fun fetch(): List<Reminder> = data.first()

    override suspend fun update(transform: (t: List<Reminder>) -> List<Reminder>) {
        reminderStoreDatastore.update(transform)
    }
}
