package com.alhuda.app.main.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.alhuda.app.R
import com.alhuda.app.core.presentation.dialog.PermissionStep
import com.alhuda.app.core.presentation.dialog.SchedulingPermission
import com.alhuda.app.core.presentation.dialog.isSchedulingPermissionGranted
import com.alhuda.app.core.presentation.dialog.rememberSchedulingPermissionRequest

data class HomePermissionCheck(
    val adhanScheduled: Boolean,
    val hasScheduledAlarms: Boolean,

    val fullScreenRequired: Boolean,

    val dndRequired: Boolean,
)

@Composable
fun HomePermissionGate(
    isDontAskAgain: (SchedulingPermission) -> Boolean,
    onDontAskAgain: (SchedulingPermission) -> Unit,
    onReschedule: () -> Unit,
    onCleanup: () -> Unit,
    getCheck: suspend () -> HomePermissionCheck,
) {
    val context = LocalContext.current
    val request = rememberSchedulingPermissionRequest(
        isDontAskAgain = isDontAskAgain,
        onDontAskAgain = onDontAskAgain,
        allowDontAskAgain = true,
        onComplete = { results ->
            results.outcome(SchedulingPermission.ExactAlarm)?.let { outcome ->
                when {
                    outcome.granted -> onReschedule()
                    outcome.asked -> onCleanup()
                }
            }
        },
    )

    LaunchedEffect(Unit) {
        val check = getCheck()
        val steps = buildList {
            if (check.adhanScheduled) {
                if (!isSchedulingPermissionGranted(context, SchedulingPermission.Notification)) {
                    add(
                        PermissionStep(
                            SchedulingPermission.Notification,
                            R.string.home_notification_permission_rationale,
                            R.string.adhan_notification_permission_denied_text,
                        ),
                    )
                }
                if (!isSchedulingPermissionGranted(context, SchedulingPermission.PhoneState)) {
                    add(
                        PermissionStep(
                            SchedulingPermission.PhoneState,
                            R.string.adhan_phone_state_permission_rationale,
                            R.string.adhan_phone_state_permission_denied_text,
                        ),
                    )
                }
            }
            if (check.hasScheduledAlarms && !isSchedulingPermissionGranted(context, SchedulingPermission.ExactAlarm)) {
                add(
                    PermissionStep(
                        SchedulingPermission.ExactAlarm,
                        R.string.home_exact_alarm_permission_rationale,
                        R.string.adhan_exact_alarm_permission_denied_text,
                    ),
                )
            }
            if (check.fullScreenRequired && !isSchedulingPermissionGranted(context, SchedulingPermission.FullScreenIntent)) {
                add(
                    PermissionStep(
                        SchedulingPermission.FullScreenIntent,
                        R.string.adhan_full_screen_intent_permission_rationale,
                        R.string.adhan_full_screen_intent_permission_denied_text,
                    ),
                )
            }
            if (check.dndRequired && !isSchedulingPermissionGranted(context, SchedulingPermission.DndAccess)) {
                add(
                    PermissionStep(
                        SchedulingPermission.DndAccess,
                        R.string.dnd_permission_rationale,
                        R.string.dnd_permission_denied_text,
                    ),
                )
            }

            if (check.hasScheduledAlarms && !isSchedulingPermissionGranted(context, SchedulingPermission.BatteryOptimization)) {
                add(
                    PermissionStep(
                        SchedulingPermission.BatteryOptimization,
                        R.string.battery_optimization_permission_rationale,
                        R.string.battery_optimization_permission_denied_text,
                    ),
                )
            }
        }
        if (steps.isNotEmpty()) request(steps)
    }
}
