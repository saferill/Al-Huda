package com.alhuda.app.core.data.model

import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.calculation.CalculationSettings
import com.alhuda.app.core.domain.model.counter.Counter
import com.alhuda.app.core.domain.model.favorite_location.FavoriteLocation
import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.model.widget.CustomWidgetConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExportedSettingsV2(
    @SerialName("SETTINGS_STORAGE_V2")
    val settings: Settings,
    @SerialName("CALC_SETTINGS_STORAGE_V2")
    val calculationSettings: CalculationSettings,
    @SerialName("ALARM_SETTINGS_STORAGE_V2")
    val alarmSettings: AlarmSettings,
    @SerialName("COUNTER_STORAGE_V2")
    val counters: List<Counter>,
    @SerialName("REMINDER_STORAGE_V2")
    val reminders: List<Reminder>,
    @SerialName("FAVORITE_LOCATIONS_STORAGE_V2")
    val favoriteLocations: List<FavoriteLocation>,

    @SerialName("CUSTOM_WIDGET_STORAGE_V2")
    val customWidget: CustomWidgetConfig = CustomWidgetConfig(),
)
