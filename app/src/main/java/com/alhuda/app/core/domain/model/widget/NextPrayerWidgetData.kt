package com.alhuda.app.core.domain.model.widget

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.adhan.Prayer

@Immutable
data class NextPrayerWidgetData(
    val prayer: Prayer,

    val countdownBaseMillis: Long,
    val adaptiveTheme: Boolean,

    val nextUpdateAtMillis: Long?,

    val locale: String,
)
