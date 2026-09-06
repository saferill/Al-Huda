package com.alhuda.app.core.data.model.old

import com.alhuda.app.core.data.model.RestoreData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OldExportedSettings(
    @SerialName("SETTINGS_STORAGE")
    var settingsStorage: OldSettings,
    @SerialName("CALC_SETTINGS_STORAGE")
    var calcSettingsStorage: OldCalculationSettings,
    @SerialName("ALARM_SETTINGS_STORAGE")
    var alarmSettingsStorage: OldAlarmSettings,
    @SerialName("COUNTER_STORAGE")
    var counterStoreStorage: OldCounterStore,
    @SerialName("REMINDER_STORAGE")
    var reminderSettingsStorage: OldReminderStore,
    @SerialName("FAVORITE_LOCATIONS_STORAGE")
    var favoriteLocationsStorage: OldFavoriteLocationsStore,
)

fun OldExportedSettings.toRestoreData(): RestoreData {
    val calcState = calcSettingsStorage.state

    val favoriteLocations = (
        listOf(calcState.location?.toFavoriteLocation()) +
            favoriteLocationsStorage.state.locations.map { it.toFavoriteLocation() }
        ).filterNotNull()

    return RestoreData(
        settings = settingsStorage.state.toSettings(),
        calculationSettings = calcState.toCalculationSettings(),
        alarmSettings = alarmSettingsStorage.state.toAlarmSettings(),
        counters = counterStoreStorage.state.counters.map { it.toCounter() },
        reminders = reminderSettingsStorage.state.reminders.map { it.toReminder() },
        favoriteLocations = favoriteLocations,
    )
}
