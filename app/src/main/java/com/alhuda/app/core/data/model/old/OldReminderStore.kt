package com.alhuda.app.core.data.model.old

import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.reminder.Reminder
import com.alhuda.app.core.domain.model.reminder.ReminderAudioEntry
import com.alhuda.app.core.domain.model.settings.mapAdhanIdToEntryOrNull
import com.alhuda.app.core.util.serialization.EmptyStringAsNullSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class OldReminderStore(
    val state: OldReminderStoreState,
    val version: Int,
)

@Serializable
data class OldReminderStoreState(
    @SerialName("REMINDERS") val reminders: List<OldReminder> = emptyList(),
)

@Serializable
data class OldReminder(
    val id: String,
    @Serializable(with = EmptyStringAsNullSerializer::class) val label: String? = null,
    val enabled: Boolean = false,
    val prayer: Prayer,

    val duration: Long,

    val durationModifier: Int,

    val sound: OldAudioEntry? = null,

    val once: Boolean? = null,
    val days: OldPrayerAlarmSettings? = null,
)

@Serializable(with = OldAudioEntrySerializer::class)
sealed interface OldAudioEntry {
    @Serializable
    data class OldResourceOldAudioEntry(
        val id: String,
        val filepath: Int,
        val label: String,
        val canDelete: Boolean = false,
        val loop: Boolean = false,
        val notif: Boolean = false,
    ) : OldAudioEntry

    @Serializable
    data class OldExternalAudioEntry(
        val id: String,
        val filepath: String,
        val label: String,
        val canDelete: Boolean = false,
        val loop: Boolean = false,
        val notif: Boolean = false,
    ) : OldAudioEntry

    @Serializable
    data class OldDefaultAudioEntry(
        val label: String,
    ) : OldAudioEntry {
        val id = "default"
        val filepath: String? = null
        val canDelete = false
        val loop = false
        val notif = true
    }
}

internal object OldAudioEntrySerializer : KSerializer<OldAudioEntry> {
    override val descriptor: SerialDescriptor =
        OldAudioEntry.OldResourceOldAudioEntry.serializer().descriptor

    override fun deserialize(decoder: Decoder): OldAudioEntry {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("OldAudioEntrySerializer can be used only with JSON")

        val element: JsonElement = jsonDecoder.decodeJsonElement()

        val filepath = element.jsonObject["filepath"]
        val serializer =
            when {

                filepath == null || filepath is JsonNull -> OldAudioEntry.OldDefaultAudioEntry.serializer()

                filepath.jsonPrimitive.isString -> OldAudioEntry.OldExternalAudioEntry.serializer()

                else -> OldAudioEntry.OldResourceOldAudioEntry.serializer()
            }

        try {
            return jsonDecoder.json.decodeFromJsonElement(serializer, element)
        } catch (e: Exception) {
            throw SerializationException("Cannot deserialize OldAudioEntry: ${e.message}")
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: OldAudioEntry,
    ) {
        when (value) {
            is OldAudioEntry.OldResourceOldAudioEntry -> {
                encoder.encodeSerializableValue(
                    OldAudioEntry.OldResourceOldAudioEntry.serializer(),
                    value,
                )
            }

            is OldAudioEntry.OldExternalAudioEntry -> {
                encoder.encodeSerializableValue(
                    OldAudioEntry.OldExternalAudioEntry.serializer(),
                    value,
                )
            }

            is OldAudioEntry.OldDefaultAudioEntry -> {
                encoder.encodeSerializableValue(
                    OldAudioEntry.OldDefaultAudioEntry.serializer(),
                    value,
                )
            }
        }
    }
}

fun OldReminder.toReminder() =
    Reminder(
        id = this.id,
        label = this.label ?: "",
        enabled = this.enabled,
        prayer = this.prayer,

        duration = (this.duration / 60_000L).toInt(),
        durationModifier = this.durationModifier,
        sound = this.sound?.toReminderAudioEntry(),
        once = this.once,
        days = this.days?.toPrayerAlarmSettings(),
    )

fun OldAudioEntry.toReminderAudioEntry(): ReminderAudioEntry {

    val id: String
    val label: String
    val canDelete: Boolean
    val loop: Boolean
    val externalPath: String?
    when (this) {
        is OldAudioEntry.OldResourceOldAudioEntry -> {
            id = this.id
            label = this.label
            canDelete = this.canDelete
            loop = this.loop
            externalPath = null
        }

        is OldAudioEntry.OldExternalAudioEntry -> {
            id = this.id
            label = this.label
            canDelete = this.canDelete
            loop = this.loop
            externalPath = this.filepath
        }

        is OldAudioEntry.OldDefaultAudioEntry -> {
            id = this.id
            label = this.label
            canDelete = this.canDelete
            loop = this.loop
            externalPath = null
        }
    }

    mapAdhanIdToEntryOrNull(id)?.resId?.let { resourceId ->
        return ReminderAudioEntry.ResourceReminderAudioEntry(
            id = id,
            resourceId = resourceId,
            label = label,
            canDelete = canDelete,
            loop = loop,
        )
    }

    return externalPath?.let { path ->
        ReminderAudioEntry.ExternalReminderAudioEntry(
            id = id,
            filepath = path,
            label = label,
            canDelete = canDelete,
            loop = loop,
        )
    } ?: ReminderAudioEntry.DefaultReminderAudioEntry
}
