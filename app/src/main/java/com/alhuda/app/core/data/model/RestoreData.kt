package com.alhuda.app.core.data.model

import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.calculation.CalculationSettings
import com.alhuda.app.core.domain.model.counter.Counter
import com.alhuda.app.core.domain.model.favorite_location.FavoriteLocation
import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.model.widget.CustomWidgetConfig

data class RestoreData(
    val settings: Settings,
    val calculationSettings: CalculationSettings,
    val alarmSettings: AlarmSettings,
    val counters: List<Counter>,
    val reminders: List<Reminder>,
    val favoriteLocations: List<FavoriteLocation>,

    val customWidget: CustomWidgetConfig = CustomWidgetConfig(),
)

fun ExportedSettingsV2.toRestoreData(): RestoreData =
    RestoreData(
        settings = settings,
        calculationSettings = calculationSettings,
        alarmSettings = alarmSettings,
        counters = counters,
        reminders = reminders,
        favoriteLocations = favoriteLocations,
        customWidget = customWidget,
    )
