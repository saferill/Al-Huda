package com.alhuda.app.main.quran

import com.alhuda.app.core.presentation.navigation.Route
import com.alhuda.app.main.quran.model.JuzInfo
import com.alhuda.app.main.quran.model.QuranBookmark
import com.alhuda.app.main.quran.model.Surah

data class QuranUiState(
    val surahs: List<Surah> = emptyList(),
    val juzList: List<JuzInfo> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: Int = 0,
    val bookmark: QuranBookmark? = null,
)

sealed interface QuranUiAction {
    data class OnSearchQueryChange(val query: String) : QuranUiAction
    data class OnTabSelected(val index: Int) : QuranUiAction
    data class OnSurahClick(val surahNumber: Int) : QuranUiAction
    data class OnJuzClick(val juz: JuzInfo) : QuranUiAction
    data class OnBookmarkClick(val bookmark: QuranBookmark) : QuranUiAction
    data class OnNavigate(val route: Route) : QuranUiAction
    data object OnBackClick : QuranUiAction
}
