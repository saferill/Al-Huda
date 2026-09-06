package com.alhuda.app.core.presentation.dialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.presentation.components.LocalSnackbarController
import com.alhuda.app.core.presentation.components.PermissionRationaleDialog
import kotlinx.coroutines.launch
import android.provider.Settings as AndroidSettings

enum class SchedulingPermission {

    Notification,

    PhoneState,

    ExactAlarm,

    FullScreenIntent,

    DndAccess,

    BatteryOptimization,

    DisplayOverApps,
}

@Immutable
data class PermissionStep(
    val permission: SchedulingPermission,
    @param:StringRes val rationale: Int,
    @param:StringRes val denied: Int,
)

@Immutable
data class PermissionOutcome(
    val granted: Boolean,

    val asked: Boolean,
)

@Immutable
data class PermissionResults(
    val outcomes: Map<SchedulingPermission, PermissionOutcome>,
) {
    fun outcome(permission: SchedulingPermission): PermissionOutcome? = outcomes[permission]

    fun granted(permission: SchedulingPermission): Boolean = outcomes[permission]?.granted == true

    fun requiredAllGranted(): Boolean = outcomes.all { (permission, outcome) -> permission.isOptional() || outcome.granted }
}

fun SchedulingPermission.isOptional(): Boolean =
    this == SchedulingPermission.PhoneState ||
        this == SchedulingPermission.FullScreenIntent ||
        this == SchedulingPermission.BatteryOptimization

fun Settings.isDontAskAgain(permission: SchedulingPermission): Boolean =
    when (permission) {
        SchedulingPermission.Notification -> dontAskPermissionNotifications
        SchedulingPermission.PhoneState -> dontAskPermissionPhoneState
        SchedulingPermission.ExactAlarm -> dontAskPermissionAlarm
        SchedulingPermission.FullScreenIntent -> dontAskPermissionFullScreenIntent
        SchedulingPermission.DndAccess -> dontAskPermissionDndAccess
        SchedulingPermission.BatteryOptimization -> dontAskPermissionBatteryOptimization
        SchedulingPermission.DisplayOverApps -> dontAskPermissionDisplayOverApps
    }

fun Settings.withDontAskAgain(permission: SchedulingPermission): Settings =
    when (permission) {
        SchedulingPermission.Notification -> copy(dontAskPermissionNotifications = true)
        SchedulingPermission.PhoneState -> copy(dontAskPermissionPhoneState = true)
        SchedulingPermission.ExactAlarm -> copy(dontAskPermissionAlarm = true)
        SchedulingPermission.FullScreenIntent -> copy(dontAskPermissionFullScreenIntent = true)
        SchedulingPermission.DndAccess -> copy(dontAskPermissionDndAccess = true)
        SchedulingPermission.BatteryOptimization -> copy(dontAskPermissionBatteryOptimization = true)
        SchedulingPermission.DisplayOverApps -> copy(dontAskPermissionDisplayOverApps = true)
    }

object SchedulingPermissionSteps {
    val widget: List<PermissionStep> = listOf(
        PermissionStep(
            SchedulingPermission.Notification,
            R.string.notification_permission_rationale,
            R.string.notification_permission_denied_text,
        ),
        PermissionStep(
            SchedulingPermission.ExactAlarm,
            R.string.exact_alarm_permission_rationale,
            R.string.exact_alarm_permission_denied_text,
        ),
    )

    val adhan: List<PermissionStep> = listOf(
        PermissionStep(
            SchedulingPermission.Notification,
            R.string.adhan_notification_permission_rationale,
            R.string.adhan_notification_permission_denied_text,
        ),
        PermissionStep(
            SchedulingPermission.PhoneState,
            R.string.adhan_phone_state_permission_rationale,
            R.string.adhan_phone_state_permission_denied_text,
        ),
        PermissionStep(
            SchedulingPermission.ExactAlarm,
            R.string.adhan_exact_alarm_permission_rationale,
            R.string.adhan_exact_alarm_permission_denied_text,
        ),
        PermissionStep(
            SchedulingPermission.FullScreenIntent,
            R.string.adhan_full_screen_intent_permission_rationale,
            R.string.adhan_full_screen_intent_permission_denied_text,
        ),
    )

    val reminder: List<PermissionStep> = listOf(
        PermissionStep(
            SchedulingPermission.Notification,
            R.string.reminder_notification_permission_rationale,
            R.string.reminder_notification_permission_denied_text,
        ),
        PermissionStep(
            SchedulingPermission.PhoneState,
            R.string.reminder_phone_state_permission_rationale,
            R.string.reminder_phone_state_permission_denied_text,
        ),
        PermissionStep(
            SchedulingPermission.ExactAlarm,
            R.string.reminder_exact_alarm_permission_rationale,
            R.string.reminder_exact_alarm_permission_denied_text,
        ),
        PermissionStep(
            SchedulingPermission.FullScreenIntent,
            R.string.reminder_full_screen_intent_permission_rationale,
            R.string.reminder_full_screen_intent_permission_denied_text,
        ),
    )

    val forceLaunchAlarm: List<PermissionStep> = listOf(
        PermissionStep(
            SchedulingPermission.DisplayOverApps,
            R.string.display_over_apps_permission_rationale,
            R.string.display_over_apps_permission_denied_text,
        ),
    )

    val dndBypass: List<PermissionStep> = listOf(
        PermissionStep(
            SchedulingPermission.DndAccess,
            R.string.dnd_permission_rationale,
            R.string.dnd_permission_denied_text,
        ),
    )
}

@SuppressLint("InlinedApi")
@Composable
fun rememberSchedulingPermissionRequest(
    isDontAskAgain: (SchedulingPermission) -> Boolean,
    onDontAskAgain: (SchedulingPermission) -> Unit,

    allowDontAskAgain: Boolean = false,
    onComplete: (PermissionResults) -> Unit = {},
): (List<PermissionStep>) -> Unit {
    val context = LocalContext.current
    val resources = LocalResources.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val snackbarController = LocalSnackbarController.current
    val openSettingsLabel = stringResource(R.string.open_settings_label)

    val onCompleteState = rememberUpdatedState(onComplete)
    val isSuppressed = rememberUpdatedState(isDontAskAgain)
    val persistDontAsk = rememberUpdatedState(onDontAskAgain)

    val queue = remember { mutableStateOf<List<PermissionStep>>(emptyList()) }
    val outcomes = remember { mutableStateOf<Map<SchedulingPermission, PermissionOutcome>>(emptyMap()) }
    val dialogStep = remember { mutableStateOf<PermissionStep?>(null) }

    fun guideToSettings(
        @StringRes message: Int,
        openSettings: () -> Unit,
    ) {
        val text = resources.getString(message)
        scope.launch {
            if (snackbarController.show(text, actionLabel = openSettingsLabel) == SnackbarResult.ActionPerformed) {
                openSettings()
            }
        }
    }

    val processRef = remember { mutableStateOf<() -> Unit>({}) }

    fun finishStep(
        step: PermissionStep,
        granted: Boolean,
        asked: Boolean,
    ) {
        outcomes.value = outcomes.value + (step.permission to PermissionOutcome(granted, asked))
        queue.value = queue.value.drop(1)
        processRef.value()
    }

    fun resolveSettingsStep(
        step: PermissionStep,
        isGranted: () -> Boolean,
        openSettings: () -> Unit,
    ) {
        val granted = isGranted()
        if (!granted) guideToSettings(step.denied, openSettings)
        finishStep(step, granted, asked = true)
    }

    val notificationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val step = queue.value.firstOrNull() ?: return@rememberLauncherForActivityResult
            if (!granted && activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
            ) {

                guideToSettings(step.denied) { openAppNotificationSettings(context) }
            }
            finishStep(step, granted, asked = true)
        }

    val phoneStateLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val step = queue.value.firstOrNull() ?: return@rememberLauncherForActivityResult
            finishStep(step, granted, asked = true)
        }

    val exactAlarmSettingsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val step = queue.value.firstOrNull() ?: return@rememberLauncherForActivityResult
            resolveSettingsStep(step, { canScheduleExactAlarms(context) }) { openExactAlarmSettings(context) }
        }

    val dndSettingsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val step = queue.value.firstOrNull() ?: return@rememberLauncherForActivityResult
            resolveSettingsStep(step, { dndAccessGranted(context) }) { openDndSettings(context) }
        }

    val fullScreenIntentSettingsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val step = queue.value.firstOrNull() ?: return@rememberLauncherForActivityResult
            resolveSettingsStep(step, { fullScreenIntentGranted(context) }) { openFullScreenIntentSettings(context) }
        }

    val batteryOptimizationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val step = queue.value.firstOrNull() ?: return@rememberLauncherForActivityResult
            resolveSettingsStep(step, { batteryOptimizationGranted(context) }) { openBatteryOptimizationSettings(context) }
        }

    val displayOverAppsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val step = queue.value.firstOrNull() ?: return@rememberLauncherForActivityResult
            resolveSettingsStep(step, { displayOverAppsGranted(context) }) { openDisplayOverAppsSettings(context) }
        }

    fun ask(step: PermissionStep) {
        when (step.permission) {
            SchedulingPermission.Notification -> notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

            SchedulingPermission.PhoneState -> phoneStateLauncher.launch(Manifest.permission.READ_PHONE_STATE)

            SchedulingPermission.DndAccess -> {
                try {
                    dndSettingsLauncher.launch(Intent(AndroidSettings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.open_settings_failed, Toast.LENGTH_LONG).show()
                    finishStep(step, granted = false, asked = true)
                }
            }

            SchedulingPermission.ExactAlarm -> {
                val intent = exactAlarmSettingsIntent(context)
                if (intent != null) {
                    try {
                        exactAlarmSettingsLauncher.launch(intent)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, R.string.open_settings_failed, Toast.LENGTH_LONG).show()
                        finishStep(step, granted = false, asked = true)
                    }
                } else {
                    finishStep(step, granted = true, asked = true)
                }
            }

            SchedulingPermission.FullScreenIntent -> {
                val intent = fullScreenIntentSettingsIntent(context)
                if (intent != null) {
                    try {
                        fullScreenIntentSettingsLauncher.launch(intent)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, R.string.open_settings_failed, Toast.LENGTH_LONG).show()
                        finishStep(step, granted = false, asked = true)
                    }
                } else {
                    finishStep(step, granted = true, asked = true)
                }
            }

            SchedulingPermission.BatteryOptimization -> {
                try {
                    batteryOptimizationLauncher.launch(batteryOptimizationIntent(context))
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.open_settings_failed, Toast.LENGTH_LONG).show()
                    finishStep(step, granted = false, asked = true)
                }
            }

            SchedulingPermission.DisplayOverApps -> {
                try {
                    displayOverAppsLauncher.launch(displayOverAppsIntent(context))
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.open_settings_failed, Toast.LENGTH_LONG).show()
                    finishStep(step, granted = false, asked = true)
                }
            }
        }
    }

    fun process() {
        val step = queue.value.firstOrNull()
        if (step == null) {
            onCompleteState.value(PermissionResults(outcomes.value))
            return
        }

        val respectFlag = allowDontAskAgain || step.permission.isOptional()
        when {
            isGranted(context, step.permission) -> finishStep(step, granted = true, asked = false)
            respectFlag && isSuppressed.value(step.permission) -> finishStep(step, granted = false, asked = false)
            else -> dialogStep.value = step
        }
    }
    processRef.value = { process() }

    dialogStep.value?.let { step ->
        PermissionRationaleDialog(
            title = stringResource(titleResFor(step.permission)),
            text = stringResource(step.rationale),
            confirmLabel = stringResource(confirmLabelResFor(step.permission)),
            onConfirm = {
                dialogStep.value = null
                ask(step)
            },
            onCancel = {
                dialogStep.value = null
                finishStep(step, granted = false, asked = true)
            },

            onDontAskAgain = if (allowDontAskAgain || step.permission.isOptional()) {
                {
                    dialogStep.value = null
                    persistDontAsk.value(step.permission)
                    finishStep(step, granted = false, asked = true)
                }
            } else {
                null
            },
        )
    }

    return remember {
        { steps: List<PermissionStep> ->
            outcomes.value = emptyMap()
            queue.value = steps
            process()
        }
    }
}

fun isSchedulingPermissionGranted(
    context: Context,
    permission: SchedulingPermission,
): Boolean = isGranted(context, permission)

private fun isGranted(
    context: Context,
    permission: SchedulingPermission,
): Boolean =
    when (permission) {
        SchedulingPermission.Notification -> notificationGranted(context)

        SchedulingPermission.PhoneState ->
            context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

        SchedulingPermission.ExactAlarm -> canScheduleExactAlarms(context)

        SchedulingPermission.FullScreenIntent -> fullScreenIntentGranted(context)

        SchedulingPermission.DndAccess -> dndAccessGranted(context)

        SchedulingPermission.BatteryOptimization -> batteryOptimizationGranted(context)

        SchedulingPermission.DisplayOverApps -> displayOverAppsGranted(context)
    }

private fun displayOverAppsGranted(context: Context): Boolean = AndroidSettings.canDrawOverlays(context)

private fun batteryOptimizationGranted(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

private fun dndAccessGranted(context: Context): Boolean {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return nm.isNotificationPolicyAccessGranted
}

private fun fullScreenIntentGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return nm.canUseFullScreenIntent()
}

@SuppressLint("InlinedApi")
private fun notificationGranted(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private fun canScheduleExactAlarms(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
}

@StringRes
private fun titleResFor(permission: SchedulingPermission): Int =
    when (permission) {
        SchedulingPermission.Notification -> R.string.notification_permission_title
        SchedulingPermission.PhoneState -> R.string.phone_state_permission_title
        SchedulingPermission.ExactAlarm -> R.string.exact_alarm_permission_title
        SchedulingPermission.FullScreenIntent -> R.string.full_screen_intent_permission_title
        SchedulingPermission.DndAccess -> R.string.dnd_permission_title
        SchedulingPermission.BatteryOptimization -> R.string.battery_optimization_permission_title
        SchedulingPermission.DisplayOverApps -> R.string.display_over_apps_permission_title
    }

@StringRes
private fun confirmLabelResFor(permission: SchedulingPermission): Int =
    when (permission) {
        SchedulingPermission.ExactAlarm,
        SchedulingPermission.FullScreenIntent,
        SchedulingPermission.DndAccess,
        SchedulingPermission.DisplayOverApps,
        -> R.string.open_settings_label

        else -> R.string.okay
    }

private fun openAppNotificationSettings(context: Context) {
    val intent = Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName)
        .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
    safeStart(context, intent)
}

private fun exactAlarmSettingsIntent(context: Context): Intent? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    return Intent(
        AndroidSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
        "package:${context.packageName}".toUri(),
    ).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
}

private fun openExactAlarmSettings(context: Context) {
    safeStart(context, exactAlarmSettingsIntent(context) ?: return)
}

private fun fullScreenIntentSettingsIntent(context: Context): Intent? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return null
    return Intent(
        AndroidSettings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
        "package:${context.packageName}".toUri(),
    ).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
}

private fun openFullScreenIntentSettings(context: Context) {
    safeStart(context, fullScreenIntentSettingsIntent(context) ?: return)
}

@SuppressLint("BatteryLife")
private fun batteryOptimizationIntent(context: Context): Intent =
    Intent(
        AndroidSettings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        "package:${context.packageName}".toUri(),
    ).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }

private fun openBatteryOptimizationSettings(context: Context) {
    safeStart(
        context,
        Intent(AndroidSettings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP },
    )
}

private fun displayOverAppsIntent(context: Context): Intent =
    Intent(
        AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION,
        "package:${context.packageName}".toUri(),
    ).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }

private fun openDisplayOverAppsSettings(context: Context) {
    safeStart(
        context,
        Intent(AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION)
            .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP },
    )
}

private fun openDndSettings(context: Context) {
    safeStart(
        context,
        Intent(AndroidSettings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP },
    )
}

private fun safeStart(
    context: Context,
    intent: Intent,
) {
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.open_settings_failed, Toast.LENGTH_LONG).show()
    }
}
