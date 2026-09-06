package com.alhuda.app.core.data.repository

import android.content.Context
import android.net.Uri
import com.alhuda.app.adhan.AdhanScheduler
import com.alhuda.app.core.data.model.ExportedSettingsV2
import com.alhuda.app.core.data.model.RestoreData
import com.alhuda.app.core.data.model.old.LegacyStorageKeys
import com.alhuda.app.core.data.model.old.OldExportedSettings
import com.alhuda.app.core.data.model.old.toRestoreData
import com.alhuda.app.core.data.model.toRestoreData
import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.model.reminder.ReminderAudioEntry
import com.alhuda.app.core.domain.model.settings.AudioEntry
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.model.settings.getDefaultAdhanEntries
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.BackupRepository
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.CounterRepository
import com.alhuda.app.core.domain.repository.CustomWidgetConfigRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.ReminderRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.di.DiyanetChangeNoticePoster
import com.alhuda.app.reminder.ReminderScheduler
import com.alhuda.app.widget.WidgetUpdater
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class BackupRepositoryImpl(
    private val context: Context,
    private val json: Json,
    private val mmkv: MMKV,
    private val settingsRepository: SettingsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val counterRepository: CounterRepository,
    private val reminderRepository: ReminderRepository,
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val customWidgetConfigRepository: CustomWidgetConfigRepository,
    private val restoreApplier: RestoreApplier,
    private val adhanScheduler: AdhanScheduler,
    private val reminderScheduler: ReminderScheduler,
    private val widgetUpdater: WidgetUpdater,
    private val diyanetChangeNoticePoster: DiyanetChangeNoticePoster,
) : BackupRepository {

    private companion object {
        const val V2_DETECT_KEY = "SETTINGS_STORAGE_V2"
        const val LEGACY_DETECT_KEY = "SETTINGS_STORAGE"
    }

    override suspend fun exportTo(uri: Uri) {

        val exported = ExportedSettingsV2(
            settings = stripCustomSounds(settingsRepository.fetch()),
            calculationSettings = calculationSettingsRepository.fetch(),
            alarmSettings = alarmSettingsRepository.fetch(),
            counters = counterRepository.fetch(),
            reminders = stripCustomSounds(reminderRepository.fetch()),
            favoriteLocations = favoriteLocationsRepository.fetch(),
            customWidget = customWidgetConfigRepository.fetch(),
        )
        val content = json.encodeToString(ExportedSettingsV2.serializer(), exported)
        writeToUri(uri, content)
    }

    override fun hasLegacyData(): Boolean = mmkv.containsKey(LegacyStorageKeys.SETTINGS)

    override suspend fun exportLegacyTo(uri: Uri) {

        val stores = LegacyStorageKeys.ALL.mapNotNull { key ->
            mmkv.decodeString(key)?.let { raw -> key to json.parseToJsonElement(raw) }
        }
        if (stores.isEmpty()) throw IllegalStateException("No legacy app data found to export")
        val content = json.encodeToString(JsonObject.serializer(), JsonObject(stores.toMap()))
        writeToUri(uri, content)
    }

    private suspend fun writeToUri(
        uri: Uri,
        content: String,
    ) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(content.toByteArray())
            } ?: throw IllegalStateException("Could not open output stream for $uri")
        }
    }

    override suspend fun restoreFrom(uri: Uri) {
        val content = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                ?: throw IllegalStateException("Could not open input stream for $uri")
        }

        val keys = json.parseToJsonElement(content).jsonObject.keys
        val data = when {
            V2_DETECT_KEY in keys -> json.decodeFromString(ExportedSettingsV2.serializer(), content).toRestoreData()
            LEGACY_DETECT_KEY in keys -> json.decodeFromString(OldExportedSettings.serializer(), content).toRestoreData()
            else -> throw IllegalArgumentException("Unrecognized backup file format")
        }

        val sanitized = data.copy(
            settings = stripCustomSounds(data.settings),
            reminders = stripCustomSounds(data.reminders),
        )
        restoreApplier.apply(sanitized)

        adhanScheduler.schedule()
        reminderScheduler.schedule()
        widgetUpdater.update()

        diyanetChangeNoticePoster.postIfPending()
    }

    private fun stripCustomSounds(settings: Settings): Settings {
        val default = getDefaultAdhanEntries().first()
        return settings.copy(
            savedUserAudioEntries = emptyList(),
            selectedAdhanEntries = settings.selectedAdhanEntries.mapValues { (_, entry) ->
                if (entry is AudioEntry.ExternalAudioEntry) default else entry
            },
        )
    }

    private fun stripCustomSounds(reminders: List<Reminder>): List<Reminder> =
        reminders.map { reminder ->
            if (reminder.sound is ReminderAudioEntry.ExternalReminderAudioEntry) {
                reminder.copy(sound = ReminderAudioEntry.DefaultReminderAudioEntry)
            } else {
                reminder
            }
        }
}
