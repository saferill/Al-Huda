package com.alhuda.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var schedulerReconciler: SchedulerReconciler

    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {

        val catchUpMissed = when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> true
            Intent.ACTION_MY_PACKAGE_REPLACED -> false
            else -> return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                schedulerReconciler.reconcileAll(catchUpMissed = catchUpMissed)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
