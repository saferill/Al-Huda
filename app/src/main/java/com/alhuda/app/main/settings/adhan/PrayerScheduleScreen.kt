package com.alhuda.app.main.settings.adhan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.adhan.AdhanKey
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.adhan.i18n
import com.alhuda.app.core.domain.model.adhan.toAdhanKey
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import com.alhuda.app.core.domain.model.settings.AudioEntry
import com.alhuda.app.core.domain.model.settings.NOTIFICATION_AUDIO_ID
import com.alhuda.app.core.domain.model.settings.SILENT_AUDIO_ID
import com.alhuda.app.core.domain.model.settings.isResolvable
import com.alhuda.app.core.domain.model.settings.mapAdhanIdToEntry
import com.alhuda.app.core.presentation.AlHudaTheme
import com.alhuda.app.core.presentation.components.AudioPickerField
import com.alhuda.app.core.presentation.components.AudioPickerSection
import com.alhuda.app.core.presentation.components.BottomSelect
import com.alhuda.app.core.presentation.components.ScreenScaffold
import com.alhuda.app.core.presentation.components.SettingLabel
import com.alhuda.app.core.presentation.dialog.SchedulingPermissionSteps
import com.alhuda.app.core.presentation.dialog.isDontAskAgain
import com.alhuda.app.core.presentation.dialog.rememberSchedulingPermissionRequest
import com.alhuda.app.core.presentation.mapper.stringRes
import com.alhuda.app.core.presentation.navigation.NavigationController
import com.alhuda.app.main.settings.adhan.components.AdhanScheduleRowUiState
import com.alhuda.app.main.settings.adhan.components.AdhanToggleChip
import com.alhuda.app.main.settings.adhan.components.ChipAccent
import com.alhuda.app.main.settings.adhan.components.WeekdayChipRow

private const val DEFAULT_MUEZZIN_KEY = "__default__"
private const val DEFAULT_VIBRATION_KEY = "__default__"

@Composable
fun PrayerScheduleScreen(
    prayer: Prayer,
    uiState: AdhanSettingsUiState,
    onAction: (AdhanSettingsUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val globalDefaultMuezzin = uiState.settings.selectedAdhanEntries[AdhanKey.Default]

    val customMuezzin = uiState.settings.selectedAdhanEntries[prayer.toAdhanKey()]?.takeIf { it.isResolvable() }
    val soundDays = uiState.alarmSettings.getSoundSettings(prayer).selectedDays()
    val notifyDays = uiState.alarmSettings.getNotifSettings(prayer).selectedDays()
    val customVibration = uiState.alarmSettings.getVibrationSettings(prayer)

    val resources = LocalResources.current
    val defaultVibrationLabel = stringResource(R.string.use_default_vibration)
    val vibrationOptions = listOf<VibrationMode?>(null) + VibrationMode.entries

    val defaultLabel = stringResource(R.string.use_default_muezzin)
    val labelFn = audioEntryLabel()
    val userIds = uiState.settings.savedUserAudioEntries.map { it.id }.toSet()
    val muezzinSections = listOf<AudioPickerSection<AudioEntry?>>(
        AudioPickerSection(
            null,
            listOf<AudioEntry?>(null, mapAdhanIdToEntry(NOTIFICATION_AUDIO_ID), mapAdhanIdToEntry(SILENT_AUDIO_ID)),
        ),
        AudioPickerSection(stringResource(R.string.muezzin), uiState.settings.savedAdhanAudioEntries),
        AudioPickerSection(stringResource(R.string.your_sounds), uiState.settings.savedUserAudioEntries),
        AudioPickerSection(stringResource(R.string.device_sounds), uiState.deviceSounds),
    )

    val rowState = AdhanScheduleRowUiState.fromPrayerAlarmSettings(
        prayer,
        uiState.alarmSettings.getNotifSettings(prayer),
        uiState.alarmSettings.getSoundSettings(prayer),
    )

    val pendingRevert = remember { mutableStateOf<(() -> Unit)?>(null) }
    val requestPermissions = rememberSchedulingPermissionRequest(

        isDontAskAgain = { uiState.settings.isDontAskAgain(it) },
        onDontAskAgain = { onAction(AdhanSettingsUiAction.OnPermissionDontAskAgain(it)) },
        onComplete = { results -> if (!results.requiredAllGranted()) pendingRevert.value?.invoke() },
    )

    fun guardEnable(
        enabling: Boolean,
        revert: () -> Unit,
    ) {
        if (enabling) {
            pendingRevert.value = revert
            requestPermissions(SchedulingPermissionSteps.adhan)
        }
    }

    ScreenScaffold(
        title = prayer.i18n(),
        onBackClick = { NavigationController.navigateBack() },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding), Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AdhanToggleChip(
                label = stringResource(R.string.notification),
                state = rowState.notifyState,
                onClick = {
                    val enabling = rowState.notifyState != ToggleableState.On
                    onAction(AdhanSettingsUiAction.OnNotifyClick(prayer))
                    guardEnable(enabling) { onAction(AdhanSettingsUiAction.OnNotifyClick(prayer)) }
                },
            )
            AdhanToggleChip(
                label = stringResource(R.string.sound),
                state = rowState.soundState,
                onClick = {
                    val enabling = rowState.soundState != ToggleableState.On
                    val notifyWasOn = rowState.notifyState == ToggleableState.On
                    onAction(AdhanSettingsUiAction.OnSoundClick(prayer))
                    guardEnable(enabling) {
                        onAction(AdhanSettingsUiAction.OnSoundClick(prayer))

                        if (!notifyWasOn) onAction(AdhanSettingsUiAction.OnNotifyClick(prayer))
                    }
                },
            )
        }

        HorizontalDivider()

        Column {
            SettingLabel(stringResource(R.string.custom_muezzin))
            AudioPickerField(
                modifier = Modifier.fillMaxWidth(),
                sections = muezzinSections,
                selectedKey = customMuezzin?.id ?: DEFAULT_MUEZZIN_KEY,
                playingId = uiState.playingId,
                optionKey = { it?.id ?: DEFAULT_MUEZZIN_KEY },
                optionLabel = { it?.let(labelFn) ?: defaultLabel },
                optionSubtitle = { if (it == null) globalDefaultMuezzin?.let(labelFn) else null },

                optionPreviewKey = { it?.id ?: globalDefaultMuezzin?.id ?: DEFAULT_MUEZZIN_KEY },

                optionPreviewable = { it?.id != SILENT_AUDIO_ID },
                optionLeadingIcon = { if (it?.id == SILENT_AUDIO_ID) R.drawable.outline_volume_off else null },
                optionCanDelete = { it != null && it.id in userIds },
                onSelect = { onAction(AdhanSettingsUiAction.OnScheduleMuezzinChange(prayer, it)) },
                onPreview = { (it ?: globalDefaultMuezzin)?.let { e -> onAction(AdhanSettingsUiAction.OnPreviewAudio(e)) } },
                onStopPreview = { onAction(AdhanSettingsUiAction.OnStopPreview) },
                onAddLocalFile = { filepath, name ->
                    onAction(AdhanSettingsUiAction.OnAddPrayerMuezzinFile(prayer, filepath, name))
                },
                onDelete = { it?.let { e -> onAction(AdhanSettingsUiAction.OnDeleteUserAudio(e)) } },
            )
        }

        Column {
            SettingLabel(stringResource(R.string.vibration_mode))
            BottomSelect(
                modifier = Modifier.fillMaxWidth(),
                options = vibrationOptions,
                optionKey = { it?.name ?: DEFAULT_VIBRATION_KEY },
                optionLabel = { it?.let { mode -> resources.getString(mode.stringRes()) } ?: defaultVibrationLabel },
                selectedKey = customVibration?.name ?: DEFAULT_VIBRATION_KEY,
                onSelect = { onAction(AdhanSettingsUiAction.OnScheduleVibrationChange(prayer, it)) },
            )
        }

        Column {
            SettingLabel(stringResource(R.string.adhan))
            WeekdayChipRow(
                selected = soundDays,
                onToggle = { day ->
                    val enabling = day !in soundDays
                    val notifyHadDay = day in notifyDays
                    onAction(AdhanSettingsUiAction.OnScheduleSoundDayToggle(prayer, day))
                    guardEnable(enabling) {
                        onAction(AdhanSettingsUiAction.OnScheduleSoundDayToggle(prayer, day))

                        if (!notifyHadDay) onAction(AdhanSettingsUiAction.OnScheduleNotifyDayToggle(prayer, day))
                    }
                },
                accent = ChipAccent.Tertiary,
            )
        }

        HorizontalDivider()

        Column {
            SettingLabel(stringResource(R.string.notifications))
            WeekdayChipRow(
                selected = notifyDays,
                onToggle = { day ->
                    val enabling = day !in notifyDays
                    onAction(AdhanSettingsUiAction.OnScheduleNotifyDayToggle(prayer, day))
                    guardEnable(enabling) { onAction(AdhanSettingsUiAction.OnScheduleNotifyDayToggle(prayer, day)) }
                },
            )
        }
    }
}

@Preview
@Composable
private fun PrayerScheduleScreenPreview() {
    AlHudaTheme {
        PrayerScheduleScreen(
            prayer = Prayer.Fajr,
            uiState = AdhanSettingsUiState(),
            onAction = {},
        )
    }
}
