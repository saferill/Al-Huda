package com.alhuda.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alhuda.app.core.domain.model.alarm.PrayerAlarmSettings
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.domain.model.favorite_location.StaticFavoriteLocation
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import io.github.meypod.adhan_kotlin.CalculationMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScreenshotSetupReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var alarmSettingsRepository: AlarmSettingsRepository

    @Inject
    lateinit var calculationSettingsRepository: CalculationSettingsRepository

    @Inject
    lateinit var favoriteLocationsRepository: FavoriteLocationsRepository

    @Inject
    lateinit var schedulerReconciler: SchedulerReconciler

    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                when (intent?.action) {
                    ACTION_SETUP_SCREENSHOTS -> {
                        applyPreset()
                        schedulerReconciler.reconcileAll()
                    }

                    ACTION_RESET_INTRO -> resetIntro()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun resetIntro() {
        settingsRepository.update {
            it.copy(appInitialConfigDone = false, appIntroDone = false)
        }
    }

    private suspend fun applyPreset() {
        val mecca = StaticFavoriteLocation(
            id = LOCATION_ID,
            locationDetail = CalculationLocationDetail(
                lat = 21.42664,
                long = 39.82563,
                label = "Mecca",
            ),
        )
        favoriteLocationsRepository.update { locations ->
            listOf(mecca) + locations.filter { it.id != LOCATION_ID }
        }
        calculationSettingsRepository.update {
            it.copy(
                locationId = LOCATION_ID,
                parameters = CalculationMethod.UMM_AL_QURA.parameters,
            )
        }

        val on = PrayerAlarmSettings.Bool(true)
        alarmSettingsRepository.update {
            it.copy(
                fajrNotify = on,
                fajrSound = on,
                dhuhrNotify = on,
                dhuhrSound = on,
                asrNotify = on,
                asrSound = on,
                maghribNotify = on,
                maghribSound = on,
                ishaNotify = on,
                ishaSound = on,
            )
        }

        settingsRepository.update {
            it.copy(
                appInitialConfigDone = true,
                appIntroDone = true,
                showWidget = true,
                dontAskPermissionNotifications = true,
                dontAskPermissionAlarm = true,
                dontAskPermissionPhoneState = true,
                dontAskPermissionFullScreenIntent = true,
                dontAskPermissionDndAccess = true,
                dontAskPermissionBatteryOptimization = true,
            )
        }
    }

    private companion object {
        const val ACTION_SETUP_SCREENSHOTS = "com.alhuda.app.action.SETUP_SCREENSHOTS"
        const val ACTION_RESET_INTRO = "com.alhuda.app.action.RESET_INTRO"
        const val LOCATION_ID = "screenshot-mecca"
    }
}
