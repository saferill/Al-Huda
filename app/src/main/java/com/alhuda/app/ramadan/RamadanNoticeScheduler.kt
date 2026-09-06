package com.alhuda.app.ramadan

import com.alhuda.app.core.domain.model.alarm.AlarmType
import com.alhuda.app.core.domain.model.alarm.ScheduledAlarm
import com.alhuda.app.core.domain.repository.AlarmRepository
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RamadanNoticeScheduler @Inject constructor(
    private val alarmRepository: AlarmRepository,
) {
    suspend fun schedule() {
        alarmRepository.schedule(
            ScheduledAlarm(
                id = RamadanNoticeContract.CHECK_ALARM_ID,
                triggerAtMillis = nextCheckMillis(),
                action = RamadanNoticeContract.ACTION_RAMADAN_CHECK,
                type = AlarmType.Inexact,
                wakeup = false,
            ),
        )
    }

    private fun nextCheckMillis(): Long {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var next = now.withHour(RamadanNoticeContract.CHECK_HOUR).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return next.toInstant().toEpochMilli()
    }
}
