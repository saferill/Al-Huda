package com.alhuda.app.main.settings.widget

import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.settings.NotificationWidgetLayout
import com.alhuda.app.core.domain.model.settings.WidgetCityNamePos

sealed interface WidgetSettingsUiAction {
    data class OnShowNotificationWidgetToggle(
        val value: Boolean,
    ) : WidgetSettingsUiAction

    data class OnNotificationLayoutChange(
        val value: NotificationWidgetLayout,
    ) : WidgetSettingsUiAction

    data class OnShowCountdownToggle(
        val value: Boolean,
    ) : WidgetSettingsUiAction

    data class OnAdaptiveThemeToggle(
        val value: Boolean,
    ) : WidgetSettingsUiAction

    data class OnCityNamePosChange(
        val value: WidgetCityNamePos,
    ) : WidgetSettingsUiAction

    data class OnSwapLayoutDirectionToggle(
        val value: Boolean,
    ) : WidgetSettingsUiAction

    data class OnHighlightCurrentPrayerToggle(
        val value: Boolean,
    ) : WidgetSettingsUiAction

    data class OnHijriDayStartsAtMaghribToggle(
        val value: Boolean,
    ) : WidgetSettingsUiAction

    data class OnPrayerVisibilityChange(
        val prayer: Prayer,
        val visible: Boolean,
    ) : WidgetSettingsUiAction

    data class OnCountdownPrayerToggle(
        val prayer: Prayer,
        val enabled: Boolean,
    ) : WidgetSettingsUiAction
}
