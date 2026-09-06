package com.alhuda.app.core.domain.model.alarm

object AlarmSchedulingDefaults {

    const val REFIRE_GUARD_MS = 10_000L

    const val SEARCH_DAYS = 10

    fun alarmType(useDifferentAlarmType: Boolean): AlarmType =
        if (useDifferentAlarmType) AlarmType.ExactAllowWhileIdle else AlarmType.AlarmClock
}
