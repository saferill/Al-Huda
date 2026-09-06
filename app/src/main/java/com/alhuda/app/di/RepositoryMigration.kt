package com.alhuda.app.di

import com.alhuda.app.core.data.model.old.LegacyStorageKeys
import com.alhuda.app.core.data.model.old.OldAlarmSettings
import com.alhuda.app.core.data.model.old.OldAlarmSettingsState
import com.alhuda.app.core.data.model.old.OldCalculationSettings
import com.alhuda.app.core.data.model.old.OldCalculationSettingsState
import com.alhuda.app.core.data.model.old.OldCounterStore
import com.alhuda.app.core.data.model.old.OldCounterStoreState
import com.alhuda.app.core.data.model.old.OldExportedSettings
import com.alhuda.app.core.data.model.old.OldFavoriteLocationsStore
import com.alhuda.app.core.data.model.old.OldFavoriteLocationsStoreState
import com.alhuda.app.core.data.model.old.OldReminderStore
import com.alhuda.app.core.data.model.old.OldReminderStoreState
import com.alhuda.app.core.data.model.old.OldSettings
import com.alhuda.app.core.data.model.old.OldSettingsState
import com.alhuda.app.core.data.model.old.toRestoreData
import com.alhuda.app.core.data.repository.RestoreApplier
import com.alhuda.app.core.domain.repository.NotificationChannelManager
import com.tencent.mmkv.MMKV
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.meypod.adhan_kotlin.MidnightMethod
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class RepositoryMigrationRunner
@Inject
constructor(
    private val mmkv: MMKV,
    @param:Named("storage") private val storageJson: Json,
    private val restoreApplier: RestoreApplier,
    private val notificationChannelManager: NotificationChannelManager,
) {
    private companion object {

        val LEGACY_CHANNEL_IDS = listOf(
            "adhan-channel-1",
            "adhan-channel",
            "adhan-dnd-channel-1",
            "adhan-dnd-channel",
            "pre-adhan-channel",
            "widget-channel",
            "widget-update-channel",
            "reminder-channel-1",
            "reminder-channel",
            "reminder-dnd-channel-1",
            "reminder-dnd-channel",
            "pre-reminder-channel",
            "important-channel",
        )
    }

    suspend fun run() {
        LEGACY_CHANNEL_IDS.forEach { notificationChannelManager.deleteChannel(it) }
        restoreApplier.apply(readLegacyStores().toRestoreData())
    }

    private fun readLegacyStores(): OldExportedSettings =
        OldExportedSettings(
            settingsStorage = decode(
                LegacyStorageKeys.SETTINGS,
                OldSettings.serializer(),
                OldSettings(state = OldSettingsState(selectedLocale = "en"), version = 1),
            ),
            calcSettingsStorage = decode(
                LegacyStorageKeys.CALC_SETTINGS,
                OldCalculationSettings.serializer(),
                OldCalculationSettings(
                    state = OldCalculationSettingsState(
                        midnightMethod = MidnightMethod.SunsetToFajr,
                        fajrAdjustment = 0,
                        sunriseAdjustment = 0,
                        dhuhrAdjustment = 0,
                        asrAdjustment = 0,
                        sunsetAdjustment = 0,
                        maghribAdjustment = 0,
                        ishaAdjustment = 0,
                        midnightAdjustment = 0,
                        hijriDateAdjustment = 0,
                    ),
                    version = 1,
                ),
            ),
            alarmSettingsStorage = decode(
                LegacyStorageKeys.ALARM_SETTINGS,
                OldAlarmSettings.serializer(),
                OldAlarmSettings(state = OldAlarmSettingsState(), version = 1),
            ),
            counterStoreStorage = decode(
                LegacyStorageKeys.COUNTER,
                OldCounterStore.serializer(),
                OldCounterStore(state = OldCounterStoreState(), version = 1),
            ),
            reminderSettingsStorage = decode(
                LegacyStorageKeys.REMINDER,
                OldReminderStore.serializer(),
                OldReminderStore(state = OldReminderStoreState(), version = 1),
            ),
            favoriteLocationsStorage = decode(
                LegacyStorageKeys.FAVORITE_LOCATIONS,
                OldFavoriteLocationsStore.serializer(),
                OldFavoriteLocationsStore(state = OldFavoriteLocationsStoreState(), version = 1),
            ),
        )

    private fun <T> decode(
        key: String,
        serializer: KSerializer<T>,
        default: T,
    ): T = mmkv.decodeString(key)?.let { storageJson.decodeFromString(serializer, it) } ?: default
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MigrationEntryPoint {
    fun repositoryMigrationRunner(): RepositoryMigrationRunner
}
