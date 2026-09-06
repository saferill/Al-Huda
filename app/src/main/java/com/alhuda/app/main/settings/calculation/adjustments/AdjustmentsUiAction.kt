package com.alhuda.app.main.settings.calculation.adjustments

import com.alhuda.app.core.domain.model.adhan.Prayer

sealed interface AdjustmentsUiAction {
    data class OnPrayerChange(val prayer: Prayer, val delta: Int) : AdjustmentsUiAction
    data class OnPrayerSet(val prayer: Prayer, val value: Int) : AdjustmentsUiAction
    data class OnLunarDayChange(val delta: Int) : AdjustmentsUiAction
    data class OnLunarDaySet(val value: Int) : AdjustmentsUiAction
}
