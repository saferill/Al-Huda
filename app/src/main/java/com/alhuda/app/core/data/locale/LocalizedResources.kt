package com.alhuda.app.core.data.locale

import android.content.Context
import android.content.res.Resources
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.util.storage.MMKVDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalizedResources @Inject constructor(
    @param:ApplicationContext private val base: Context,
    private val settingsStore: MMKVDataStore<Settings>,
) {
    private class Cached(
        val languageTags: String,
        val resources: Resources,
    )

    @Volatile
    private var cached: Cached? = null

    val current: Resources
        get() {
            val tags = settingsStore.data.value.selectedLocale
            cached?.takeIf { it.languageTags == tags }?.let { return it.resources }
            return base.withAppLocale(tags).resources.also { cached = Cached(tags, it) }
        }
}
