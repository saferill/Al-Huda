package com.alhuda.app.main.settings.widget.custom

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.widget.CustomWidgetConfig
import com.alhuda.app.core.domain.model.widget.CustomWidgetData

@Immutable
data class LocationToggle(
    val id: String,
    val name: String,
    val enabled: Boolean,

    val isTravelMode: Boolean = false,
)

@Immutable
data class CustomWidgetBuilderUiState(
    val config: CustomWidgetConfig = CustomWidgetConfig(),
    val locations: List<LocationToggle> = emptyList(),

    val prayerTimes: Map<Prayer, String> = emptyMap(),

    val previewData: CustomWidgetData? = null,
)
