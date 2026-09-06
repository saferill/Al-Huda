package com.alhuda.app.main.settings.developer

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alhuda.app.R
import com.alhuda.app.adhan.AdhanFiringHandler
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.presentation.navigation.NavigationController
import com.alhuda.app.core.util.device.VibrationController
import com.alhuda.app.reminder.ReminderFiringHandler
import com.alhuda.app.widget.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeveloperViewModel
@Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val adhanFiringHandler: AdhanFiringHandler,
    private val reminderFiringHandler: ReminderFiringHandler,
    private val widgetUpdater: WidgetUpdater,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private companion object {
        const val TEST_DELAY_SECONDS = 10
    }

    fun onAction(action: DeveloperUiAction) {
        when (action) {
            DeveloperUiAction.OnFireAdhanNow -> onFireAdhanNow()
            DeveloperUiAction.OnFireReminderNow -> onFireReminderNow()
            DeveloperUiAction.OnScheduleAdhanWithSound -> onScheduleAdhanWithSound()
            DeveloperUiAction.OnScheduleAdhanNotifyOnly -> onScheduleAdhanNotifyOnly()
            DeveloperUiAction.OnPostUpcoming -> onPostUpcoming()
            DeveloperUiAction.OnVibrateShort -> onVibrateShort()
            DeveloperUiAction.OnVibrateLong -> onVibrateLong()
            DeveloperUiAction.OnStopVibration -> onStopVibration()
            DeveloperUiAction.OnUpdateWidgets -> onUpdateWidgets()
            DeveloperUiAction.OnResetSilence -> onResetSilence()
            DeveloperUiAction.OnClearDeliveredTimestamps -> onClearDeliveredTimestamps()
            DeveloperUiAction.OnDisableDeveloperMode -> onDisableDeveloperMode()
        }
    }

    private fun onFireAdhanNow() {
        viewModelScope.launch { adhanFiringHandler.devFireNow() }
    }

    private fun onFireReminderNow() {
        viewModelScope.launch { reminderFiringHandler.devFireNow() }
    }

    private fun onScheduleAdhanWithSound() = scheduleAdhan(playSound = true)

    private fun onScheduleAdhanNotifyOnly() = scheduleAdhan(playSound = false)

    private fun onPostUpcoming() {
        viewModelScope.launch { adhanFiringHandler.devPostUpcoming() }
    }

    private fun onVibrateShort() = VibrationController.vibrate(context, VibrationMode.Once)

    private fun onVibrateLong() = VibrationController.vibrate(context, VibrationMode.Continuous)

    private fun onStopVibration() = VibrationController.stop(context)

    private fun onUpdateWidgets() {
        viewModelScope.launch { widgetUpdater.update() }
    }

    private fun onResetSilence() {
        viewModelScope.launch {
            adhanFiringHandler.onUnsilence()
            toast(context.getString(R.string.dev_silence_reset_toast))
        }
    }

    private fun onClearDeliveredTimestamps() {
        viewModelScope.launch {
            settingsRepository.update { it.copy(deliveredAlarmTimestamps = emptyMap()) }
            toast(context.getString(R.string.dev_delivered_cleared_toast))
        }
    }

    private fun onDisableDeveloperMode() {
        viewModelScope.launch {
            settingsRepository.update { it.copy(devMode = false) }
            NavigationController.navigateBack()
        }
    }

    private fun scheduleAdhan(playSound: Boolean) {
        viewModelScope.launch {
            adhanFiringHandler.devScheduleAdhan(playSound, TEST_DELAY_SECONDS)
            toast(context.getString(R.string.dev_scheduled_toast, TEST_DELAY_SECONDS))
        }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
