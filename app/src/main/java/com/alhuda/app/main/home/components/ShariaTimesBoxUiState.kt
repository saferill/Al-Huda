package com.alhuda.app.main.home.components

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.adhan.ShariaTimes
import com.alhuda.app.core.domain.model.settings.NumberingSystem
import com.alhuda.app.core.domain.model.settings.ThemeColor
import com.alhuda.app.core.domain.usecase.ShariaTimeDetails
import kotlin.time.Instant

@Immutable
data class ShariaTimesBoxUiState(
    val shariahTimes: ShariaTimes?,
    val highlightedShariaTime: ShariaTimeDetails?,
    val locale: String = "en-US",
    val numberingSystem: NumberingSystem = NumberingSystem.Default,
    val is24Hours: Boolean = true,
    val hiddenPrayers: List<Prayer> = emptyList(),
    val themeColor: ThemeColor = ThemeColor.Default,

    val skippedPrayers: Set<Prayer> = emptySet(),

    val now: Instant = Instant.fromEpochMilliseconds(0),
)
