package com.alhuda.app.alarm

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.settings.ThemeColor

@Immutable
data class AlarmFullscreenUiState(
    val header: String = "",
    val title: String = "",
    val timeLabel: String = "",
    val dismissAndSilentMinutes: Int = 0,
    val shortRemindMinutes: Int = 0,
    val longRemindMinutes: Int = 0,

    val autoSilentOnDismiss: Boolean = false,
    val themeColor: ThemeColor = ThemeColor.Default,
)
