package com.alhuda.app.core.data.locale

import android.content.res.Resources
import com.alhuda.app.core.domain.model.settings.SupportedLocales

fun deviceSupportedLanguageOrEnglish(): String {
    val deviceLocales = Resources.getSystem().configuration.locales
    for (i in 0 until deviceLocales.size()) {
        val language = deviceLocales[i].language
        if (SupportedLocales.any { it.value == language }) return language
    }
    return "en"
}
