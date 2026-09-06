package com.alhuda.app.main.settings.troubleshoot.advanced

sealed interface AdvancedTroubleshootUiAction {
    data class OnAdaptiveChargingToggle(val value: Boolean) : AdvancedTroubleshootUiAction
}
