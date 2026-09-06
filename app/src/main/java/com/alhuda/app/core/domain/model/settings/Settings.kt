package com.alhuda.app.core.domain.model.settings

import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.adhan.AdhanKey
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.alarm.SkippedAlarm
import com.alhuda.app.core.util.serialization.EmptyStringAsNullSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject

private enum class DefaultAdhanEntryId(
    val key: String,
) {
    MasjidAnNabawi("masjid_an_nabawi"),
    AbdulBasitAbduSamad("abdul_basit_abdus_samad"),
    RaghebMustafaGhalwash("ragheb_mustafa_ghalwash"),
    MoazenZade("moazen_zade"),
}

const val SILENT_AUDIO_ID = "silent"

const val NOTIFICATION_AUDIO_ID = "notification_default"

fun mapAdhanIdToEntry(key: String): AudioEntry.ResourceAudioEntry =
    mapAdhanIdToEntryOrNull(key) ?: mapAdhanIdToEntry(DefaultAdhanEntryId.MasjidAnNabawi.key)

fun mapAdhanIdToEntryOrNull(key: String): AudioEntry.ResourceAudioEntry? =
    when (key) {
        SILENT_AUDIO_ID -> AudioEntry.ResourceAudioEntry(
            SILENT_AUDIO_ID,
            R.raw.silence,
            R.string.silent_sound,
        )

        NOTIFICATION_AUDIO_ID -> AudioEntry.ResourceAudioEntry(
            NOTIFICATION_AUDIO_ID,
            R.raw.silence,
            R.string.reminder_default_sound,
        )

        DefaultAdhanEntryId.MasjidAnNabawi.key -> AudioEntry.ResourceAudioEntry(
            DefaultAdhanEntryId.MasjidAnNabawi.key,
            R.raw.masjid_an_nabawi,
            R.string.masjid_an_nabawi,
        )

        DefaultAdhanEntryId.AbdulBasitAbduSamad.key -> AudioEntry.ResourceAudioEntry(
            DefaultAdhanEntryId.AbdulBasitAbduSamad.key,
            R.raw.abdul_basit_abdus_samad,
            R.string.abdul_basit_abdus_samad,
        )

        DefaultAdhanEntryId.RaghebMustafaGhalwash.key -> AudioEntry.ResourceAudioEntry(
            DefaultAdhanEntryId.RaghebMustafaGhalwash.key,
            R.raw.ragheb_mustafa_ghalwash,
            R.string.ragheb_mustafa_ghalwash,
        )

        DefaultAdhanEntryId.MoazenZade.key -> AudioEntry.ResourceAudioEntry(
            DefaultAdhanEntryId.MoazenZade.key,
            R.raw.moazen_zade,
            R.string.moazen_zade,
        )

        else -> null
    }

fun getDefaultAdhanEntries(): List<AudioEntry.ResourceAudioEntry> =
    listOf(
        mapAdhanIdToEntry(DefaultAdhanEntryId.MasjidAnNabawi.key),
        mapAdhanIdToEntry(DefaultAdhanEntryId.AbdulBasitAbduSamad.key),
        mapAdhanIdToEntry(DefaultAdhanEntryId.RaghebMustafaGhalwash.key),
        mapAdhanIdToEntry(DefaultAdhanEntryId.MoazenZade.key),
    )

@Serializable
data class Settings(
    val deliveredAlarmTimestamps: Map<String, Long?> = emptyMap(),
    val themeColor: ThemeColor = ThemeColor.Default,
    val is24HourFormat: Boolean = true,

    val displayScale: Float = 1f,
    val numberingSystem: NumberingSystem = NumberingSystem.Default,
    val highlightCurrentPrayer: Boolean = false,
    val selectedLocale: String,
    val selectedArabicCalendar: String = "islamic",
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val selectedLocaleForArabicCalendar: String? = null,
    val selectedSecondaryCalendar: SecondaryCalendar = SecondaryCalendar.Gregorian,

    val hideToolbarCalendar: Boolean = false,

    val swapHomeCalendars: Boolean = false,
    val appInitialConfigDone: Boolean = false,
    val appIntroDone: Boolean = false,
    val savedAdhanAudioEntries: List<AudioEntry> = getDefaultAdhanEntries(),
    val savedUserAudioEntries: List<AudioEntry.ExternalAudioEntry> = emptyList(),
    val selectedAdhanEntries: Map<AdhanKey, AudioEntry> = mapOf(
        AdhanKey.Default to getDefaultAdhanEntries()[0],
    ),
    val lastAppFocusTimestamp: Int? = null,
    val hiddenPrayers: List<Prayer> = emptyList(),
    val alarmVolume: Int? = null,

    val gradualAlarmVolume: Boolean = false,
    val volumeButtonStopsAdhan: Boolean = false,
    val preferExternalAudioDevice: Boolean = false,
    val bypassDnd: Boolean = false,
    val hiddenWidgetPrayers: List<Prayer> = listOf(Prayer.Sunset, Prayer.Midnight, Prayer.Tahajjud),
    val showWidget: Boolean = false,

    val notificationWidgetLayout: NotificationWidgetLayout = NotificationWidgetLayout.Table,
    val showWidgetCountdown: Boolean = false,
    val adaptiveWidgets: Boolean = false,
    val widgetCityNamePos: WidgetCityNamePos = WidgetCityNamePos.None,
    val swapWidgetLayoutDirection: Boolean = false,

    val highlightCurrentPrayerWidget: Boolean = false,

    val widgetHijriDayStartsAtMaghrib: Boolean = false,

    val countdownWidgetPrayers: List<Prayer> = listOf(
        Prayer.Fajr,
        Prayer.Dhuhr,
        Prayer.Asr,
        Prayer.Maghrib,
        Prayer.Isha,
    ),
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val calcSettingsHash: String? = null,
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val alarmSettingsHash: String? = null,
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val reminderSettingsHash: String? = null,
    val isPlayingAudio: Boolean = false,
    val dontAskPermissionNotifications: Boolean = false,
    val dontAskPermissionAlarm: Boolean = false,
    val dontAskPermissionPhoneState: Boolean = false,
    val dontAskPermissionFullScreenIntent: Boolean = false,
    val dontAskPermissionDndAccess: Boolean = false,
    val dontAskPermissionBatteryOptimization: Boolean = false,
    val dontAskPermissionDisplayOverApps: Boolean = false,
    val devMode: Boolean = false,
    val qiblaFinderUnderstood: Boolean = false,
    val qiblaFinderOrientationLocked: Boolean = true,
    val counterHistoryVisible: Boolean = false,
    val upcomingAlarmsHelpDismissed: Boolean = false,
    val advancedCustomAdhan: Boolean = false,
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val ramadanRemindedYear: String? = null,
    val ramadanReminderDontShow: Boolean = false,
    val useDifferentAlarmType: Boolean = false,
    val hijriMonthlyView: Boolean = false,
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val locationIdBeforeTravel: String? = null,
    val travelModeLastUpdateMillis: Long? = null,
    val showHomeNextPrayerCountdown: Boolean = true,
    val countdownSkipNonPrayers: Boolean = false,

    val homeShortcuts: List<HomeShortcut> = emptyList(),

    val skippedOccurrences: List<SkippedAlarm> = emptyList(),

    val silencedUntilMillis: Long? = null,

    val dndRestoreFilter: Int? = null,

    val forceLaunchAlarmActivity: Boolean = false,

    val diyanetFixApplied: Boolean = false,

    val diyanetChangeNoticePending: Boolean = false,
)

@Serializable
enum class NumberingSystem(
    val value: String,
) {
    @SerialName("")
    Default(""),

    @SerialName("latn")
    Latn("latn"),

    @SerialName("arab")
    Arab("arab"),

    @SerialName("arabext")
    Arabext("arabext"),
}

@Serializable
enum class SecondaryCalendar(
    val value: String,
) {
    @SerialName("gregorian")
    Gregorian("gregorian"),

    @SerialName("persian")
    Persian("persian"),

    @SerialName("ethiopic")
    Ethiopic("ethiopic"),

    @SerialName("buddhist")
    Buddhist("buddhist"),
}

@Serializable
enum class WidgetCityNamePos {
    @SerialName("")
    None,

    @SerialName("top_start")
    TopStart,

    @SerialName("top_end")
    TopEnd,
}

fun WidgetCityNamePos.i18n(resources: Resources): String =
    when (this) {
        WidgetCityNamePos.None -> resources.getString(R.string.none)
        WidgetCityNamePos.TopStart -> resources.getString(R.string.lunar_calendar)
        WidgetCityNamePos.TopEnd -> resources.getString(R.string.secondary_calendar)
    }

@Serializable
enum class NotificationWidgetLayout {
    @SerialName("table")
    Table,

    @SerialName("compact")
    Compact,

    @SerialName("custom")
    Custom,
}

fun NotificationWidgetLayout.i18n(resources: Resources): String =
    when (this) {
        NotificationWidgetLayout.Table -> resources.getString(R.string.notification_widget_layout_table)
        NotificationWidgetLayout.Compact -> resources.getString(R.string.notification_widget_layout_compact)
        NotificationWidgetLayout.Custom -> resources.getString(R.string.widget_section_custom_appearance)
    }

@Serializable
enum class ThemeColor {
    @SerialName("default")
    Default,

    @SerialName("light")
    Light,

    @SerialName("dark")
    Dark,

    @SerialName("classic_light")
    ClassicLight,

    @SerialName("classic_dark")
    ClassicDark,

    @SerialName("dynamic")
    Dynamic,

    ;

    fun isClassic(): Boolean =
        when (this) {
            ClassicLight, ClassicDark -> true
            else -> false
        }

    @Composable
    fun isDark(): Boolean =
        when (this) {
            ClassicDark, Dark -> true
            Dynamic -> isSystemInDarkTheme()
            else -> false
        }
}

@Serializable(with = AudioEntrySerializer::class)
sealed interface AudioEntry {
    val id: String
    val loop: Boolean

    @Serializable
    data class ResourceAudioEntry(
        override val id: String,
        @param:RawRes val resId: Int? = null,
        @param:StringRes val labelResId: Int = R.string.unknown,
    ) : AudioEntry {
        override val loop: Boolean = false
        val canDelete: Boolean = false
    }

    @Serializable
    data class ExternalAudioEntry(
        override val id: String,
        @Serializable(with = EmptyStringAsNullSerializer::class) val filepath: String? = null,
        val label: String = "",
        override val loop: Boolean = false,
    ) : AudioEntry {
        val canDelete: Boolean = true
    }

    @Composable
    fun getLabel(): String =
        when (this) {
            is ResourceAudioEntry -> stringResource(this.labelResId)
            is ExternalAudioEntry -> this.label
        }
}

fun AudioEntry.isResolvable(): Boolean =
    when (this) {
        is AudioEntry.ResourceAudioEntry -> resId != null
        is AudioEntry.ExternalAudioEntry -> !filepath.isNullOrEmpty()
    }

object AudioEntrySerializer :
    JsonContentPolymorphicSerializer<AudioEntry>(AudioEntry::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AudioEntry> {

        val hasResId = element.jsonObject["resId"]?.takeIf { it != JsonNull } != null
        return if (hasResId) {
            ReResolvingResourceAudioEntrySerializer
        } else {
            AudioEntry.ExternalAudioEntry.serializer()
        }
    }
}

private object ReResolvingResourceAudioEntrySerializer : KSerializer<AudioEntry.ResourceAudioEntry> {
    private val delegate = AudioEntry.ResourceAudioEntry.serializer()
    override val descriptor = delegate.descriptor

    override fun serialize(
        encoder: Encoder,
        value: AudioEntry.ResourceAudioEntry,
    ) = delegate.serialize(encoder, value)

    override fun deserialize(decoder: Decoder): AudioEntry.ResourceAudioEntry {
        val decoded = delegate.deserialize(decoder)
        return mapAdhanIdToEntryOrNull(decoded.id)
            ?: decoded.copy(resId = null, labelResId = R.string.unknown)
    }
}
