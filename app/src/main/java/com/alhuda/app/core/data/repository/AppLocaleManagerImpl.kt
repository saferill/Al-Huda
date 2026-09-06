package com.alhuda.app.core.data.repository

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.alhuda.app.core.data.locale.PerAppLocaleMarker
import com.alhuda.app.core.domain.repository.AppLocaleManager

class AppLocaleManagerImpl(
    private val perAppLocaleMarker: PerAppLocaleMarker,
) : AppLocaleManager {
    override fun apply(localeTags: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(localeTags),
        )
        perAppLocaleMarker.mark()
    }
}
