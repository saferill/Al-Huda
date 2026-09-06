package com.alhuda.app.core.domain.model.widget

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.settings.NotificationWidgetLayout

@Immutable
data class WidgetPrayerRow(
    val prayer: Prayer,
    val timeText: String,
    val isActive: Boolean,
)

@Immutable
data class WidgetCountdown(
    val prayer: Prayer,
    val baseMillis: Long,
)

@Immutable
data class WidgetData(
    val rows: List<WidgetPrayerRow>,
    val topStartText: String,
    val topEndText: String,
    val countdown: WidgetCountdown?,
    val adaptiveTheme: Boolean,
    val showCountdown: Boolean,
    val showNotification: Boolean,

    val notificationLayout: NotificationWidgetLayout,

    val swapLayoutDirection: Boolean,

    val nextUpdateAtMillis: Long?,

    val locale: String,
)
