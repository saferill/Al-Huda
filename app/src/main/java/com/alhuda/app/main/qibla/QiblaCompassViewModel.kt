package com.alhuda.app.main.qibla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.domain.model.calculation.toCoordinates
import com.alhuda.app.core.domain.model.compass.CompassReading
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.CompassRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.meypod.adhan_kotlin.Qibla
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QiblaCompassViewModel @Inject constructor(
    calculationSettingsRepository: CalculationSettingsRepository,
    favoriteLocationsRepository: FavoriteLocationsRepository,
    compassRepository: CompassRepository,
) : ViewModel() {

    private val fetchedLocation = MutableStateFlow<CalculationLocationDetail?>(null)
    private val orientationLocked = MutableStateFlow(false)

    private val settingsLocation: StateFlow<CalculationLocationDetail?> =
        combine(
            calculationSettingsRepository.data,
            favoriteLocationsRepository.data,
        ) { calcSettings, locations ->
            locations.firstOrNull { it.id == calcSettings.locationId }?.locationDetail
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val activeLocation: StateFlow<Pair<CalculationLocationDetail?, Boolean>> =
        combine(fetchedLocation, settingsLocation) { fetched, fromSettings ->
            if (fetched != null) fetched to true else fromSettings to false
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null to false)

    val uiState: StateFlow<QiblaCompassUiState> =
        combine(activeLocation, orientationLocked) { (location, isFetched), locked ->
            QiblaCompassUiState(
                qiblaDegrees = location?.let {
                    Qibla(it.toCoordinates()).direction.toFloat()
                },
                locationLabel = when {
                    location == null -> QiblaLocationLabel.None
                    isFetched -> QiblaLocationLabel.Fetched(location)
                    else -> QiblaLocationLabel.FromSettings
                },
                isOrientationLocked = locked,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QiblaCompassUiState())

    val readings: StateFlow<CompassReading?> =
        activeLocation
            .map { (location, _) -> location }
            .distinctUntilChanged()
            .flatMapLatest { location -> compassRepository.headings(location?.lat, location?.long) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onAction(action: QiblaCompassUiAction) {
        when (action) {
            QiblaCompassUiAction.OnToggleOrientationLock -> onToggleOrientationLock()
            is QiblaCompassUiAction.OnLocationFetched -> onLocationFetched(action)
        }
    }

    private fun onToggleOrientationLock() {
        orientationLocked.update { !it }
    }

    private fun onLocationFetched(action: QiblaCompassUiAction.OnLocationFetched) {
        fetchedLocation.value = action.detail
    }
}
