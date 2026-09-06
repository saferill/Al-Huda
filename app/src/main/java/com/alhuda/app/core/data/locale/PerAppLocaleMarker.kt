package com.alhuda.app.core.data.locale

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerAppLocaleMarker @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val file: File
        get() = File(context.noBackupFilesDir, "per_app_locale_applied")

    fun isMarked(): Boolean = file.exists()

    fun mark() {
        runCatching { file.createNewFile() }
    }
}
