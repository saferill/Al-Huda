package com.alhuda.app.core.presentation.feedback

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.alhuda.app.R
import com.alhuda.app.core.presentation.components.LocalSnackbarController
import com.alhuda.app.core.presentation.components.SnackbarController
import com.alhuda.app.core.presentation.mapper.reminderDisplayName
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
internal fun ObserveScheduleFeedback(
    scheduleFeedbackViewModel: ScheduleFeedbackViewModel = hiltViewModel(),
    snackbarController: SnackbarController = LocalSnackbarController.current,
) {
    CollectScheduleFeedback(
        scheduleFeedbackViewModel = scheduleFeedbackViewModel,
        snackbarController = snackbarController,
    )
}

@Composable
private fun CollectScheduleFeedback(
    scheduleFeedbackViewModel: ScheduleFeedbackViewModel,
    snackbarController: SnackbarController,
) {
    val resources = LocalResources.current

    LaunchedEffect(scheduleFeedbackViewModel, snackbarController, resources) {

        var shownKey: String? = null
        var shownIsAdjustment = false
        var showJob: Job? = null

        scheduleFeedbackViewModel.rescheduled.collect { info ->
            val active = showJob?.isActive == true
            when {

                info is ScheduleFeedbackInfo.Adjustment -> if (active) showJob?.cancel()

                active && shownIsAdjustment -> return@collect

                info.key == shownKey && active -> showJob?.cancel()

                else -> showJob?.join()
            }

            shownKey = info.key
            shownIsAdjustment = info is ScheduleFeedbackInfo.Adjustment
            showJob = launch { snackbarController.show(info.message(resources)) }
        }
    }
}

private fun ScheduleFeedbackInfo.message(resources: Resources): String =
    when (this) {
        is ScheduleFeedbackInfo.Adhan ->
            resources.getString(
                R.string.prayer_times_rescheduled,
                resources.getString(prayer.stringRes),
                formattedTime,
            )

        is ScheduleFeedbackInfo.PrayerAdjusted ->
            resources.getString(
                R.string.prayer_time_adjusted,
                resources.getString(prayer.stringRes),
                formattedTime,
            )

        is ScheduleFeedbackInfo.HijriDateAdjusted -> formattedDate

        is ScheduleFeedbackInfo.Reminder ->
            resources.getString(
                R.string.reminder_rescheduled,
                reminderDisplayName(resources, label, duration, durationModifier, prayer),
                formattedTime,
            )

        is ScheduleFeedbackInfo.ReminderBatch ->
            resources.getString(R.string.reminders_rescheduled_batch, count)
    }
