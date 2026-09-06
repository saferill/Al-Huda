package com.alhuda.app.main.settings.adhan

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.settings.AudioEntry
import com.alhuda.app.core.domain.model.settings.Settings

@Immutable
data class AdhanSettingsUiState(
    val alarmSettings: AlarmSettings = AlarmSettings(),
    val settings: Settings = Settings(selectedLocale = "en"),

    val deviceSounds: List<AudioEntry> = emptyList(),

    val playingId: String? = null,
)
