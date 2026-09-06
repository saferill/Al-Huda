package com.alhuda.app.di

import android.util.Log
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.TextResource
import com.alhuda.app.core.domain.model.notification.AndroidNotificationCategory
import com.alhuda.app.core.domain.model.notification.AndroidNotificationConfig
import com.alhuda.app.core.domain.model.notification.NotificationConfig
import com.alhuda.app.core.domain.model.notification.NotificationPressAction
import com.alhuda.app.core.domain.repository.NotificationRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.usecase.EnsureNotificationChannelsUseCase
import com.alhuda.app.core.presentation.navigation.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Singleton
class DiyanetChangeNoticePoster
@Inject
constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationRepository: NotificationRepository,
    private val ensureNotificationChannels: EnsureNotificationChannelsUseCase,
) {
    companion object {
        const val NOTIFICATION_ID = "diyanet_change_notice"
    }

    suspend fun postIfPending() {
        if (!settingsRepository.fetch().diyanetChangeNoticePending) return
        if (!notificationRepository.isNotificationsAllowed()) return

        ensureNotificationChannels()
        notificationRepository.notify(
            NotificationConfig(
                id = NOTIFICATION_ID,
                title = TextResource.StringResId(R.string.diyanet_change_notice_title),
                body = TextResource.StringResId(R.string.diyanet_change_notice_body),
                android = AndroidNotificationConfig(
                    channelId = EnsureNotificationChannelsUseCase.IMPORTANT_NOTICE_CHANNEL_ID,
                    category = AndroidNotificationCategory.CATEGORY_STATUS,
                    autoCancel = true,
                    pressAction = NotificationPressAction.Route(Route.Main.Settings.Calculations),
                ),
            ),
        )
        settingsRepository.update { it.copy(diyanetChangeNoticePending = false) }
    }
}

@Singleton
class DiyanetFixInitializer @Inject constructor(
    private val diyanetParamsMigrationRunner: DiyanetParamsMigrationRunner,
    private val diyanetChangeNoticePoster: DiyanetChangeNoticePoster,
) {
    private companion object {
        const val TAG = "DiyanetFixInitializer"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @OptIn(ExperimentalAtomicApi::class)
    private val started = AtomicBoolean(false)

    @OptIn(ExperimentalAtomicApi::class)
    fun start() {
        if (!started.compareAndSet(expectedValue = false, newValue = true)) return
        scope.launch {
            runCatching { diyanetParamsMigrationRunner.run() }
                .onFailure { Log.e(TAG, "Diyanet fix failed. Will retry on next launch.", it) }
            diyanetChangeNoticePoster.postIfPending()
        }
    }
}
