package com.alhuda.app.core.data.repository

import android.content.Context
import android.media.RingtoneManager
import com.alhuda.app.core.domain.model.audio.DeviceRingtone
import com.alhuda.app.core.domain.repository.RingtoneRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RingtoneRepositoryImpl(
    private val context: Context,
) : RingtoneRepository {
    private data class BaseSound(
        val id: String,
        val label: String,
        val uri: String,
    )

    override suspend fun getDeviceRingtones(): List<DeviceRingtone> =
        withContext(Dispatchers.IO) {
            val seen = HashSet<String>()
            val base = buildList {
                collectType(RingtoneManager.TYPE_NOTIFICATION, seen, this)
                collectType(RingtoneManager.TYPE_ALARM, seen, this)
                collectType(RingtoneManager.TYPE_RINGTONE, seen, this)
            }
            base.flatMap { sound ->
                listOf(
                    DeviceRingtone(id = sound.id, label = sound.label, uri = sound.uri, loop = false),
                    DeviceRingtone(id = "${sound.id}_loop", label = sound.label, uri = sound.uri, loop = true),
                )
            }
        }

    private fun collectType(
        type: Int,
        seen: MutableSet<String>,
        out: MutableList<BaseSound>,
    ) {
        try {
            val manager = RingtoneManager(context).apply { setType(type) }
            val cursor = manager.cursor
            while (cursor.moveToNext()) {
                val id = cursor.getLong(RingtoneManager.ID_COLUMN_INDEX).toString()
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX) ?: continue
                val uri = manager.getRingtoneUri(cursor.position)?.toString() ?: continue
                if (!seen.add(uri)) continue
                out.add(BaseSound(id = "ringtone_$id", label = title, uri = uri))
            }
        } catch (_: Exception) {

        }
    }
}
