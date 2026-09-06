package com.alhuda.app.main.settings.developer

sealed interface DeveloperUiAction {
    object OnFireAdhanNow : DeveloperUiAction
    object OnFireReminderNow : DeveloperUiAction
    object OnScheduleAdhanWithSound : DeveloperUiAction
    object OnScheduleAdhanNotifyOnly : DeveloperUiAction
    object OnPostUpcoming : DeveloperUiAction
    object OnVibrateShort : DeveloperUiAction
    object OnVibrateLong : DeveloperUiAction
    object OnStopVibration : DeveloperUiAction
    object OnUpdateWidgets : DeveloperUiAction
    object OnResetSilence : DeveloperUiAction
    object OnClearDeliveredTimestamps : DeveloperUiAction
    object OnDisableDeveloperMode : DeveloperUiAction
}
