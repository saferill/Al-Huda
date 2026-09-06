package com.alhuda.app.core.domain.repository

import com.alhuda.app.core.domain.model.reminder.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    val data: Flow<List<Reminder>>

    suspend fun fetch(): List<Reminder>

    suspend fun update(transform: (t: List<Reminder>) -> List<Reminder>)
}
