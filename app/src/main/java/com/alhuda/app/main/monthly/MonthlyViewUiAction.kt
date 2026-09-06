package com.alhuda.app.main.monthly

sealed interface MonthlyViewUiAction {
    object OnPrevMonthClick : MonthlyViewUiAction
    object OnNextMonthClick : MonthlyViewUiAction
    object OnShowThisMonthClick : MonthlyViewUiAction
    object OnToggleCalendarClick : MonthlyViewUiAction
}
