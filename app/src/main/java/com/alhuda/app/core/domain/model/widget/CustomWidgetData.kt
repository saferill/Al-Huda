package com.alhuda.app.core.domain.model.widget

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.adhan.Prayer

@Immutable
data class CustomWidgetPrayerCell(
    val prayer: Prayer,
    val timeText: String,
    val isActive: Boolean,
)

@Immutable
data class CustomWidgetLocationPage(
    val name: String,
    val prayerRows: List<List<CustomWidgetPrayerCell>>,
    val topStartText: String? = null,
    val topEndText: String? = null,
)

@Immutable
data class CustomWidgetData(
    val bgColor: Int?,
    val textColor: Int?,
    val highlightColor: Int?,

    val topStartText: String?,
    val topEndText: String?,

    val prayerRows: List<List<CustomWidgetPrayerCell>>,

    val pages: List<CustomWidgetLocationPage> = emptyList(),
    val countdown: WidgetCountdown?,
    val showCountdown: Boolean,
    val countdownColor: Int?,

    val headerFontScale: Float = 1f,
    val prayerFontScale: Float = 1f,
    val countdownFontScale: Float = 1f,

    val nextUpdateAtMillis: Long?,

    val locale: String,
)
