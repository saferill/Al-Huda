package com.alhuda.app.core.presentation.feedback

import com.alhuda.app.core.domain.model.adhan.Prayer
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ScheduleFeedbackInfo {
    val key: String

    data class Adhan(
        val prayer: Prayer,
        val formattedTime: String,
    ) : ScheduleFeedbackInfo {
        override val key = "adhan"
    }

    sealed interface Adjustment : ScheduleFeedbackInfo {
        override val key: String get() = ADJUSTMENT_KEY
    }

    data class PrayerAdjusted(
        val prayer: Prayer,
        val formattedTime: String,
    ) : Adjustment

    data class HijriDateAdjusted(
        val formattedDate: String,
    ) : Adjustment

    data class Reminder(
        val label: String,
        val prayer: Prayer,

        val duration: Int,
        val durationModifier: Int,
        val formattedTime: String,
    ) : ScheduleFeedbackInfo {
        override val key = REMINDER_KEY
    }

    data class ReminderBatch(
        val count: Int,
    ) : ScheduleFeedbackInfo {
        override val key = REMINDER_KEY
    }

    private companion object {

        const val REMINDER_KEY = "reminder"

        const val ADJUSTMENT_KEY = "adjustment"
    }
}

@Singleton
class ScheduleFeedback @Inject constructor() {

    private val _events =
        MutableSharedFlow<ScheduleFeedbackInfo>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val events: SharedFlow<ScheduleFeedbackInfo> = _events.asSharedFlow()

    fun notify(info: ScheduleFeedbackInfo) {
        _events.tryEmit(info)
    }
}
