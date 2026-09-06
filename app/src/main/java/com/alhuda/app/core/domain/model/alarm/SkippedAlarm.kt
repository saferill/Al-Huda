package com.alhuda.app.core.domain.model.alarm

import com.alhuda.app.core.domain.model.adhan.Prayer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
sealed interface SkippedAlarm {
    val date: LocalDate

    @Serializable
    data class Adhan(
        val prayer: Prayer,
        override val date: LocalDate,
    ) : SkippedAlarm

    @Serializable
    data class Reminder(
        val reminderId: String,
        override val date: LocalDate,
    ) : SkippedAlarm
}

fun List<SkippedAlarm>.isAdhanSkipped(
    prayer: Prayer,
    date: LocalDate,
): Boolean = any { it is SkippedAlarm.Adhan && it.prayer == prayer && it.date == date }

fun List<SkippedAlarm>.isReminderSkipped(
    reminderId: String,
    date: LocalDate,
): Boolean = any { it is SkippedAlarm.Reminder && it.reminderId == reminderId && it.date == date }

inline fun <reified T : SkippedAlarm> List<SkippedAlarm>.prunePastDays(today: LocalDate): List<SkippedAlarm> =
    filterNot { it is T && it.date < today }

fun List<SkippedAlarm>.upsert(entry: SkippedAlarm): List<SkippedAlarm> = filterNot { it == entry } + entry

fun List<SkippedAlarm>.without(entry: SkippedAlarm): List<SkippedAlarm> = filterNot { it == entry }
