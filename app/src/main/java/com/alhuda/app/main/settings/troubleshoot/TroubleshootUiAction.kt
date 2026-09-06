package com.alhuda.app.main.settings.troubleshoot

import android.app.Activity
import com.alhuda.app.core.presentation.navigation.Route

sealed interface TroubleshootUiAction {
    data class OnAppIsAllowedToKeepRunningClick(
        val activity: Activity?,
    ) : TroubleshootUiAction

    data class OnOpenPowerManagerSettingsClick(
        val activity: Activity?,
    ) : TroubleshootUiAction

    data class OnOpenAutostartSettingsClick(
        val activity: Activity?,
    ) : TroubleshootUiAction

    data class OnAdvancedSettingsClick(
        val route: Route,
    ) : TroubleshootUiAction

    object OnLifecycleChanged : TroubleshootUiAction
}
