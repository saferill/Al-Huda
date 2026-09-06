package com.alhuda.app.main.upcoming_alarms

import com.alhuda.app.core.domain.model.alarm.SkippedAlarm

sealed interface UpcomingAlarmsUiAction {

    data class OnSkip(
        val occurrence: SkippedAlarm,
    ) : UpcomingAlarmsUiAction

    data class OnReschedule(
        val occurrence: SkippedAlarm,
    ) : UpcomingAlarmsUiAction

    object OnDismissHelp : UpcomingAlarmsUiAction

    object OnShowHelp : UpcomingAlarmsUiAction
}
