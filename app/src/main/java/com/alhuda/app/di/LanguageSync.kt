package com.alhuda.app.di

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.alhuda.app.core.data.locale.PerAppLocaleMarker
import com.alhuda.app.core.data.locale.deviceSupportedLanguageOrEnglish
import com.alhuda.app.core.domain.repository.AppLocaleManager
import com.alhuda.app.core.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageSync
@Inject
constructor(
    private val settingsRepository: SettingsRepository,
    private val appLocaleManager: AppLocaleManager,
    private val perAppLocaleMarker: PerAppLocaleMarker,
) {

    suspend fun reconcile() {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val settingsLocale = settingsRepository.fetch().selectedLocale
        if (appLocales.isEmpty) {
            val isExplicitReset =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && perAppLocaleMarker.isMarked()
            if (isExplicitReset) {
                updateSettingsLocale(deviceSupportedLanguageOrEnglish(), settingsLocale)
            } else if (settingsLocale.isNotBlank()) {
                appLocaleManager.apply(settingsLocale)
            }
        } else {

            val appLang = appLocales.get(0)!!.toLanguageTag().substringBefore('-')
            updateSettingsLocale(appLang, settingsLocale)

            perAppLocaleMarker.mark()
        }
    }

    private suspend fun updateSettingsLocale(
        locale: String,
        current: String,
    ) {
        if (locale == current) return
        settingsRepository.update { it.copy(selectedLocale = locale) }
    }
}
