package com.alhuda.app.core.domain.repository

import com.alhuda.app.core.domain.model.audio.DeviceRingtone

interface RingtoneRepository {

    suspend fun getDeviceRingtones(): List<DeviceRingtone>
}
