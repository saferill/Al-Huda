package com.alhuda.app.main.upcoming_alarms

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.alarm.SkippedAlarm
import com.alhuda.app.core.domain.model.settings.NumberingSystem

@Immutable
data class UpcomingAlarmUi(

    val occurrence: SkippedAlarm,
    val isAdhan: Boolean,

    val prayer: Prayer?,

    val reminderLabel: String?,

    val fireTimeMs: Long,

    val skipped: Boolean,

    val reminderDuration: Int = 0,
    val reminderDurationModifier: Int = 0,
)

@Immutable
data class UpcomingAlarmsUiState(
    val alarms: List<UpcomingAlarmUi> = emptyList(),
    val loading: Boolean = true,

    val locale: String = "en-US",
    val is24Hour: Boolean = true,
    val numberingSystem: NumberingSystem = NumberingSystem.Default,

    val nowMs: Long = 0L,

    val helpDismissed: Boolean = false,
)
