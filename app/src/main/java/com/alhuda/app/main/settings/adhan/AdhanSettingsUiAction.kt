package com.alhuda.app.main.settings.adhan

import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import com.alhuda.app.core.domain.model.settings.AudioEntry
import com.alhuda.app.core.presentation.dialog.SchedulingPermission
import com.alhuda.app.core.presentation.navigation.Route
import kotlinx.datetime.DayOfWeek

sealed interface AdhanSettingsUiAction {

    data class OnGlobalMuezzinSelect(
        val entry: AudioEntry,
    ) : AdhanSettingsUiAction

    data class OnPreviewAudio(
        val entry: AudioEntry,

        val atAlarmVolume: Boolean = false,
    ) : AdhanSettingsUiAction

    object OnStopPreview : AdhanSettingsUiAction

    data class OnAddGlobalMuezzinFile(
        val filepath: String,
        val label: String,
    ) : AdhanSettingsUiAction

    data class OnAddPrayerMuezzinFile(
        val prayer: Prayer,
        val filepath: String,
        val label: String,
    ) : AdhanSettingsUiAction

    data class OnDeleteUserAudio(
        val entry: AudioEntry,
    ) : AdhanSettingsUiAction

    data class OnNotifyClick(
        val prayer: Prayer,
    ) : AdhanSettingsUiAction

    data class OnSoundClick(
        val prayer: Prayer,
    ) : AdhanSettingsUiAction

    data class OnCogClick(
        val prayer: Prayer,
        val route: Route = Route.Main.Settings.SoundAndNotifications.PrayerSchedule(prayer),
    ) : AdhanSettingsUiAction

    data class OnScheduleMuezzinChange(
        val prayer: Prayer,
        val entry: AudioEntry?,
    ) : AdhanSettingsUiAction

    data class OnScheduleSoundDayToggle(
        val prayer: Prayer,
        val day: DayOfWeek,
    ) : AdhanSettingsUiAction

    data class OnScheduleNotifyDayToggle(
        val prayer: Prayer,
        val day: DayOfWeek,
    ) : AdhanSettingsUiAction

    data class OnScheduleVibrationChange(
        val prayer: Prayer,
        val mode: VibrationMode?,
    ) : AdhanSettingsUiAction

    data class OnVibrationModeChange(
        val mode: VibrationMode,
    ) : AdhanSettingsUiAction

    data class OnShowUpcomingAlarmToggle(
        val enabled: Boolean,
    ) : AdhanSettingsUiAction

    data class OnUpcomingTimeChange(
        val minutes: Int,
    ) : AdhanSettingsUiAction

    data class OnShowNextInNotificationToggle(
        val enabled: Boolean,
    ) : AdhanSettingsUiAction

    data class OnBypassDndToggle(
        val enabled: Boolean,
    ) : AdhanSettingsUiAction

    data class OnPreferHeadphonesToggle(
        val enabled: Boolean,
    ) : AdhanSettingsUiAction

    data class OnVolumeButtonStopsAdhanToggle(
        val enabled: Boolean,
    ) : AdhanSettingsUiAction

    data class OnCustomVolumeToggle(
        val enabled: Boolean,
    ) : AdhanSettingsUiAction

    data class OnAlarmVolumeChange(
        val percent: Int,
    ) : AdhanSettingsUiAction

    data class OnAlarmVolumeDrag(
        val percent: Int,
    ) : AdhanSettingsUiAction

    data class OnGradualVolumeToggle(
        val enabled: Boolean,
    ) : AdhanSettingsUiAction

    data class OnDontShowAlarmScreenToggle(
        val enabled: Boolean,
    ) : AdhanSettingsUiAction

    data class OnForceLaunchAlarmActivityToggle(
        val enabled: Boolean,
    ) : AdhanSettingsUiAction

    data class OnAutoSilentOnDismissToggle(
        val enabled: Boolean,
    ) : AdhanSettingsUiAction

    data class OnAutoSilentDurationChange(
        val minutes: Int,
    ) : AdhanSettingsUiAction

    data class OnNotifyOnSkippedAdhanToggle(
        val enabled: Boolean,
    ) : AdhanSettingsUiAction

    object OnNotificationSettingsClick : AdhanSettingsUiAction
    object OnPlaybackSettingsClick : AdhanSettingsUiAction

    data class OnPermissionDontAskAgain(
        val permission: SchedulingPermission,
    ) : AdhanSettingsUiAction
}
