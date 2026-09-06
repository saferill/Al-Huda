package com.alhuda.app

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExactAlarmPermissionReceiver : BroadcastReceiver() {

    private companion object {
        const val TAG = "ExactAlarmPermReceiver"
    }

    @Inject
    lateinit var schedulerReconciler: SchedulerReconciler

    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        if (intent?.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                schedulerReconciler.reconcileAll()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reconcile alarms after exact-alarm permission change", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
