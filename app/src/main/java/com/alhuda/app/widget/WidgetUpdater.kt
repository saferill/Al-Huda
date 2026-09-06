package com.alhuda.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.alarm.AlarmType
import com.alhuda.app.core.domain.model.alarm.ScheduledAlarm
import com.alhuda.app.core.domain.model.notification.AndroidNotificationCategory
import com.alhuda.app.core.domain.model.notification.AndroidNotificationConfig
import com.alhuda.app.core.domain.model.notification.NotificationConfig
import com.alhuda.app.core.domain.model.settings.NotificationWidgetLayout
import com.alhuda.app.core.domain.model.widget.CustomWidgetData
import com.alhuda.app.core.domain.model.widget.NextPrayerWidgetData
import com.alhuda.app.core.domain.model.widget.WidgetData
import com.alhuda.app.core.domain.repository.AlarmRepository
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.CustomWidgetConfigRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.NotificationRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.usecase.BuildCustomWidgetDataUseCase
import com.alhuda.app.core.domain.usecase.BuildNextPrayerWidgetDataUseCase
import com.alhuda.app.core.domain.usecase.BuildWidgetDataUseCase
import com.alhuda.app.core.domain.usecase.EnsureNotificationChannelsUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class WidgetUpdater @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val customWidgetConfigRepository: CustomWidgetConfigRepository,
    private val buildWidgetDataUseCase: BuildWidgetDataUseCase,
    private val buildNextPrayerWidgetDataUseCase: BuildNextPrayerWidgetDataUseCase,
    private val buildCustomWidgetDataUseCase: BuildCustomWidgetDataUseCase,
    private val alarmRepository: AlarmRepository,
    private val notificationRepository: NotificationRepository,
) {
    private val appWidgetManager by lazy { AppWidgetManager.getInstance(context) }
    private val mutex = Mutex()

    private companion object {
        const val TAG = "WidgetUpdater"
    }

    suspend fun update() =
        mutex.withLock {
            val settings = settingsRepository.data.first()
            val calcSettings = calculationSettingsRepository.data.first()
            val locations = favoriteLocationsRepository.data.first()
            val location = locations.firstOrNull { it.id == calcSettings.locationId }?.locationDetail
            val now = Clock.System.now()

            val adaptiveSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

            val screenIds = appWidgetManager.getAppWidgetIds(ComponentName(context, PrayerTimesWidget::class.java))
            val nextIds = appWidgetManager.getAppWidgetIds(ComponentName(context, NextPrayerWidget::class.java))
            val customIds = appWidgetManager.getAppWidgetIds(ComponentName(context, CustomWidget::class.java))

            val showNotif = settings.showWidget
            val notifLayout = settings.notificationWidgetLayout

            val data = if (screenIds.isNotEmpty() || showNotif) {
                buildWidgetDataUseCase(now, settings, calcSettings, location)
                    ?.let { if (adaptiveSupported) it else it.copy(adaptiveTheme = false) }
            } else {
                null
            }
            val nextData = if (nextIds.isNotEmpty() || (showNotif && notifLayout == NotificationWidgetLayout.Compact)) {
                buildNextPrayerWidgetDataUseCase(now, settings, calcSettings, location)
                    ?.let { if (adaptiveSupported) it else it.copy(adaptiveTheme = false) }
            } else {
                null
            }
            val customData = if (customIds.isNotEmpty() || (showNotif && notifLayout == NotificationWidgetLayout.Custom)) {
                val customConfig = customWidgetConfigRepository.data.first()
                buildCustomWidgetDataUseCase(now, settings, calcSettings, location, customConfig, locations)
            } else {
                null
            }

            val configHintRes = if (calcSettings.parameters == null) {
                R.string.set_calculation_hint
            } else {
                R.string.set_location_hint
            }

            WidgetRenderCache.lastData = data
            if (data == null) {
                notificationRepository.cancelNotification(WidgetContract.NOTIFICATION_ID)
                if (screenIds.isNotEmpty()) {
                    appWidgetManager.updateAppWidget(screenIds, WidgetRenderer.buildHint(context, configHintRes))
                }
            } else {
                if (screenIds.isNotEmpty()) {
                    appWidgetManager.updateAppWidget(screenIds, WidgetRenderer.buildScreenWidget(context, data))
                }
                updateNotification(data, nextData, customData)
            }

            NextPrayerRenderCache.lastData = nextData
            if (nextData != null) {
                if (nextIds.isNotEmpty()) {
                    appWidgetManager.updateAppWidget(nextIds, WidgetRenderer.buildNextPrayerWidget(context, nextData))
                }
            } else if (nextIds.isNotEmpty()) {
                appWidgetManager.updateAppWidget(nextIds, WidgetRenderer.buildHint(context, configHintRes))
            }

            CustomWidgetRenderCache.lastData = customData
            if (customData != null) {
                customIds.forEach { id ->
                    appWidgetManager.updateAppWidget(
                        id,
                        CustomWidgetRenderer.build(context, customData, CustomWidgetPageState.get(id), id),
                    )
                }
            } else if (customIds.isNotEmpty()) {

                val hint = CustomWidgetRenderer.buildConfigHint(context, configHintRes)
                customIds.forEach { id -> appWidgetManager.updateAppWidget(id, hint) }
            }

            val compactNotificationShown = data != null && data.showNotification &&
                data.notificationLayout == NotificationWidgetLayout.Compact && nextData != null
            val nextUpdateCandidates = buildList {
                if (data != null && (screenIds.isNotEmpty() || data.showNotification)) {
                    data.nextUpdateAtMillis?.let { add(it) }
                }

                if (nextData != null && (nextIds.isNotEmpty() || compactNotificationShown)) {
                    nextData.nextUpdateAtMillis?.let { add(it) }
                }

                if (customData != null) {
                    customData.nextUpdateAtMillis?.let { add(it) }
                }
            }
            scheduleNextRedraw(nextUpdateCandidates.minOrNull())
        }

    private suspend fun updateNotification(
        data: WidgetData,
        nextData: NextPrayerWidgetData?,
        customData: CustomWidgetData?,
    ) {
        if (!data.showNotification) {
            notificationRepository.cancelNotification(WidgetContract.NOTIFICATION_ID)
            return
        }

        val small: RemoteViews
        val big: RemoteViews
        if (data.notificationLayout == NotificationWidgetLayout.Custom && customData != null) {

            val custom = CustomWidgetRenderer.build(context, customData.copy(pages = emptyList()))
            small = custom
            big = custom
        } else if (data.notificationLayout == NotificationWidgetLayout.Compact && nextData != null) {
            small = WidgetRenderer.buildNextPrayerNotification(context, nextData, expanded = false)
            big = WidgetRenderer.buildNextPrayerNotification(context, nextData, expanded = true)
        } else {
            small = WidgetRenderer.build(context, notifSmallLayout(data.adaptiveTheme), data)
            big = WidgetRenderer.build(context, notifBigLayout(data.adaptiveTheme, data.showCountdown), data)
        }
        notificationRepository.notify(
            NotificationConfig(
                id = WidgetContract.NOTIFICATION_ID,
                android = AndroidNotificationConfig(
                    channelId = EnsureNotificationChannelsUseCase.WIDGET_CHANNEL_ID,
                    ongoing = true,
                    category = AndroidNotificationCategory.CATEGORY_STATUS,
                    onlyAlertOnce = true,
                    autoCancel = false,
                    showTimestamp = false,
                    sortKey = "-1",
                    group = WidgetContract.NOTIFICATION_GROUP,
                    customContentView = small,
                    customBigContentView = big,
                ),
            ),
        )
    }

    private suspend fun scheduleNextRedraw(nextMillis: Long?) {
        if (nextMillis == null) {
            alarmRepository.cancel(WidgetContract.REDRAW_ALARM_ID)
            return
        }

        val triggerAt = nextMillis + 1_000
        val now = System.currentTimeMillis()
        if (triggerAt <= now) {

            Log.w(TAG, "Skipping widget redraw: non-future target nextMillis=$nextMillis now=$now")
            alarmRepository.cancel(WidgetContract.REDRAW_ALARM_ID)
            return
        }
        Log.i(TAG, "Next widget redraw in ${(triggerAt - now) / 1000}s")

        alarmRepository.schedule(
            ScheduledAlarm(
                id = WidgetContract.REDRAW_ALARM_ID,
                triggerAtMillis = triggerAt,
                action = WidgetContract.ACTION_WIDGET_UPDATE,
                type = AlarmType.Exact,
                wakeup = false,
            ),
        )
    }

    private fun notifSmallLayout(adaptive: Boolean): Int =
        if (adaptive) R.layout.notif_widget_small_adaptive else R.layout.notif_widget_small

    private fun notifBigLayout(
        adaptive: Boolean,
        countdown: Boolean,
    ): Int =
        when {
            countdown && adaptive -> R.layout.notif_widget_big_countdown_adaptive
            countdown -> R.layout.notif_widget_big_countdown
            adaptive -> R.layout.notif_widget_big_adaptive
            else -> R.layout.notif_widget_big
        }
}
