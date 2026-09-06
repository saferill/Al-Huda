package com.alhuda.app.main.settings.troubleshoot

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.util.device.PowerManagerUtils

@Immutable
data class TroubleshootUiState(
    val appIsAllowedToKeepRunning: Boolean = false,
    val powerManagerInfo: PowerManagerUtils.PowerManagerInfo? = null,
    val autostartAvailable: Boolean = false,
    val dndAccessGranted: Boolean = false,

    val isTelevision: Boolean = false,
)
