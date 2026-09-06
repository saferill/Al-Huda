package com.alhuda.app.main.silence

import androidx.compose.runtime.Immutable

@Immutable
data class SilenceStatusUiState(
    val active: Boolean = false,

    val untilFormatted: String? = null,
)

sealed interface SilenceStatusUiAction {
    data object OnEndSilence : SilenceStatusUiAction
    data object OnClose : SilenceStatusUiAction
}
