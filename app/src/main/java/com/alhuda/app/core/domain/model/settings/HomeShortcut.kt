package com.alhuda.app.core.domain.model.settings

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alhuda.app.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class HomeShortcut(
    @param:StringRes val labelRes: Int,
) {
    @SerialName("qibla")
    Qibla(R.string.qibla),

    @SerialName("counter")
    Counter(R.string.counter),

    @SerialName("reminders")
    Reminders(R.string.reminders_title),

    @SerialName("monthly_view")
    MonthlyView(R.string.monthly_view_title),

    @SerialName("upcoming_alarms")
    UpcomingAlarms(R.string.upcoming_alarms),
}

@Composable
fun HomeShortcut.i18n() = stringResource(labelRes)

val HOME_SHORTCUTS_IN_ORDER: List<HomeShortcut> = HomeShortcut.entries.toList()

const val MAX_HOME_SHORTCUTS = 2

const val MAX_HOME_SHORTCUTS_TOOLBAR_HIDDEN = 4

fun maxHomeShortcuts(toolbarCalendarHidden: Boolean): Int =
    if (toolbarCalendarHidden) MAX_HOME_SHORTCUTS_TOOLBAR_HIDDEN else MAX_HOME_SHORTCUTS
