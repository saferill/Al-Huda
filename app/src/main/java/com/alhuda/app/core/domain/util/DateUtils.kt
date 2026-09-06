package com.alhuda.app.core.domain.util

import android.content.res.Resources
import android.icu.text.DateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.icu.util.ULocale
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.settings.NumberingSystem
import com.alhuda.app.core.domain.model.settings.Settings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.math.abs
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration
import kotlin.time.toJavaInstant
import kotlinx.datetime.TimeZone as KotlinxTimeZone

fun formatInstant(
    instant: Instant,
    locale: String = "en-US",
    calendar: String = "gregorian",
    skeleton: String = DateFormat.YEAR_MONTH_DAY,
    numberingSystem: NumberingSystem = NumberingSystem.Default,
): String {
    val numbers = if (numberingSystem == NumberingSystem.Default) "" else ";numbers=${numberingSystem.value}"
    val formatter = DateFormat.getInstanceForSkeleton(skeleton, ULocale("$locale@calendar=$calendar$numbers"))
    return formatter.format(Date.from(instant.toJavaInstant()))
}

fun formatTimeOfDay(
    instant: Instant,
    is24Hour: Boolean,
    numberingSystem: NumberingSystem = NumberingSystem.Default,
    locale: String = "en-US",
): String =
    formatInstant(
        instant = instant,
        locale = locale,
        calendar = "gregorian",
        skeleton = if (is24Hour) DateFormat.HOUR24_MINUTE else DateFormat.HOUR_MINUTE,
        numberingSystem = numberingSystem,
    )

fun Settings.formatTime(timestamp: Long): String =
    formatTimeOfDay(
        instant = Instant.fromEpochMilliseconds(timestamp),
        is24Hour = is24HourFormat,
        numberingSystem = numberingSystem,
        locale = selectedLocale,
    )

fun Settings.formatRescheduleWhen(
    timestamp: Long,
    nowMs: Long,
    resources: Resources,
): String {
    val fire = Instant.fromEpochMilliseconds(timestamp)
    val now = Instant.fromEpochMilliseconds(nowMs)
    val time = formatTime(timestamp)
    val day = when {
        isSameGregorianDay(fire, now) -> return time
        isSameGregorianDay(fire, addDaysTimeZoneAware(now, 1)) -> resources.getString(R.string.tomorrow)
        else -> formatInstant(fire, selectedLocale, "gregorian", DateFormat.WEEKDAY, numberingSystem)
    }
    return resources.getString(R.string.reschedule_when, time, day)
}

fun Settings.formatMissedWhen(
    timestamp: Long,
    nowMs: Long,
): String {
    val fire = Instant.fromEpochMilliseconds(timestamp)
    if (isSameGregorianDay(fire, Instant.fromEpochMilliseconds(nowMs))) return formatTime(timestamp)
    val skeleton = DateFormat.YEAR_MONTH_DAY +
        if (is24HourFormat) DateFormat.HOUR24_MINUTE else DateFormat.HOUR_MINUTE
    return formatInstant(fire, selectedLocale, "gregorian", skeleton, numberingSystem)
}

fun isInRamadan(
    instant: Instant,
    arabicCalendar: String,
) = formatInstant(instant, "en", arabicCalendar, DateFormat.MONTH).contains("ramadan", ignoreCase = true)

fun isInShaaban(
    instant: Instant,
    arabicCalendar: String,
) = formatInstant(instant, "en", arabicCalendar, DateFormat.MONTH).contains("ban", ignoreCase = true)

fun hijriYear(
    instant: Instant,
    arabicCalendar: String,
): String = formatInstant(instant, "en", arabicCalendar, DateFormat.YEAR)

private fun hijriDayOfMonth(
    instant: Instant,
    arabicCalendar: String,
): Int {
    val cal = icuCalendar(arabicCalendar)
    cal.timeInMillis = instant.toEpochMilliseconds()
    return cal.get(Calendar.DAY_OF_MONTH)
}

private const val RAMADAN_NOTICE_DAY_THRESHOLD = 24

fun isRamadanNoticeDue(
    instant: Instant,
    arabicCalendar: String,
): Boolean {
    if (hijriDayOfMonth(instant, arabicCalendar) <= RAMADAN_NOTICE_DAY_THRESHOLD) return false
    return isInRamadan(instant, arabicCalendar) || isInShaaban(instant, arabicCalendar)
}

fun addDaysTimeZoneAware(
    instant: Instant,
    days: Int,
): Instant {
    if (days == 0) return instant
    val step = (days / abs(days)).toDuration(DurationUnit.HOURS)
    var newInstant = instant.plus(days.toDuration(DurationUnit.DAYS))

    while (isSameGregorianDay(instant, newInstant)) {
        newInstant = newInstant.plus(step)
    }
    return newInstant
}

fun maghribHijriDayShift(
    now: Instant,
    maghrib: Instant?,
    enabled: Boolean,
): Int {
    if (!enabled || maghrib == null) return 0
    return if (now >= maghrib) 1 else 0
}

private fun icuCalendar(calendar: String): Calendar = Calendar.getInstance(TimeZone.getDefault(), ULocale("@calendar=$calendar"))

fun calendarMonthDays(
    anchor: Instant,
    calendar: String,
): List<Instant> {
    val cal = icuCalendar(calendar)
    cal.timeInMillis = anchor.toEpochMilliseconds()
    cal.set(Calendar.HOUR_OF_DAY, 12)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val firstDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH)
    val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    return (firstDay..lastDay).map { day ->
        cal.set(Calendar.DAY_OF_MONTH, day)
        Instant.fromEpochMilliseconds(cal.timeInMillis)
    }
}

fun addCalendarMonths(
    instant: Instant,
    months: Int,
    calendar: String,
): Instant {
    val cal = icuCalendar(calendar)
    cal.timeInMillis = instant.toEpochMilliseconds()
    cal.add(Calendar.MONTH, months)
    return Instant.fromEpochMilliseconds(cal.timeInMillis)
}

fun isSameCalendarMonth(
    a: Instant,
    b: Instant,
    calendar: String,
): Boolean =
    formatInstant(a, "en", calendar, DateFormat.YEAR_NUM_MONTH) ==
        formatInstant(b, "en", calendar, DateFormat.YEAR_NUM_MONTH)

fun isSameGregorianDay(
    a: Instant,
    b: Instant,
): Boolean =
    formatInstant(a, "en", "gregorian", DateFormat.YEAR_NUM_MONTH_DAY) ==
        formatInstant(b, "en", "gregorian", DateFormat.YEAR_NUM_MONTH_DAY)

fun Instant.toLocalDate(): LocalDate = toLocalDateTime(KotlinxTimeZone.currentSystemDefault()).date

fun getDayBeginning(instant: Instant): Instant {
    val utcInstant = instant.toJavaInstant()
    val zdt = utcInstant.atZone(ZoneId.systemDefault())
    val startOfDayZdt = zdt.withHour(0).withMinute(0).withSecond(0).withNano(0)
    val utcStartInstant = startOfDayZdt.toInstant()
    return Instant.fromEpochSeconds(utcStartInstant.epochSecond, utcStartInstant.nano.toLong())
}
