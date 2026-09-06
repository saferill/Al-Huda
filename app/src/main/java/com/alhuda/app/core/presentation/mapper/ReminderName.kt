package com.alhuda.app.core.presentation.mapper

import android.content.res.Resources
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.reminder.Reminder

fun reminderDurationTitle(
    resources: Resources,
    duration: Int,
    durationModifier: Int,
    prayer: Prayer,
): String {
    val plural = if (durationModifier >= 0) R.plurals.reminder_minutes_after else R.plurals.reminder_minutes_before
    return resources.getQuantityString(plural, duration, duration, resources.getString(prayer.stringRes))
}

fun reminderDisplayName(
    resources: Resources,
    label: String,
    duration: Int,
    durationModifier: Int,
    prayer: Prayer,
): String = label.ifBlank { reminderDurationTitle(resources, duration, durationModifier, prayer) }

fun Reminder.displayName(resources: Resources): String = reminderDisplayName(resources, label, duration, durationModifier, prayer)
