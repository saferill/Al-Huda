package com.alhuda.app.alarm

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alhuda.app.R
import com.alhuda.app.adhan.AdhanContract
import com.alhuda.app.adhan.AdhanFiringHandler
import com.alhuda.app.core.data.locale.LocalizedResources
import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.settings.ThemeColor
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.playback.PlaybackService
import com.alhuda.app.reminder.ReminderFiringHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlarmFullscreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val adhanFiringHandler: AdhanFiringHandler,
    private val reminderFiringHandler: ReminderFiringHandler,
    alarmSettingsRepository: AlarmSettingsRepository,
    settingsRepository: SettingsRepository,
    private val localizedResources: LocalizedResources,
) : ViewModel() {

    private val dndAccessGranted: Boolean =
        context.getSystemService<NotificationManager>()?.isNotificationPolicyAccessGranted == true

    val isSounding: StateFlow<Boolean> = PlaybackService.activeAlarm
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlaybackService.activeAlarm.value != null)

    private val shownAlarm: StateFlow<PlaybackService.ActiveAlarm?> = PlaybackService.activeAlarm
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlaybackService.activeAlarm.value)

    private val alarmSettings: StateFlow<AlarmSettings> = alarmSettingsRepository.data
        .stateIn(viewModelScope, SharingStarted.Eagerly, AlarmSettings())

    val uiState: StateFlow<AlarmFullscreenUiState> =
        combine(shownAlarm, alarmSettings, settingsRepository.data.map { it.themeColor }, ::buildUiState)
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                buildUiState(shownAlarm.value, alarmSettings.value, ThemeColor.Default),
            )

    private fun buildUiState(
        alarm: PlaybackService.ActiveAlarm?,
        settings: AlarmSettings,
        themeColor: ThemeColor,
    ): AlarmFullscreenUiState {
        val isReminder = alarm?.isReminder == true

        val autoSilent = !isReminder && settings.autoSilentOnDismiss && dndAccessGranted
        return AlarmFullscreenUiState(
            header = alarm?.header?.takeIf { it.isNotBlank() }
                ?: localizedResources.current.getString(R.string.adhan_channel_name),
            title = alarm?.title?.takeIf { it.isNotBlank() }
                ?: alarm?.prayer?.let { localizedResources.current.getString(it.stringRes) }.orEmpty(),
            timeLabel = alarm?.timeLabel.orEmpty(),
            shortRemindMinutes = if (isReminder) 0 else AdhanContract.SHORT_REMIND_MINUTES,
            longRemindMinutes = if (isReminder) 0 else AdhanContract.LONG_REMIND_MINUTES,
            dismissAndSilentMinutes = if (!autoSilent && dndAccessGranted) AdhanContract.DISMISS_SILENT_MINUTES else 0,
            autoSilentOnDismiss = autoSilent,
            themeColor = themeColor,
        )
    }

    fun onAction(action: AlarmFullscreenUiAction) {
        val alarm = shownAlarm.value

        alarm?.let { PlaybackService.markAlarmHandled(it.id) }
        val isReminder = alarm?.isReminder == true
        when (action) {
            AlarmFullscreenUiAction.OnDismiss -> onDismiss(isReminder)
            AlarmFullscreenUiAction.OnDismissAndSilent -> onDismissAndSilent(isReminder)
            AlarmFullscreenUiAction.OnShortRemind -> onRemindLater(alarm, AdhanContract.SHORT_REMIND_MINUTES)
            AlarmFullscreenUiAction.OnLongRemind -> onRemindLater(alarm, AdhanContract.LONG_REMIND_MINUTES)
        }
    }

    private fun onDismiss(isReminder: Boolean) =
        if (isReminder) {
            reminderFiringHandler.dismissFromUi()
        } else {
            val settings = alarmSettings.value
            adhanFiringHandler.dismissFromUi(settings.autoSilentOnDismiss, settings.autoSilentDurationMinutes)
        }

    private fun onDismissAndSilent(isReminder: Boolean) =
        if (isReminder) {
            reminderFiringHandler.dismissAndSilentFromUi(AdhanContract.DISMISS_SILENT_MINUTES)
        } else {
            adhanFiringHandler.dismissAndSilentFromUi(AdhanContract.DISMISS_SILENT_MINUTES)
        }

    private fun onRemindLater(
        alarm: PlaybackService.ActiveAlarm?,
        minutes: Int,
    ) {
        alarm?.prayer?.let { adhanFiringHandler.remindLaterFromUi(it, minutes) }
    }
}
