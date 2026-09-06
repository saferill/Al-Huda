package com.alhuda.app.core.domain.usecase

import com.alhuda.app.core.domain.model.settings.NumberingSystem
import kotlin.time.Instant

interface WidgetFormatter {
    fun formatPrayerTime(
        instant: Instant,
        is24Hour: Boolean,
        numberingSystem: NumberingSystem,
        locale: String,
    ): String

    fun formatDate(
        instant: Instant,
        locale: String,
        calendar: String,
        numberingSystem: NumberingSystem,

        withDayName: Boolean = false,
    ): String

    fun adjustDays(
        instant: Instant,
        days: Int,
    ): Instant

    fun isSameDay(
        a: Instant,
        b: Instant,
    ): Boolean

    fun nextDayBeginningMillis(instant: Instant): Long
}
