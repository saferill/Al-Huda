package com.alhuda.app.main.settings.adhan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.adhan.AdhanKey
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.model.settings.getDefaultAdhanEntries
import com.alhuda.app.core.presentation.AlHudaTheme
import com.alhuda.app.core.presentation.components.ACard
import com.alhuda.app.core.presentation.components.BottomSelect
import com.alhuda.app.core.presentation.components.InformationRow
import com.alhuda.app.core.presentation.components.MinutesSelect
import com.alhuda.app.core.presentation.components.PreviewIconButton
import com.alhuda.app.core.presentation.components.PrimaryButton
import com.alhuda.app.core.presentation.components.ScreenScaffold
import com.alhuda.app.core.presentation.components.SettingHeader
import com.alhuda.app.core.presentation.components.SettingSwitch
import com.alhuda.app.core.presentation.dialog.SchedulingPermission
import com.alhuda.app.core.presentation.dialog.SchedulingPermissionSteps
import com.alhuda.app.core.presentation.dialog.isSchedulingPermissionGranted
import com.alhuda.app.core.presentation.dialog.rememberSchedulingPermissionRequest
import com.alhuda.app.core.presentation.mapper.stringRes
import com.alhuda.app.core.presentation.navigation.NavigationController
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhanSettingsScreen(
    uiState: AdhanSettingsUiState,
    onAction: (AdhanSettingsUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        title = stringResource(R.string.alarm_settings_title),
        onBackClick = { NavigationController.navigateBack() },
        modifier = modifier,
    ) { AdhanSettingsContent(uiState, onAction) }
}

private val UPCOMING_TIME_OPTIONS = listOf(5, 10, 15, 30, 60, 90)

@Composable
private fun ColumnScope.AdhanSettingsContent(
    uiState: AdhanSettingsUiState,
    onAction: (AdhanSettingsUiAction) -> Unit,
) {
    NotificationsCard(uiState, onAction)
    VibrationCard(uiState, onAction)
    PlaybackCard(uiState, onAction)
    VolumeCard(uiState, onAction)
    GradualVolumeCard(uiState, onAction)
    AutoSilentCard(uiState, onAction)
    DisplayCard(uiState, onAction)
}

@Composable
private fun VolumeCard(
    uiState: AdhanSettingsUiState,
    onAction: (AdhanSettingsUiAction) -> Unit,
) {
    val alarmVolume = uiState.settings.alarmVolume

    ACard { cardPadding ->
        Column(Modifier.padding(cardPadding)) {
            SettingSwitch(
                title = stringResource(R.string.custom_alarm_volume),
                subtitle = stringResource(R.string.custom_alarm_volume_help),
                checked = alarmVolume != null,
                onCheckedChange = { onAction(AdhanSettingsUiAction.OnCustomVolumeToggle(it)) },
            )
            AnimatedVisibility(visible = alarmVolume != null) {

                var lastVolume by remember { mutableIntStateOf(alarmVolume ?: 0) }
                if (alarmVolume != null) lastVolume = alarmVolume
                VolumeSliderRow(lastVolume, uiState, onAction)
            }
        }
    }
}

@Composable
private fun GradualVolumeCard(
    uiState: AdhanSettingsUiState,
    onAction: (AdhanSettingsUiAction) -> Unit,
) {
    SettingsCard {
        SettingSwitch(
            title = stringResource(R.string.gradual_alarm_volume),
            subtitle = stringResource(R.string.gradual_alarm_volume_help),
            checked = uiState.settings.gradualAlarmVolume,
            onCheckedChange = { onAction(AdhanSettingsUiAction.OnGradualVolumeToggle(it)) },
        )
    }
}

@Composable
private fun VolumeSliderRow(
    alarmVolume: Int,
    uiState: AdhanSettingsUiState,
    onAction: (AdhanSettingsUiAction) -> Unit,
) {

    val defaultAdhan = uiState.settings.selectedAdhanEntries[AdhanKey.Default] ?: getDefaultAdhanEntries()[0]

    var sliderValue by remember(alarmVolume) { mutableFloatStateOf(alarmVolume.toFloat()) }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.element_padding)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Slider(
            value = sliderValue,
            onValueChange = { value ->

                if (value.roundToInt() != sliderValue.roundToInt()) {
                    onAction(AdhanSettingsUiAction.OnAlarmVolumeDrag(value.roundToInt()))
                }
                sliderValue = value
            },
            onValueChangeFinished = { onAction(AdhanSettingsUiAction.OnAlarmVolumeChange(sliderValue.roundToInt())) },
            valueRange = 0f..100f,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(dimensionResource(R.dimen.element_padding)))
        Text(
            "${sliderValue.roundToInt()}%",
            style = MaterialTheme.typography.titleMedium,
        )
        PreviewIconButton(
            playing = uiState.playingId == defaultAdhan.id,
            onToggle = {
                if (uiState.playingId == defaultAdhan.id) {
                    onAction(AdhanSettingsUiAction.OnStopPreview)
                } else {
                    onAction(AdhanSettingsUiAction.OnPreviewAudio(defaultAdhan, atAlarmVolume = true))
                }
            },
        )
    }
}

@Composable
private fun VibrationCard(
    uiState: AdhanSettingsUiState,
    onAction: (AdhanSettingsUiAction) -> Unit,
) {
    val resources = LocalResources.current
    SettingsCard {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.tiny_padding))) {
            SettingHeader(stringResource(R.string.vibration_mode), stringResource(R.string.vibration_mode_help))
            BottomSelect(
                modifier = Modifier.fillMaxWidth(),
                options = VibrationMode.entries,
                optionKey = { it.name },
                optionLabel = { resources.getString(it.stringRes()) },
                selectedKey = uiState.alarmSettings.vibrationMode.name,
                onSelect = { onAction(AdhanSettingsUiAction.OnVibrationModeChange(it)) },
            )
        }
    }
}

@Composable
private fun NotificationsCard(
    uiState: AdhanSettingsUiState,
    onAction: (AdhanSettingsUiAction) -> Unit,
) {
    SettingsCard {
        SettingSwitch(
            title = stringResource(R.string.show_upcoming_alarm),
            subtitle = stringResource(R.string.show_upcoming_alarm_help),
            checked = !uiState.alarmSettings.dontNotifyUpcoming,
            onCheckedChange = { onAction(AdhanSettingsUiAction.OnShowUpcomingAlarmToggle(it)) },
        )
        MinutesSelect(
            modifier = Modifier.fillMaxWidth(),
            options = UPCOMING_TIME_OPTIONS,
            selected = uiState.alarmSettings.preAlarmMinutesBefore,
            onSelect = { onAction(AdhanSettingsUiAction.OnUpcomingTimeChange(it)) },
            label = { Text(stringResource(R.string.custom_upcoming_time)) },
            supportingText = { Text(stringResource(R.string.custom_upcoming_time_help)) },
        )
        SettingSwitch(
            title = stringResource(R.string.show_next_in_notification),
            subtitle = stringResource(R.string.show_next_in_notification_help),
            checked = uiState.alarmSettings.showNextPrayerTime,
            onCheckedChange = { onAction(AdhanSettingsUiAction.OnShowNextInNotificationToggle(it)) },
        )
        SettingSwitch(
            title = stringResource(R.string.notify_on_skipped_adhan),
            subtitle = stringResource(R.string.notify_on_skipped_adhan_help),
            checked = uiState.alarmSettings.notifyOnSkippedAdhan,
            onCheckedChange = { onAction(AdhanSettingsUiAction.OnNotifyOnSkippedAdhanToggle(it)) },
        )
    }
}

@Composable
private fun PlaybackCard(
    uiState: AdhanSettingsUiState,
    onAction: (AdhanSettingsUiAction) -> Unit,
) {
    val requestDndAccess = rememberSchedulingPermissionRequest(
        isDontAskAgain = { false },
        onDontAskAgain = {},
        onComplete = { results ->

            if (!results.requiredAllGranted()) onAction(AdhanSettingsUiAction.OnBypassDndToggle(false))
        },
    )
    SettingsCard {
        SettingSwitch(
            title = stringResource(R.string.use_headphones),
            subtitle = stringResource(R.string.use_headphones_help),
            checked = uiState.settings.preferExternalAudioDevice,
            onCheckedChange = { onAction(AdhanSettingsUiAction.OnPreferHeadphonesToggle(it)) },
        )
        SettingSwitch(
            title = stringResource(R.string.volume_button_stops_adhan),
            subtitle = stringResource(R.string.volume_button_stops_adhan_help),
            checked = uiState.settings.volumeButtonStopsAdhan,
            onCheckedChange = { onAction(AdhanSettingsUiAction.OnVolumeButtonStopsAdhanToggle(it)) },
        )
        SettingSwitch(
            title = stringResource(R.string.bypass_dnd),
            subtitle = stringResource(R.string.bypass_dnd_help),
            checked = uiState.settings.bypassDnd,
            onCheckedChange = { enabled ->
                onAction(AdhanSettingsUiAction.OnBypassDndToggle(enabled))

                if (enabled) requestDndAccess(SchedulingPermissionSteps.dndBypass)
            },
        )
    }
}

private val AUTO_SILENT_DURATION_OPTIONS = listOf(15, 30, 45, 60, 90, 120)

@Composable
private fun AutoSilentCard(
    uiState: AdhanSettingsUiState,
    onAction: (AdhanSettingsUiAction) -> Unit,
) {
    val requestDndAccess = rememberSchedulingPermissionRequest(
        isDontAskAgain = { false },
        onDontAskAgain = {},
        onComplete = { results ->

            if (!results.requiredAllGranted()) onAction(AdhanSettingsUiAction.OnAutoSilentOnDismissToggle(false))
        },
    )

    ACard { cardPadding ->
        Column(Modifier.padding(cardPadding)) {
            SettingSwitch(
                title = stringResource(R.string.auto_silent_on_dismiss),
                subtitle = stringResource(R.string.auto_silent_on_dismiss_help),
                checked = uiState.alarmSettings.autoSilentOnDismiss,
                onCheckedChange = { enabled ->
                    onAction(AdhanSettingsUiAction.OnAutoSilentOnDismissToggle(enabled))
                    if (enabled) requestDndAccess(SchedulingPermissionSteps.dndBypass)
                },
            )
            AnimatedVisibility(visible = uiState.alarmSettings.autoSilentOnDismiss) {
                MinutesSelect(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.element_padding)),
                    options = AUTO_SILENT_DURATION_OPTIONS,
                    selected = uiState.alarmSettings.autoSilentDurationMinutes,
                    onSelect = { onAction(AdhanSettingsUiAction.OnAutoSilentDurationChange(it)) },
                    label = { Text(stringResource(R.string.auto_silent_duration)) },
                    supportingText = { Text(stringResource(R.string.auto_silent_duration_help)) },
                )
            }
        }
    }
}

@Composable
private fun DisplayCard(
    uiState: AdhanSettingsUiState,
    onAction: (AdhanSettingsUiAction) -> Unit,
) {
    val context = LocalContext.current

    var canDisplayOverApps by remember {
        mutableStateOf(isSchedulingPermissionGranted(context, SchedulingPermission.DisplayOverApps))
    }
    LifecycleResumeEffect(Unit) {
        canDisplayOverApps = isSchedulingPermissionGranted(context, SchedulingPermission.DisplayOverApps)
        onPauseOrDispose {}
    }
    val requestDisplayOverApps = rememberSchedulingPermissionRequest(
        isDontAskAgain = { false },
        onDontAskAgain = {},
        onComplete = { results ->
            val granted = results.granted(SchedulingPermission.DisplayOverApps)
            canDisplayOverApps = granted

            if (granted) onAction(AdhanSettingsUiAction.OnForceLaunchAlarmActivityToggle(true))
        },
    )
    SettingsCard {

        SettingSwitch(
            title = stringResource(R.string.dont_show_alarm_screen),
            subtitle = stringResource(R.string.dont_show_alarm_screen_help),
            checked = uiState.alarmSettings.dontTurnOnScreen,
            onCheckedChange = { onAction(AdhanSettingsUiAction.OnDontShowAlarmScreenToggle(it)) },
        )
        SettingSwitch(
            title = stringResource(R.string.force_launch_alarm_screen),
            subtitle = stringResource(R.string.force_launch_alarm_screen_help),
            checked = uiState.settings.forceLaunchAlarmActivity,
            onCheckedChange = { enabled ->
                when {
                    !enabled -> onAction(AdhanSettingsUiAction.OnForceLaunchAlarmActivityToggle(false))

                    canDisplayOverApps -> onAction(AdhanSettingsUiAction.OnForceLaunchAlarmActivityToggle(true))

                    else -> requestDisplayOverApps(SchedulingPermissionSteps.forceLaunchAlarm)
                }
            },
        )

        if (uiState.settings.forceLaunchAlarmActivity && !canDisplayOverApps) {
            DisplayOverAppsWarning(
                onGrantClick = { requestDisplayOverApps(SchedulingPermissionSteps.forceLaunchAlarm) },
            )
        }
    }
}

@Composable
private fun DisplayOverAppsWarning(onGrantClick: () -> Unit) {

    ACard(tonalElevation = 2.dp) { innerCardPadding ->
        InformationRow(
            Modifier
                .fillMaxWidth()
                .padding(innerCardPadding),

            iconDescription = null,
            iconRes = R.drawable.baseline_close_24,
            contentColor = MaterialTheme.colorScheme.error,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding))) {
                Text(stringResource(R.string.force_launch_alarm_screen_permission_missing))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    PrimaryButton(onGrantClick) {
                        Text(stringResource(R.string.open_settings_label))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    ACard { cardPadding ->
        Column(
            Modifier.padding(cardPadding),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
            content = content,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00585A)
@Preview(showBackground = true, backgroundColor = 0xFF00585A, device = Devices.TABLET)
@Composable
private fun AdhanSettingsPreview() {
    AlHudaTheme {
        AdhanSettingsScreen(uiState = AdhanSettingsUiState(), onAction = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00585A)
@Composable
private fun NotificationsCardPreview() {
    AlHudaTheme {
        PreviewPart { NotificationsCard(AdhanSettingsUiState(), onAction = {}) }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00585A)
@Composable
private fun VibrationCardPreview() {
    AlHudaTheme {
        PreviewPart { VibrationCard(AdhanSettingsUiState(), onAction = {}) }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00585A)
@Composable
private fun PlaybackCardPreview() {
    AlHudaTheme {
        PreviewPart { PlaybackCard(AdhanSettingsUiState(), onAction = {}) }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00585A)
@Composable
private fun VolumeCardPreview() {
    AlHudaTheme {
        PreviewPart {
            VolumeCard(
                AdhanSettingsUiState(settings = Settings(selectedLocale = "en", alarmVolume = 70)),
                onAction = {},
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00585A)
@Composable
private fun GradualVolumeCardPreview() {
    AlHudaTheme {
        PreviewPart {
            GradualVolumeCard(
                AdhanSettingsUiState(settings = Settings(selectedLocale = "en", gradualAlarmVolume = true)),
                onAction = {},
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00585A)
@Composable
private fun AutoSilentCardPreview() {
    AlHudaTheme {
        PreviewPart { AutoSilentCard(AdhanSettingsUiState(), onAction = {}) }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00585A)
@Composable
private fun DisplayCardPreview() {
    AlHudaTheme {
        PreviewPart { DisplayCard(AdhanSettingsUiState(), onAction = {}) }
    }
}

@Composable
internal fun PreviewPart(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.padding(dimensionResource(R.dimen.page_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
        content = content,
    )
}
