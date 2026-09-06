package com.alhuda.app.main.reminder

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.alarm.PrayerAlarmSettings
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.model.reminder.ReminderAudioEntry
import com.alhuda.app.core.domain.model.settings.AudioEntry
import com.alhuda.app.core.domain.model.settings.Settings
import kotlinx.datetime.DayOfWeek

enum class ReminderTimeModifier { Before, After }

@Immutable
data class ReminderEditDraft(
    val id: String? = null,
    val label: String = "",

    val duration: Int = 5,
    val modifier: ReminderTimeModifier = ReminderTimeModifier.Before,
    val prayer: Prayer = Prayer.Fajr,
    val sound: ReminderAudioEntry? = null,
    val vibration: VibrationMode? = null,
    val only: Boolean = true,

    val days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
)

@Immutable
data class ReminderUiState(
    val reminders: List<Reminder> = emptyList(),

    val userSounds: List<ReminderAudioEntry> = emptyList(),

    val deviceSounds: List<ReminderAudioEntry> = emptyList(),

    val adhanEntries: List<AudioEntry> = emptyList(),

    val playingSoundId: String? = null,
    val selectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
    val contextMenuId: String? = null,
    val editDraft: ReminderEditDraft? = null,
    val deletingReminderId: String? = null,
    val deletingBulk: Boolean = false,

    val settings: Settings = Settings(selectedLocale = "en"),
) {
    fun allSelected(): Boolean = reminders.isNotEmpty() && selectedIds.size == reminders.size
}

fun Reminder.toDraft(): ReminderEditDraft =
    ReminderEditDraft(
        id = id,
        label = label,
        duration = duration,
        modifier = if (durationModifier >= 0) ReminderTimeModifier.After else ReminderTimeModifier.Before,
        prayer = prayer,
        sound = sound,
        vibration = vibration,
        only = once ?: true,

        days = (days as? PrayerAlarmSettings.ByWeekDay)?.days?.filterValues { it }?.keys
            ?.ifEmpty { null }
            ?: DayOfWeek.entries.toSet(),
    )
