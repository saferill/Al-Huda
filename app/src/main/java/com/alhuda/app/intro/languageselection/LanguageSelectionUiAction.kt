package com.alhuda.app.intro.languageselection

sealed interface LanguageSelectionUiAction {
    data class OnLanguageSelected(
        val locale: String,
    ) : LanguageSelectionUiAction
}
