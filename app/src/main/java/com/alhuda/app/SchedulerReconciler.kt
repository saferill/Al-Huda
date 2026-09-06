package com.alhuda.app

import com.alhuda.app.adhan.AdhanScheduler
import com.alhuda.app.alarm.DndSilenceController
import com.alhuda.app.alarm.MissedAlarmCatchUp
import com.alhuda.app.ramadan.RamadanNoticeScheduler
import com.alhuda.app.reminder.ReminderScheduler
import com.alhuda.app.widget.WidgetUpdater
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchedulerReconciler @Inject constructor(
    private val adhanScheduler: AdhanScheduler,
    private val reminderScheduler: ReminderScheduler,
    private val ramadanNoticeScheduler: RamadanNoticeScheduler,
    private val widgetUpdater: WidgetUpdater,
    private val dndSilenceController: DndSilenceController,
    private val missedAlarmCatchUp: MissedAlarmCatchUp,
) {

    suspend fun reconcileAll(catchUpMissed: Boolean = false) {
        if (catchUpMissed) missedAlarmCatchUp.catchUpMissed()
        adhanScheduler.schedule()
        reminderScheduler.schedule()
        ramadanNoticeScheduler.schedule()
        widgetUpdater.update()

        dndSilenceController.reconcile()
    }
}
