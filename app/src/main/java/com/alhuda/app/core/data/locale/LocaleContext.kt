package com.alhuda.app.core.data.locale

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.core.app.LocaleManagerCompat

fun Context.withAppLocale(languageTags: String = ""): Context {
    val tags = languageTags.ifBlank {
        LocaleManagerCompat.getApplicationLocales(this).toLanguageTags()
    }
    if (tags.isBlank()) return this
    val config = Configuration(resources.configuration)
    config.setLocales(LocaleList.forLanguageTags(tags))
    return createConfigurationContext(config)
}
