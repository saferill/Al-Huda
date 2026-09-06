package com.alhuda.app.core.domain.repository

import com.alhuda.app.core.domain.model.alarm.ScheduledAlarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {

    val data: Flow<List<ScheduledAlarm>>

    suspend fun schedule(alarm: ScheduledAlarm)

    suspend fun cancel(id: String)

    suspend fun cancelAll()

    suspend fun getScheduled(): List<ScheduledAlarm>

    suspend fun rescheduleAll()

    fun canScheduleExact(): Boolean
}
