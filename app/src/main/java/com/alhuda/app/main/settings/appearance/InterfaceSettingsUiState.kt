package com.alhuda.app.main.settings.appearance

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.settings.Settings

@Immutable
data class InterfaceSettingsUiState(
    val settings: Settings = Settings(selectedLocale = "en"),
)

sealed interface InterfaceSettingsUiEvent {
    data class ShowMessage(
        @param:StringRes val messageRes: Int,
    ) : InterfaceSettingsUiEvent
}
