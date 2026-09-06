package com.alhuda.app.core.domain.model.alarm

import kotlinx.serialization.Serializable

@Serializable
enum class AlarmType {

    AlarmClock,

    ExactAllowWhileIdle,

    Exact,

    Inexact,
}

@Serializable
data class ScheduledAlarm(
    val id: String,
    val triggerAtMillis: Long,
    val action: String,
    val type: AlarmType = AlarmType.ExactAllowWhileIdle,
    val extras: Map<String, String> = emptyMap(),
    val wakeup: Boolean = true,
)
