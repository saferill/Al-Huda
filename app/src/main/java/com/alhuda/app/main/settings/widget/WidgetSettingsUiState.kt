package com.alhuda.app.main.settings.widget

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.settings.Settings

@Immutable
data class WidgetSettingsUiState(
    val settings: Settings = Settings(selectedLocale = "en"),
)

sealed interface WidgetSettingsUiEvent {
    data class ShowMessage(@param:StringRes val messageRes: Int) : WidgetSettingsUiEvent
}
