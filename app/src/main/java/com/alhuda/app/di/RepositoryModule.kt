package com.alhuda.app.di

import android.content.Context
import com.alhuda.app.MainActivity
import com.alhuda.app.adhan.AdhanScheduler
import com.alhuda.app.core.data.audio.AudioPreviewPlayerImpl
import com.alhuda.app.core.data.format.WidgetFormatterImpl
import com.alhuda.app.core.data.locale.LocalizedResources
import com.alhuda.app.core.data.locale.PerAppLocaleMarker
import com.alhuda.app.core.data.repository.AlarmRepositoryImpl
import com.alhuda.app.core.data.repository.AlarmSettingsRepositoryImpl
import com.alhuda.app.core.data.repository.AppLocaleManagerImpl
import com.alhuda.app.core.data.repository.BackupRepositoryImpl
import com.alhuda.app.core.data.repository.CalculationSettingsRepositoryImpl
import com.alhuda.app.core.data.repository.CounterRepositoryImpl
import com.alhuda.app.core.data.repository.CustomWidgetConfigRepositoryImpl
import com.alhuda.app.core.data.repository.FavoriteLocationsRepositoryImpl
import com.alhuda.app.core.data.repository.GeoInfoRepositoryImpl
import com.alhuda.app.core.data.repository.NotificationChannelManagerImpl
import com.alhuda.app.core.data.repository.NotificationRepositoryImpl
import com.alhuda.app.core.data.repository.ReminderRepositoryImpl
import com.alhuda.app.core.data.repository.RestoreApplier
import com.alhuda.app.core.data.repository.RingtoneRepositoryImpl
import com.alhuda.app.core.data.repository.SettingsRepositoryImpl
import com.alhuda.app.core.data.repository.SystemChangeRepositoryImpl
import com.alhuda.app.core.data.sensor.CompassRepositoryImpl
import com.alhuda.app.core.domain.audio.AudioPreviewPlayer
import com.alhuda.app.core.domain.model.alarm.AlarmSettings
import com.alhuda.app.core.domain.model.alarm.ScheduledAlarm
import com.alhuda.app.core.domain.model.calculation.CalculationSettings
import com.alhuda.app.core.domain.model.counter.Counter
import com.alhuda.app.core.domain.model.favorite_location.FavoriteLocation
import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.model.settings.Settings
import com.alhuda.app.core.domain.model.widget.CustomWidgetConfig
import com.alhuda.app.core.domain.repository.AlarmRepository
import com.alhuda.app.core.domain.repository.AlarmSettingsRepository
import com.alhuda.app.core.domain.repository.AppLocaleManager
import com.alhuda.app.core.domain.repository.BackupRepository
import com.alhuda.app.core.domain.repository.CalculationSettingsRepository
import com.alhuda.app.core.domain.repository.CompassRepository
import com.alhuda.app.core.domain.repository.CounterRepository
import com.alhuda.app.core.domain.repository.CustomWidgetConfigRepository
import com.alhuda.app.core.domain.repository.FavoriteLocationsRepository
import com.alhuda.app.core.domain.repository.GeoInfoRepository
import com.alhuda.app.core.domain.repository.NotificationChannelManager
import com.alhuda.app.core.domain.repository.NotificationRepository
import com.alhuda.app.core.domain.repository.ReminderRepository
import com.alhuda.app.core.domain.repository.RingtoneRepository
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.domain.repository.SystemChangeRepository
import com.alhuda.app.core.domain.usecase.WidgetFormatter
import com.alhuda.app.core.util.storage.MMKVDataStore
import com.alhuda.app.reminder.ReminderScheduler
import com.alhuda.app.widget.WidgetUpdater
import com.tencent.mmkv.MMKV
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Named
import javax.inject.Singleton

private object StorageKeysV2 {
    const val SETTINGS = "SETTINGS_STORAGE_V2"
    const val CALC_SETTINGS = "CALC_SETTINGS_STORAGE_V2"
    const val ALARM_SETTINGS = "ALARM_SETTINGS_STORAGE_V2"
    const val COUNTER = "COUNTER_STORAGE_V2"
    const val REMINDER = "REMINDER_STORAGE_V2"
    const val FAVORITE_LOCATIONS = "FAVORITE_LOCATIONS_STORAGE_V2"
    const val SCHEDULED_ALARMS = "SCHEDULED_ALARMS_V2"
    const val CUSTOM_WIDGET = "CUSTOM_WIDGET_STORAGE_V2"
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAppLocaleManager(perAppLocaleMarker: PerAppLocaleMarker): AppLocaleManager = AppLocaleManagerImpl(perAppLocaleMarker)

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        mmkv: MMKV,
        @Named("storage") storageJson: Json,
    ): MMKVDataStore<Settings> =
        MMKVDataStore(
            mmkv = mmkv,
            key = StorageKeysV2.SETTINGS,
            serializer = Settings.serializer(),
            defaultValue = Settings(selectedLocale = "en"),
            json = storageJson,
        )

    @Provides
    @Singleton
    fun provideSettingsRepository(settingsDatastore: MMKVDataStore<Settings>): SettingsRepository =
        SettingsRepositoryImpl(settingsDatastore = settingsDatastore)

    @Provides
    @Singleton
    fun provideCalculationSettingsRepository(
        mmkv: MMKV,
        @Named("storage") storageJson: Json,
    ): CalculationSettingsRepository =
        CalculationSettingsRepositoryImpl(
            calcSettingsDatastore =
                MMKVDataStore(
                    mmkv = mmkv,
                    key = StorageKeysV2.CALC_SETTINGS,
                    serializer = CalculationSettings.serializer(),
                    defaultValue = CalculationSettings(),
                    json = storageJson,
                ),
        )

    @Provides
    @Singleton
    fun provideAlarmSettingsRepository(
        mmkv: MMKV,
        @Named("storage") storageJson: Json,
    ): AlarmSettingsRepository =
        AlarmSettingsRepositoryImpl(
            alarmSettingsDatastore =
                MMKVDataStore(
                    mmkv = mmkv,
                    key = StorageKeysV2.ALARM_SETTINGS,
                    serializer = AlarmSettings.serializer(),
                    defaultValue = AlarmSettings(),
                    json = storageJson,
                ),
        )

    @Provides
    @Singleton
    fun provideCounterRepository(
        mmkv: MMKV,
        @Named("storage") storageJson: Json,
    ): CounterRepository =
        CounterRepositoryImpl(
            counterStoreDatastore =
                MMKVDataStore(
                    mmkv = mmkv,
                    key = StorageKeysV2.COUNTER,
                    serializer = ListSerializer(Counter.serializer()),
                    defaultValue = emptyList(),
                    json = storageJson,
                ),
        )

    @Provides
    @Singleton
    fun provideReminderRepository(
        mmkv: MMKV,
        @Named("storage") storageJson: Json,
    ): ReminderRepository =
        ReminderRepositoryImpl(
            reminderStoreDatastore =
                MMKVDataStore(
                    mmkv = mmkv,
                    key = StorageKeysV2.REMINDER,
                    serializer = ListSerializer(Reminder.serializer()),
                    defaultValue = emptyList(),
                    json = storageJson,
                ),
        )

    @Provides
    @Singleton
    fun provideFavoriteLocationsRepository(
        mmkv: MMKV,
        @Named("storage") storageJson: Json,
    ): FavoriteLocationsRepository =
        FavoriteLocationsRepositoryImpl(
            favoriteLocationsDatastore =
                MMKVDataStore(
                    mmkv = mmkv,
                    key = StorageKeysV2.FAVORITE_LOCATIONS,
                    serializer = ListSerializer(FavoriteLocation.serializer()),
                    defaultValue = emptyList(),
                    json = storageJson,
                ),
        )

    @Provides
    @Singleton
    fun provideCustomWidgetConfigRepository(
        mmkv: MMKV,
        @Named("storage") storageJson: Json,
    ): CustomWidgetConfigRepository =
        CustomWidgetConfigRepositoryImpl(
            customWidgetConfigDatastore =
                MMKVDataStore(
                    mmkv = mmkv,
                    key = StorageKeysV2.CUSTOM_WIDGET,
                    serializer = CustomWidgetConfig.serializer(),
                    defaultValue = CustomWidgetConfig(),
                    json = storageJson,
                ),
        )

    @Provides
    @Singleton
    fun provideBackupRepository(
        @ApplicationContext context: Context,
        @Named("storage") storageJson: Json,
        mmkv: MMKV,
        settingsRepository: SettingsRepository,
        calculationSettingsRepository: CalculationSettingsRepository,
        alarmSettingsRepository: AlarmSettingsRepository,
        counterRepository: CounterRepository,
        reminderRepository: ReminderRepository,
        favoriteLocationsRepository: FavoriteLocationsRepository,
        customWidgetConfigRepository: CustomWidgetConfigRepository,
        restoreApplier: RestoreApplier,
        diyanetChangeNoticePoster: DiyanetChangeNoticePoster,
        adhanScheduler: AdhanScheduler,
        reminderScheduler: ReminderScheduler,
        widgetUpdater: WidgetUpdater,
    ): BackupRepository =
        BackupRepositoryImpl(
            context = context,
            json = storageJson,
            mmkv = mmkv,
            settingsRepository = settingsRepository,
            calculationSettingsRepository = calculationSettingsRepository,
            alarmSettingsRepository = alarmSettingsRepository,
            counterRepository = counterRepository,
            reminderRepository = reminderRepository,
            favoriteLocationsRepository = favoriteLocationsRepository,
            customWidgetConfigRepository = customWidgetConfigRepository,
            restoreApplier = restoreApplier,
            diyanetChangeNoticePoster = diyanetChangeNoticePoster,
            adhanScheduler = adhanScheduler,
            reminderScheduler = reminderScheduler,
            widgetUpdater = widgetUpdater,
        )

    @Provides
    @Singleton
    fun provideAlarmRepository(
        @ApplicationContext context: Context,
        mmkv: MMKV,
        @Named("storage") storageJson: Json,
    ): AlarmRepository =
        AlarmRepositoryImpl(
            context = context,
            store =
                MMKVDataStore(
                    mmkv = mmkv,
                    key = StorageKeysV2.SCHEDULED_ALARMS,
                    serializer = ListSerializer(ScheduledAlarm.serializer()),
                    defaultValue = emptyList(),
                    json = storageJson,
                ),
        )

    @Provides
    @Singleton
    fun provideWidgetFormatter(): WidgetFormatter = WidgetFormatterImpl()

    @Provides
    fun provideAudioPreviewPlayer(
        @ApplicationContext context: Context,
        localizedResources: LocalizedResources,
    ): AudioPreviewPlayer = AudioPreviewPlayerImpl(context, localizedResources)

    @Provides
    @Singleton
    fun provideRingtoneRepository(@ApplicationContext context: Context): RingtoneRepository = RingtoneRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideCompassRepository(@ApplicationContext context: Context): CompassRepository = CompassRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideGeoInfoRepository(@ApplicationContext context: Context): GeoInfoRepository = GeoInfoRepositoryImpl(context = context)

    @Provides
    @Singleton
    fun provideSystemChangeRepository(): SystemChangeRepository = SystemChangeRepositoryImpl()

    @Provides
    @Singleton
    fun provideNotificationChannelManager(
        @ApplicationContext context: Context,
        localizedResources: LocalizedResources,
    ): NotificationChannelManager = NotificationChannelManagerImpl(context, localizedResources)

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        localizedResources: LocalizedResources,
    ): NotificationRepository =
        NotificationRepositoryImpl(
            context,
            MainActivity::class.java,
            localizedResources,
        )
}
