package com.alhuda.app.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.TextResource
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.domain.model.favorite_location.TravelingFavoriteLocation
import com.alhuda.app.core.domain.model.notification.AndroidNotificationConfig
import com.alhuda.app.core.domain.model.notification.NotificationConfig
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.NotificationRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.usecase.EnsureNotificationChannelsUseCase.Companion.TRAVEL_MODE_CHANNEL_ID
import com.alhuda.app.core.util.android.LocationUtils
import com.alhuda.app.core.util.lang.ListUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.time.Clock

const val TRAVEL_MODE_WORK_NAME = "travel_mode_worker"

@HiltWorker
class TravelModeWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val settingsRepository: SettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val notificationRepository: NotificationRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        if (calculationSettingsRepository.fetch().locationId != TravelingFavoriteLocation.LOCATION_ID) {
            WorkManager.getInstance(applicationContext).cancelUniqueWork(TRAVEL_MODE_WORK_NAME)
            return Result.success()
        }
        if (!LocationUtils.isLocationEnabled(applicationContext)) {
            notificationRepository.notify(
                NotificationConfig(
                    title = TextResource.StringResId(R.string.location_is_disabled_notif_title),
                    body = TextResource.StringResId(R.string.location_is_disabled_notif_body),
                    android = AndroidNotificationConfig(
                        channelId = TRAVEL_MODE_CHANNEL_ID,
                        onlyAlertOnce = true,
                    ),
                ),
            )
        } else if (
            applicationContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||

            (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    applicationContext.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED
                )
        ) {
            notificationRepository.notify(
                NotificationConfig(
                    title = TextResource.StringResId(R.string.worker_location_permission_revoked_title),
                    body = TextResource.StringResId(R.string.worker_location_permission_revoked_body),
                    android = AndroidNotificationConfig(
                        channelId = TRAVEL_MODE_CHANNEL_ID,
                        onlyAlertOnce = true,
                    ),
                ),
            )
        } else {
            val newLocation = LocationUtils.requestCurrentLocation(applicationContext, 60_000).getOrNull()
            if (newLocation != null) {
                favoriteLocationsRepository.update { list ->
                    val newTravelLocation = TravelingFavoriteLocation(
                        CalculationLocationDetail(
                            lat = newLocation.latitude,
                            long = newLocation.longitude,
                        ),
                    )
                    val travelLocationIndex = list.indexOfFirst { it is TravelingFavoriteLocation }
                    if (travelLocationIndex != -1) {
                        ListUtils.replaceInImmutableList(list, travelLocationIndex, newTravelLocation)
                    } else {
                        listOf(newTravelLocation) + list
                    }
                }
                settingsRepository.update {
                    it.copy(travelModeLastUpdateMillis = Clock.System.now().toEpochMilliseconds())
                }
                return Result.success()
            }
        }

        return Result.failure()
    }
}
