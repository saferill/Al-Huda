package com.alhuda.app.main.home

sealed interface HomeUiAction {
    object OnNextDayClick : HomeUiAction
    object OnPrevDayClick : HomeUiAction
    object OnShowTodayClick : HomeUiAction
    object OnLocationTextClick : HomeUiAction
    object OnCalculationLinkClick : HomeUiAction
    object OnCalendarDateClick : HomeUiAction
    object OnReminderLinkClick : HomeUiAction
    object OnQiblaLinkClick : HomeUiAction
    object OnQiblaShortcutClick : HomeUiAction
    object OnCounterLinkClick : HomeUiAction
    object OnQuranLinkClick : HomeUiAction
    object OnSettingsLinkClick : HomeUiAction
    object OnUpcomingAlarmsClick : HomeUiAction
    object OnPrayerGuideClick : HomeUiAction
    object OnAboutLinkClick : HomeUiAction
    object OnMonthlyViewClick : HomeUiAction
    object OnDeveloperLinkClick : HomeUiAction
}
