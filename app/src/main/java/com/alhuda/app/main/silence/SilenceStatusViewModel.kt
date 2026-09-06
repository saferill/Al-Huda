package com.alhuda.app.main.silence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alhuda.app.adhan.AdhanFiringHandler
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.util.formatTime
import com.alhuda.app.core.presentation.navigation.NavigationController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
class SilenceStatusViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val adhanFiringHandler: AdhanFiringHandler,
) : ViewModel() {

    val uiState: StateFlow<SilenceStatusUiState> =
        settingsRepository.data
            .map { settings ->
                val until = settings.silencedUntilMillis
                val active = until != null && until > Clock.System.now().toEpochMilliseconds()
                SilenceStatusUiState(
                    active = active,
                    untilFormatted = until?.takeIf { active }?.let { settings.formatTime(it) },
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SilenceStatusUiState())

    fun onAction(action: SilenceStatusUiAction) {
        when (action) {

            SilenceStatusUiAction.OnEndSilence -> viewModelScope.launch { adhanFiringHandler.onUnsilence() }

            SilenceStatusUiAction.OnClose -> NavigationController.navigateBack()
        }
    }
}
