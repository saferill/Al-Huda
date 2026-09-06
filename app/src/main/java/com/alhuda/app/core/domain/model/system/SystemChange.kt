package com.alhuda.app.core.domain.model.system

import androidx.compose.runtime.Immutable

sealed interface SystemChange {

    @Immutable
    object Nothing : SystemChange

    @Immutable
    object TimeChanged : SystemChange

    @Immutable
    data class TimeZoneChanged(
        val newZoneId: String,
    ) : SystemChange
}
