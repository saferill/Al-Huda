package com.alhuda.app.alarm

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.alhuda.app.R
import com.alhuda.app.adhan.AdhanContract
import com.alhuda.app.core.domain.model.TextResource
import com.alhuda.app.core.domain.model.alarm.AlarmType
import com.alhuda.app.core.domain.model.alarm.ScheduledAlarm
import com.alhuda.app.core.domain.model.notification.AndroidNotificationCategory
import com.alhuda.app.core.domain.model.notification.AndroidNotificationConfig
import com.alhuda.app.core.domain.model.notification.NotificationButton
import com.alhuda.app.core.domain.model.notification.NotificationConfig
import com.alhuda.app.core.domain.model.notification.NotificationPressAction
import com.alhuda.app.core.domain.repository.AlarmRepository
import com.alhuda.app.core.domain.repository.NotificationRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.usecase.EnsureNotificationChannelsUseCase
import com.alhuda.app.core.domain.util.formatTime
import com.alhuda.app.core.presentation.navigation.Route
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class DndSilenceController @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val alarmRepository: AlarmRepository,
    private val notificationRepository: NotificationRepository,
) {

    suspend fun silence(minutes: Int): Boolean = applySilence(Clock.System.now().toEpochMilliseconds() + minutes * 60_000L)

    suspend fun reconcile() {
        val until = settingsRepository.data.first().silencedUntilMillis ?: return
        if (Clock.System.now().toEpochMilliseconds() >= until) {
            unsilence()
        } else {
            applySilence(until)
        }
    }

    private suspend fun applySilence(until: Long): Boolean {
        val settings = settingsRepository.data.first()
        val nm = context.getSystemService<NotificationManager>()
            ?.takeIf { it.isNotificationPolicyAccessGranted }

        var restoreFilter: Int? = settings.dndRestoreFilter
        if (nm != null) {
            if (restoreFilter == null) restoreFilter = nm.currentInterruptionFilter
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }
        val dndEngaged = nm != null

        settingsRepository.update {
            it.copy(silencedUntilMillis = until, dndRestoreFilter = restoreFilter)
        }
        if (dndEngaged) {
            alarmRepository.schedule(
                ScheduledAlarm(
                    id = AdhanContract.UNSILENCE_ALARM_ID,
                    triggerAtMillis = until,
                    action = AdhanContract.ACTION_UNSILENCE,
                    type = AlarmType.ExactAllowWhileIdle,
                ),
            )
        }

        postSilenceNotice(settings.formatTime(until))
        return dndEngaged
    }

    private suspend fun postSilenceNotice(untilFormatted: String) {
        val end = NotificationPressAction.Broadcast(
            action = AdhanContract.ACTION_UNSILENCE,
            requestCode = AdhanContract.ACTION_UNSILENCE.hashCode(),
        )
        notificationRepository.notify(
            NotificationConfig(
                id = AdhanContract.DND_ACTIVE_NOTIFICATION_ID,
                title = TextResource.StringResId(R.string.dnd_active_title),
                body = TextResource.StringResIdWithArgs(R.string.dnd_active_body, untilFormatted),
                android = AndroidNotificationConfig(
                    channelId = EnsureNotificationChannelsUseCase.DND_ACTIVE_CHANNEL_ID,
                    category = AndroidNotificationCategory.CATEGORY_STATUS,
                    onlyAlertOnce = true,
                    autoCancel = false,

                    pressAction = NotificationPressAction.Route(Route.Main.SilenceStatus),
                    actions = listOf(
                        NotificationButton(
                            title = TextResource.StringResId(R.string.dnd_active_end_action),
                            pressAction = end,
                        ),
                    ),
                    dismissAction = end,
                ),
            ),
        )
    }

    suspend fun unsilence() {
        val settings = settingsRepository.data.first()
        val nm = context.getSystemService<NotificationManager>()
        if (nm != null && nm.isNotificationPolicyAccessGranted) {
            nm.setInterruptionFilter(settings.dndRestoreFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL)
        }
        settingsRepository.update {
            it.copy(silencedUntilMillis = null, dndRestoreFilter = null)
        }
        alarmRepository.cancel(AdhanContract.UNSILENCE_ALARM_ID)
        notificationRepository.cancelNotification(AdhanContract.DND_ACTIVE_NOTIFICATION_ID)
    }
}
