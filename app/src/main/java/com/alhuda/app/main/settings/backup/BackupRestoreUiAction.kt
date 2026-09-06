package com.alhuda.app.main.settings.backup

import android.net.Uri

sealed interface BackupRestoreUiAction {
    data class OnExportFileSelected(
        val uri: Uri,
    ) : BackupRestoreUiAction

    data class OnExportLegacyFileSelected(
        val uri: Uri,
    ) : BackupRestoreUiAction

    data class OnRestoreFileSelected(
        val uri: Uri,
    ) : BackupRestoreUiAction
}
