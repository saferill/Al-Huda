package com.alhuda.app.main.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alhuda.app.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboutViewModel
@Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    fun onAction(action: AboutUiAction) {
        when (action) {
            AboutUiAction.OnUnlockDeveloper -> onUnlockDeveloper()
        }
    }

    private fun onUnlockDeveloper() {
        viewModelScope.launch {
            settingsRepository.update { it.copy(devMode = true) }
        }
    }
}
