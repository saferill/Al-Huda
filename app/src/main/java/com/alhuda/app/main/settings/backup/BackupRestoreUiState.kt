package com.alhuda.app.main.settings.backup

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class BackupRestoreUiState(

    val busy: Boolean = false,

    val canExportLegacy: Boolean = false,
)

sealed interface BackupRestoreUiEvent {
    data class ShowMessage(
        @param:StringRes val messageRes: Int,
    ) : BackupRestoreUiEvent
}
