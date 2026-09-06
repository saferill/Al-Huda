package com.alhuda.app.main.settings.adhan.components

import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.presentation.navigation.Route
import com.alhuda.app.main.settings.adhan.AdhanSettingsUiAction

sealed interface AdhanScheduleRowUiAction {
    object OnNotifyClick : AdhanScheduleRowUiAction
    object OnSoundClick : AdhanScheduleRowUiAction
    object OnCogClick : AdhanScheduleRowUiAction
}

fun AdhanScheduleRowUiAction.toAdhanSettingsUiAction(
    prayer: Prayer,
    prayerScheduleRoute: Route = Route.Main.Settings.SoundAndNotifications.PrayerSchedule(prayer),
) = when (this) {
    AdhanScheduleRowUiAction.OnNotifyClick -> AdhanSettingsUiAction.OnNotifyClick(prayer)
    AdhanScheduleRowUiAction.OnSoundClick -> AdhanSettingsUiAction.OnSoundClick(prayer)
    AdhanScheduleRowUiAction.OnCogClick -> AdhanSettingsUiAction.OnCogClick(prayer, prayerScheduleRoute)
}
