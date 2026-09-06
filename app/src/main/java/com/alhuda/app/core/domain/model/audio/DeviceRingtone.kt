package com.alhuda.app.core.domain.model.audio

data class DeviceRingtone(
    val id: String,
    val label: String,

    val uri: String,

    val loop: Boolean,
)
