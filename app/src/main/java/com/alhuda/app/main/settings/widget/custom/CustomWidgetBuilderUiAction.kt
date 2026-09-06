package com.alhuda.app.main.settings.widget.custom

import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.widget.HeaderBlock

sealed interface CustomWidgetBuilderUiAction {
    data class OnBgColorChange(
        val color: Int?,
    ) : CustomWidgetBuilderUiAction

    data class OnTextColorChange(
        val color: Int?,
    ) : CustomWidgetBuilderUiAction

    data class OnHighlightColorChange(
        val color: Int?,
    ) : CustomWidgetBuilderUiAction

    data class OnRowsChange(
        val rows: List<List<Prayer>>,
    ) : CustomWidgetBuilderUiAction

    data class OnRowCountChange(
        val count: Int,
    ) : CustomWidgetBuilderUiAction

    data class OnCountdownToggle(
        val enabled: Boolean,
    ) : CustomWidgetBuilderUiAction

    data class OnCountdownColorChange(
        val color: Int?,
    ) : CustomWidgetBuilderUiAction

    data class OnHeaderFontScaleChange(
        val scale: Float,
    ) : CustomWidgetBuilderUiAction

    data class OnPrayerFontScaleChange(
        val scale: Float,
    ) : CustomWidgetBuilderUiAction

    data class OnCountdownFontScaleChange(
        val scale: Float,
    ) : CustomWidgetBuilderUiAction

    data class OnLocationToggle(
        val id: String,
        val enabled: Boolean,
    ) : CustomWidgetBuilderUiAction

    data class OnTopStartChange(
        val block: HeaderBlock?,
    ) : CustomWidgetBuilderUiAction

    data class OnTopEndChange(
        val block: HeaderBlock?,
    ) : CustomWidgetBuilderUiAction

    data class OnHeaderSlotsChange(
        val topStart: HeaderBlock?,
        val topEnd: HeaderBlock?,
    ) : CustomWidgetBuilderUiAction
}
