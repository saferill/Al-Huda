package com.alhuda.app.main.monthly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.settings.ThemeColor
import com.alhuda.app.core.presentation.AlHudaThemePreview
import com.alhuda.app.core.presentation.ClassicHighlightBackground
import com.alhuda.app.core.presentation.DarkOnTertiaryContainer
import com.alhuda.app.core.presentation.DarkTertiary
import com.alhuda.app.core.presentation.DarkTertiaryContainer
import com.alhuda.app.core.presentation.LightOnTertiaryContainer
import com.alhuda.app.core.presentation.LightTertiaryContainer
import com.alhuda.app.core.presentation.components.ScreenScaffold
import com.alhuda.app.core.presentation.navigation.NavigationController
import com.alhuda.app.core.presentation.util.drawHorizontalScrollbar
import com.alhuda.app.core.presentation.util.drawVerticalScrollbar
import com.alhuda.app.core.presentation.util.dropShadow2

private val COLUMN_TEXT_CUSHION = 6.dp

private val ROW_END_INSET = 4.dp

@Composable
fun MonthlyViewScreen(
    uiState: MonthlyViewUiState,
    onAction: (MonthlyViewUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        title = stringResource(R.string.monthly_view_title),
        onBackClick = { NavigationController.navigateBack() },
        modifier = modifier,
        floatingActionButton = {
            AnimatedVisibility(
                modifier = Modifier.graphicsLayer { clip = false },
                visible = !uiState.isCurrentMonth,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 150),
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it * 3 },
                    animationSpec = tween(durationMillis = 200),
                ),
            ) {
                val buttonShape = MaterialTheme.shapes.extraLarge

                val dark = uiState.themeColor.isDark()
                val containerColor = if (uiState.themeColor.isClassic()) {
                    if (dark) DarkTertiaryContainer else LightTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                }
                val contentColor = if (uiState.themeColor.isClassic()) {
                    if (dark) DarkOnTertiaryContainer else LightOnTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                }
                ExtendedFloatingActionButton(
                    onClick = { onAction(MonthlyViewUiAction.OnShowThisMonthClick) },
                    shape = buttonShape,
                    modifier = Modifier
                        .widthIn(min = 160.dp)
                        .dropShadow2(buttonShape),
                    containerColor = containerColor,
                    contentColor = contentColor,
                ) {
                    Text(
                        stringResource(R.string.show_this_month),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,

        scrollable = false,
        contentPadding = PaddingValues(),
        verticalArrangement = Arrangement.Top,
    ) {
        Column(
            Modifier.padding(dimensionResource(R.dimen.page_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding)),
        ) {

            FlowRow(
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
            ) {
                OutlinedButton(onClick = { onAction(MonthlyViewUiAction.OnPrevMonthClick) }) {
                    Icon(painterResource(R.drawable.arrow_back), null)
                    Text(stringResource(R.string.prev_month), modifier = Modifier.padding(start = 6.dp))
                }
                OutlinedButton(onClick = { onAction(MonthlyViewUiAction.OnNextMonthClick) }) {
                    Text(stringResource(R.string.next_month), modifier = Modifier.padding(end = 6.dp))
                    Icon(painterResource(R.drawable.arrow_forward), null)
                }
            }
            MonthLabelButton(
                label = uiState.monthLabel,
                calendarMode = uiState.calendarMode,
                onClick = { onAction(MonthlyViewUiAction.OnToggleCalendarClick) },
            )
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
            ) {
                MonthlyTable(
                    rows = uiState.rows,
                    themeColor = uiState.themeColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MonthlyTable(
    rows: List<MonthlyDayRow>,
    themeColor: ThemeColor,
    modifier: Modifier = Modifier,
) {
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()
    val cellWidth = rememberColumnFloor(rows)

    BoxWithConstraints(modifier.drawHorizontalScrollbar(hScroll)) {

        val rowWidth = maxWidth
        Column(Modifier.fillMaxSize()) {

            HeaderRow(cellWidth, rowWidth, ROW_END_INSET, hScroll)
            HorizontalDivider()
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .drawVerticalScrollbar(vScroll)
                    .verticalScroll(vScroll),
            ) {
                rows.forEach { row ->
                    DayRow(row, themeColor, cellWidth, rowWidth, ROW_END_INSET, hScroll)

                    if (row.isToday && themeColor != ThemeColor.ClassicLight) {
                        HorizontalDivider(color = todayAccentColor(themeColor))
                    } else {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberColumnFloor(rows: List<MonthlyDayRow>): Dp {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val headerStyle = MaterialTheme.typography.labelMedium
    val bodyStyle = MaterialTheme.typography.bodySmall
    val headers = listOf(
        R.string.date_column,
        R.string.fajr,
        R.string.dhuhr,
        R.string.asr,
        R.string.maghrib,
        R.string.isha,
    ).map { stringResource(it) }
    return remember(rows, headers, headerStyle, bodyStyle, density) {
        var widestPx = 0
        headers.forEach { widestPx = maxOf(widestPx, measurer.measure(it, headerStyle).size.width) }
        rows.forEach { row ->
            listOf(row.day, row.fajr, row.dhuhr, row.asr, row.maghrib, row.isha).forEach {
                widestPx = maxOf(widestPx, measurer.measure(it, bodyStyle).size.width)
            }
        }
        with(density) { widestPx.toDp() } + COLUMN_TEXT_CUSHION
    }
}

@Composable
private fun MonthLabelButton(
    label: String,
    calendarMode: MonthlyCalendarMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = when (calendarMode) {
        MonthlyCalendarMode.SECONDARY -> MaterialTheme.colorScheme.tertiary
        MonthlyCalendarMode.LUNAR -> MaterialTheme.colorScheme.secondary
    }
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, contentColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
    ) {
        Text(label, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HeaderRow(
    cellWidth: Dp,
    rowWidth: Dp,
    endInset: Dp,
    scrollState: ScrollState,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp)

            .widthIn(min = rowWidth)
            .padding(end = endInset)
            .semantics(mergeDescendants = true) { heading() },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        listOf(R.string.date_column, R.string.fajr, R.string.dhuhr, R.string.asr, R.string.maghrib, R.string.isha).forEach {
            Text(
                stringResource(it),
                modifier = Modifier.width(cellWidth),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun todayAccentColor(themeColor: ThemeColor): Color =
    when {
        themeColor == ThemeColor.ClassicLight -> MaterialTheme.colorScheme.primary
        themeColor == ThemeColor.ClassicDark -> DarkTertiary
        else -> MaterialTheme.colorScheme.primary
    }

@Composable
private fun DayRow(
    row: MonthlyDayRow,
    themeColor: ThemeColor,
    cellWidth: Dp,
    rowWidth: Dp,
    endInset: Dp,
    scrollState: ScrollState,
) {
    val accent = todayAccentColor(themeColor)
    val classicLightToday = row.isToday && themeColor == ThemeColor.ClassicLight

    val rowDescription = listOf(
        stringResource(R.string.date_column) to row.day,
        stringResource(R.string.fajr) to row.fajr,
        stringResource(R.string.dhuhr) to row.dhuhr,
        stringResource(R.string.asr) to row.asr,
        stringResource(R.string.maghrib) to row.maghrib,
        stringResource(R.string.isha) to row.isha,
    ).joinToString(", ") { (name, value) -> "$name $value" }
        .let { if (row.isToday) "${stringResource(R.string.today)}, $it" else it }
    Row(
        Modifier
            .fillMaxWidth()
            .then(
                if (classicLightToday) {
                    Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .background(ClassicHighlightBackground)
                } else {
                    Modifier
                },
            )
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp)
            .widthIn(min = rowWidth)
            .padding(end = endInset)
            .semantics(mergeDescendants = true) { contentDescription = rowDescription },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        listOf(row.day, row.fajr, row.dhuhr, row.asr, row.maghrib, row.isha).forEach {
            Text(
                it,
                modifier = Modifier.width(cellWidth),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (row.isToday) FontWeight.ExtraBold else FontWeight.Normal,
                color = if (row.isToday) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun previewRows() = (1..31).map { MonthlyDayRow(it.toString(), "03:59", "03:59", "03:59", "03:59", "03:59", isToday = it == 15) }

@Preview(showBackground = true)
@Composable
private fun MonthLabelButtonSecondaryPreview() {
    AlHudaThemePreview {
        MonthLabelButton(
            label = "1403, Khordad",
            calendarMode = MonthlyCalendarMode.SECONDARY,
            onClick = {},
            modifier = Modifier.padding(10.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthLabelButtonLunarPreview() {
    AlHudaThemePreview {
        MonthLabelButton(
            label = "1445, Dhu al-Qadah",
            calendarMode = MonthlyCalendarMode.LUNAR,
            onClick = {},
            modifier = Modifier.padding(10.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HeaderRowPreview() {
    AlHudaThemePreview {
        HeaderRow(64.dp, 320.dp, ROW_END_INSET, rememberScrollState())
    }
}

@Preview(showBackground = true)
@Composable
private fun DayRowPreview() {
    AlHudaThemePreview {
        val scroll = rememberScrollState()
        Column {
            DayRow(
                MonthlyDayRow("15", "03:59", "13:00", "16:30", "20:15", "21:45", isToday = true),
                ThemeColor.Default,
                64.dp,
                320.dp,
                ROW_END_INSET,
                scroll,
            )
            DayRow(
                MonthlyDayRow("16", "04:00", "13:00", "16:30", "20:16", "21:46"),
                ThemeColor.Default,
                64.dp,
                320.dp,
                ROW_END_INSET,
                scroll,
            )
        }
    }
}

@Preview(heightDp = 600)
@Composable
private fun MonthlyViewPreview() {
    AlHudaThemePreview {
        MonthlyViewScreen(
            uiState = MonthlyViewUiState(
                monthLabel = "1403, Khordad",
                rows = previewRows(),
            ),
            onAction = {},
        )
    }
}
