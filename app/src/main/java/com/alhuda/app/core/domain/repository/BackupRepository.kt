package com.alhuda.app.core.domain.repository

import android.net.Uri

interface BackupRepository {

    suspend fun exportTo(uri: Uri)

    fun hasLegacyData(): Boolean

    suspend fun exportLegacyTo(uri: Uri)

    suspend fun restoreFrom(uri: Uri)
}
