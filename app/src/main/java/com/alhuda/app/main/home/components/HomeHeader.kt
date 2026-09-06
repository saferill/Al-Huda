package com.alhuda.app.main.home.components

import android.icu.text.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.adhan.i18n
import com.alhuda.app.core.domain.model.calculation.CalculationAdjustments
import com.alhuda.app.core.domain.model.calculation.CalculationLocationDetail
import com.alhuda.app.core.domain.model.favorite_location.StaticFavoriteLocation
import com.alhuda.app.core.domain.usecase.GetNextShariaTimesUseCase
import com.alhuda.app.core.domain.usecase.GetShariaTimesUseCase
import com.alhuda.app.core.domain.util.addDaysTimeZoneAware
import com.alhuda.app.core.domain.util.formatInstant
import com.alhuda.app.core.presentation.AlHudaTheme
import com.alhuda.app.core.presentation.DarkTertiary
import com.alhuda.app.core.presentation.util.patternedBackground
import com.alhuda.app.core.presentation.util.rememberPatternImageBitmap
import com.alhuda.app.main.home.HomeUiAction
import com.alhuda.app.main.home.HomeUiState
import io.github.meypod.adhan_kotlin.CalculationMethod
import kotlin.time.Instant

@Composable
fun HomeHeader(
    uiState: HomeUiState,
    wideLayout: Boolean = false,
    onAction: (HomeUiAction) -> Unit,
) {
    val classic = uiState.themeColor.isClassic()
    val headerContentColor = if (classic) MaterialTheme.colorScheme.onSurface else Color.White
    val countdownColor = if (classic) MaterialTheme.colorScheme.onSurface else DarkTertiary
    val patternImage = rememberPatternImageBitmap(R.drawable.pattern)
    val patternBackgroundColor =
        colorResource(if (uiState.themeColor.isDark()) R.color.header_background_dark else R.color.header_background_light)
    Column(
        Modifier
            .fillMaxWidth()
            .then(
                if (classic) {
                    Modifier
                } else {
                    Modifier.patternedBackground(
                        pattern = patternImage,
                        backgroundColor = patternBackgroundColor,
                        patternAlpha = 0.03f,
                    )
                },
            )
            .padding(dimensionResource(R.dimen.element_padding_compact)),
    ) {
        val iconButtonColors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FilledIconButton(
                onClick = { onAction(HomeUiAction.OnPrevDayClick) },
                colors = iconButtonColors,
            ) {
                Icon(painterResource(R.drawable.arrow_back), contentDescription = stringResource(R.string.prev_day))
            }
            HomeDateColumn(
                uiState = uiState,
                contentColor = headerContentColor,
                modifier = Modifier.weight(1f),
            )
            FilledIconButton(
                onClick = { onAction(HomeUiAction.OnNextDayClick) },
                colors = iconButtonColors,
            ) {
                Icon(painterResource(R.drawable.arrow_forward), contentDescription = stringResource(R.string.next_day))
            }
        }

        if (uiState.showNextPrayerCountdown && uiState.nextShariaTime != null) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(
                            if (classic) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                countdownColor.copy(alpha = 0.15f)
                            },
                        )
                        .border(
                            width = 1.dp,
                            color = if (classic) MaterialTheme.colorScheme.outlineVariant else countdownColor.copy(alpha = 0.4f),
                            shape = MaterialTheme.shapes.extraLarge,
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = uiState.nextShariaTime.prayer.i18n(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = countdownColor,
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelLarge,
                        color = countdownColor.copy(alpha = 0.6f),
                    )
                    Text(
                        text = uiState.countdownText,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFeatureSettings = "tnum",
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = countdownColor,
                    )
                }
            }
        }

        if (!uiState.themeColor.isClassic()) {
            Spacer(Modifier.height(dimensionResource(R.dimen.home_card_padding)))
        }
    }
}

@Composable
private fun HomeDateColumn(
    uiState: HomeUiState,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val weekday = formatInstant(uiState.viewingInstant, uiState.locale, uiState.calendar, DateFormat.WEEKDAY)
    val gregorianDate = formatInstant(
        uiState.viewingInstant,
        uiState.locale,
        uiState.calendar,
        numberingSystem = uiState.numberingSystem,
    )
    val hijriDate = formatInstant(
        addDaysTimeZoneAware(uiState.viewingInstant, uiState.hijriDateAdjustment),
        uiState.arabicCalendarLocale,
        uiState.arabicCalendar,
        numberingSystem = uiState.numberingSystem,
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$weekday, $gregorianDate",
            color = contentColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = hijriDate,
            color = contentColor.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun HomeHeaderPreview() {
    AlHudaTheme {
        val location = StaticFavoriteLocation("foo", CalculationLocationDetail(0.0, 0.0, label = "Null Island"))
        val instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val getShariaTimesUseCase = GetShariaTimesUseCase()
        val shariahTimes = getShariaTimesUseCase(
            instant = instant,
            calculationParameters = CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters,
            calculationAdjustments = CalculationAdjustments(),
            arabicCalendar = "islamic",
            locationDetail = CalculationLocationDetail(0.0, 0.0),
        )
        val nextShariaTime = GetNextShariaTimesUseCase(getShariaTimesUseCase)(
            instant = instant,
            calculationParameters = CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters,
            calculationAdjustments = CalculationAdjustments(),
            arabicCalendar = "islamic",
            locationDetail = CalculationLocationDetail(0.0, 0.0),
        )
        HomeHeader(
            HomeUiState(
                location = location,
                shariaTimes = shariahTimes,
                showNextPrayerCountdown = true,
                nextShariaTime = nextShariaTime,
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun HomeHeaderWideLayoutPreview() {
    AlHudaTheme {
        val location = StaticFavoriteLocation("foo", CalculationLocationDetail(0.0, 0.0, label = "Null Island"))
        val instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val getShariaTimesUseCase = GetShariaTimesUseCase()
        val shariahTimes = getShariaTimesUseCase(
            instant = instant,
            calculationParameters = CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters,
            calculationAdjustments = CalculationAdjustments(),
            arabicCalendar = "islamic",
            locationDetail = CalculationLocationDetail(0.0, 0.0),
        )
        val nextShariaTime = GetNextShariaTimesUseCase(getShariaTimesUseCase)(
            instant = instant,
            calculationParameters = CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters,
            calculationAdjustments = CalculationAdjustments(),
            arabicCalendar = "islamic",
            locationDetail = CalculationLocationDetail(0.0, 0.0),
        )
        HomeHeader(
            HomeUiState(
                location = location,
                shariaTimes = shariahTimes,
                showNextPrayerCountdown = true,
                nextShariaTime = nextShariaTime,
            ),
            wideLayout = true,
            onAction = {},
        )
    }
}
