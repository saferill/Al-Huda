package com.alhuda.app.main.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alhuda.app.core.presentation.navigation.NavigationController
import com.alhuda.app.core.presentation.navigation.Route
import com.alhuda.app.main.quran.data.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val repository: QuranRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedTab = MutableStateFlow(0)

    val uiState: StateFlow<QuranUiState> = combine(
        searchQuery,
        selectedTab,
        repository.bookmark,
    ) { query, tab, bookmark ->
        val surahs = repository.searchSurahs(query)
        val allJuz = repository.getAllJuz()
        QuranUiState(
            surahs = surahs,
            juzList = allJuz,
            searchQuery = query,
            selectedTab = tab,
            bookmark = bookmark,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        QuranUiState(
            surahs = repository.getAllSurahs(),
            juzList = repository.getAllJuz(),
        ),
    )

    fun onAction(action: QuranUiAction) {
        when (action) {
            is QuranUiAction.OnSearchQueryChange -> searchQuery.value = action.query
            is QuranUiAction.OnTabSelected -> selectedTab.value = action.index
            is QuranUiAction.OnSurahClick -> {
                val startPage = repository.getStartPageForSurah(action.surahNumber)
                NavigationController.navigateTo(Route.Main.QuranDetail(action.surahNumber, startPage))
            }
            is QuranUiAction.OnJuzClick -> {
                val startPage = repository.getStartPageForJuz(action.juz.number)
                NavigationController.navigateTo(Route.Main.QuranDetail(action.juz.startSurahNumber, startPage))
            }
            is QuranUiAction.OnBookmarkClick -> {
                NavigationController.navigateTo(Route.Main.QuranDetail(action.bookmark.surahNumber, action.bookmark.pageNumber))
            }
            is QuranUiAction.OnNavigate -> {
                NavigationController.navigateTo(action.route)
            }
            QuranUiAction.OnBackClick -> NavigationController.navigateBack()
        }
    }
}
