package com.alhuda.app.main.home.components

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.settings.NumberingSystem
import com.alhuda.app.core.domain.model.settings.ThemeColor
import kotlin.time.Instant

@Immutable
data class ShariaTimeRowUiState(
    val prayer: Prayer,
    val instant: Instant?,
    val locale: String = "en-US",
    val numberingSystem: NumberingSystem = NumberingSystem.Default,
    val is24Hours: Boolean = true,
    val highlightState: HighlightState = HighlightState.BeforeHighlight,
    val themeColor: ThemeColor = ThemeColor.Default,
    val skipped: Boolean = false,
)

enum class HighlightState {
    BeforeHighlight,
    Highlighted,
    AfterHighlight,
}
