package com.alhuda.app.core.domain.model.reminder

import com.alhuda.app.R
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.alarm.PrayerAlarmSettings
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class Reminder(
    val id: String,
    val label: String = "",
    val enabled: Boolean = false,
    val prayer: Prayer,

    val duration: Int,
    val durationModifier: Int,
    val sound: ReminderAudioEntry? = null,

    val vibration: VibrationMode? = null,
    val once: Boolean? = null,
    val days: PrayerAlarmSettings? = null,
)

@Serializable(with = ReminderAudioEntrySerializer::class)
sealed interface ReminderAudioEntry {

    companion object {

        const val SILENT_ID = "silent"

        fun silent(label: String): ResourceReminderAudioEntry =
            ResourceReminderAudioEntry(
                id = SILENT_ID,
                resourceId = R.raw.silence,
                label = label,
                canDelete = false,
                loop = false,
            )
    }

    val loop: Boolean

    @Serializable
    data class ResourceReminderAudioEntry(
        val id: String,
        val resourceId: Int,
        val label: String,
        val canDelete: Boolean = false,
        override val loop: Boolean = false,
    ) : ReminderAudioEntry

    @Serializable
    data class ExternalReminderAudioEntry(
        val id: String,
        val filepath: String,
        val label: String,
        val canDelete: Boolean = true,
        override val loop: Boolean = false,
    ) : ReminderAudioEntry

    @Serializable
    object DefaultReminderAudioEntry : ReminderAudioEntry {
        val id = "default"
        val canDelete = false
        override val loop = false
    }
}

internal object ReminderAudioEntrySerializer : KSerializer<ReminderAudioEntry> {
    override val descriptor: SerialDescriptor =
        ReminderAudioEntry.ResourceReminderAudioEntry.serializer().descriptor

    override fun deserialize(decoder: Decoder): ReminderAudioEntry {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("ReminderAudioEntrySerializer can be used only with JSON")

        val element: JsonElement = jsonDecoder.decodeJsonElement()
        val keys = element.jsonObject.keys

        val serializer =
            when {
                "resourceId" in keys -> ReminderAudioEntry.ResourceReminderAudioEntry.serializer()
                "filepath" in keys -> ReminderAudioEntry.ExternalReminderAudioEntry.serializer()
                else -> ReminderAudioEntry.DefaultReminderAudioEntry.serializer()
            }

        try {
            return jsonDecoder.json.decodeFromJsonElement(serializer, element)
        } catch (e: Exception) {
            throw SerializationException("Cannot deserialize ReminderAudioEntry: ${e.message}")
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: ReminderAudioEntry,
    ) {
        when (value) {
            is ReminderAudioEntry.ResourceReminderAudioEntry -> {
                encoder.encodeSerializableValue(
                    ReminderAudioEntry.ResourceReminderAudioEntry.serializer(),
                    value,
                )
            }

            is ReminderAudioEntry.ExternalReminderAudioEntry -> {
                encoder.encodeSerializableValue(
                    ReminderAudioEntry.ExternalReminderAudioEntry.serializer(),
                    value,
                )
            }

            is ReminderAudioEntry.DefaultReminderAudioEntry -> {
                encoder.encodeSerializableValue(
                    ReminderAudioEntry.DefaultReminderAudioEntry.serializer(),
                    value,
                )
            }
        }
    }
}
