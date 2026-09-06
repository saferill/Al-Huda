package com.alhuda.app.main.quran.detail

import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alhuda.app.core.presentation.navigation.NavigationController
import com.alhuda.app.main.quran.data.QuranRepository
import com.alhuda.app.main.quran.model.Surah
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuranDetailUiState(
    val currentPage: Int = 1,
    val surah: Surah? = null,
    val currentJuz: Int = 1,
    val isPlayingAudio: Boolean = false,
    val isBufferingAudio: Boolean = false,
    val isBookmarked: Boolean = false,
)

sealed interface QuranDetailUiAction {
    data class OnPageChanged(val page: Int) : QuranDetailUiAction
    data class OnJumpToPage(val page: Int) : QuranDetailUiAction
    data object OnBookmarkCurrentPage : QuranDetailUiAction
    data object OnPlaySurahAudio : QuranDetailUiAction
    data object OnStopAudio : QuranDetailUiAction
    data object OnBackClick : QuranDetailUiAction
}

@HiltViewModel
class QuranDetailViewModel @Inject constructor(
    private val repository: QuranRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialSurahNumber: Int = savedStateHandle.get<Int>("surahNumber") ?: 1
    private val initialPageNumber: Int = savedStateHandle.get<Int>("pageNumber") ?: 0

    private val _uiState = MutableStateFlow(
        run {
            val targetPage = if (initialPageNumber > 0) initialPageNumber else repository.getStartPageForSurah(initialSurahNumber)
            val clamped = targetPage.coerceIn(1, 604)
            QuranDetailUiState(
                currentPage = clamped,
                surah = repository.getSurahForPage(clamped),
                currentJuz = repository.getJuzForPage(clamped),
            )
        }
    )
    val uiState: StateFlow<QuranDetailUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    fun initialize(surahNumber: Int, pageNumber: Int) {
        val targetPage = if (pageNumber > 0) pageNumber else repository.getStartPageForSurah(surahNumber)
        updatePage(targetPage)
    }

    suspend fun loadPageBitmap(page: Int): Bitmap? {
        return repository.getPageBitmap(page)
    }

    private fun updatePage(page: Int) {
        val clamped = page.coerceIn(1, 604)
        val surah = repository.getSurahForPage(clamped)
        val juz = repository.getJuzForPage(clamped)
        _uiState.update {
            it.copy(
                currentPage = clamped,
                surah = surah,
                currentJuz = juz,
            )
        }
    }

    fun onAction(action: QuranDetailUiAction) {
        when (action) {
            is QuranDetailUiAction.OnPageChanged -> updatePage(action.page)
            is QuranDetailUiAction.OnJumpToPage -> updatePage(action.page)
            QuranDetailUiAction.OnBookmarkCurrentPage -> {
                repository.saveBookmarkPage(_uiState.value.currentPage)
                _uiState.update { it.copy(isBookmarked = true) }
            }
            QuranDetailUiAction.OnPlaySurahAudio -> {
                if (_uiState.value.isPlayingAudio) {
                    stopAudio()
                } else {
                    val surahNum = _uiState.value.surah?.number ?: 1
                    playSurahAudio(surahNum)
                }
            }
            QuranDetailUiAction.OnStopAudio -> stopAudio()
            QuranDetailUiAction.OnBackClick -> {
                stopAudio()
                NavigationController.navigateBack()
            }
        }
    }

    private fun playSurahAudio(surahNumber: Int) {
        stopAudio()
        val paddedSurah = surahNumber.toString().padStart(3, '0')
        val url = "https://cdn.equran.id/audio-full/Misyari-Rasyid-Al-Afasi/$paddedSurah.mp3"
        try {
            _uiState.update { it.copy(isBufferingAudio = true, isPlayingAudio = false) }
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build(),
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    mp.start()
                    _uiState.update { it.copy(isBufferingAudio = false, isPlayingAudio = true) }
                }
                setOnCompletionListener {
                    stopAudio()
                }
                setOnErrorListener { _, _, _ ->
                    stopAudio()
                    true
                }
                prepareAsync()
            }
        } catch (_: Exception) {
            stopAudio()
        }
    }

    private fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
        _uiState.update {
            it.copy(
                isPlayingAudio = false,
                isBufferingAudio = false,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}
