package com.alhuda.app.core.domain.model.alarm

import kotlinx.serialization.Serializable

@Serializable
enum class VibrationMode {
    Off,
    Once,
    Continuous,
}
