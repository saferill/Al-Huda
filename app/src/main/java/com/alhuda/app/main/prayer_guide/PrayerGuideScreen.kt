package com.alhuda.app.main.prayer_guide

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alhuda.app.R
import com.alhuda.app.core.presentation.components.ACard
import com.alhuda.app.core.presentation.components.ScreenScaffold
import com.alhuda.app.core.presentation.navigation.NavigationController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerGuideScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = { NavigationController.navigateBack() },
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isIndonesian = LocalConfiguration.current.locales[0].language.startsWith("in") ||
        LocalConfiguration.current.locales[0].language.startsWith("id")

    ScreenScaffold(
        title = stringResource(R.string.prayer_guide),
        onBackClick = onBackClick,
        modifier = modifier,
        scrollable = false,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(R.dimen.page_padding)),
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.tab_dhikr_after_prayer)) },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.tab_special_prayers)) },
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.element_padding)))

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
                ) {
                    items(PrayerGuideData.dhikrList, key = { it.id }) { item ->
                        DhikrCard(item = item, isIndonesian = isIndonesian)
                    }
                    item { Spacer(modifier = Modifier.height(dimensionResource(R.dimen.element_padding))) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
                ) {
                    items(PrayerGuideData.prayerGuides, key = { it.id }) { item ->
                        PrayerGuideCard(item = item, isIndonesian = isIndonesian)
                    }
                    item { Spacer(modifier = Modifier.height(dimensionResource(R.dimen.element_padding))) }
                }
            }
        }
    }
}

@Composable
private fun DhikrCard(
    item: DhikrItem,
    isIndonesian: Boolean,
) {
    var tapCount by remember { mutableIntStateOf(0) }

    ACard(modifier = Modifier.fillMaxWidth()) { cardPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.id.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = if (isIndonesian) item.titleId else item.titleEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                if (item.source.isNotEmpty()) {
                    Text(
                        text = item.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = item.arabic,
                style = MaterialTheme.typography.headlineSmall.copy(
                    lineHeight = 38.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.latin,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isIndonesian) item.translationId else item.translationEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            )

            if (item.targetCount > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val isDone = tapCount >= item.targetCount
                    FilledTonalButton(
                        onClick = {
                            if (tapCount < item.targetCount) {
                                tapCount++
                            } else {
                                tapCount = 0
                            }
                        },
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = if (isDone) {
                                stringResource(R.string.completed)
                            } else {
                                stringResource(R.string.times_count, tapCount, item.targetCount)
                            },
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    if (tapCount > 0) {
                        IconButton(onClick = { tapCount = 0 }) {
                            Icon(
                                painter = painterResource(R.drawable.outline_stop_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerGuideCard(
    item: PrayerGuideItem,
    isIndonesian: Boolean,
) {
    var isExpanded by remember { mutableStateOf(false) }

    ACard(
        onClick = { isExpanded = !isExpanded },
        modifier = Modifier.fillMaxWidth(),
    ) { cardPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.id.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isIndonesian) item.titleId else item.titleEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (isIndonesian) item.subtitleId else item.subtitleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Icon(
                    painter = painterResource(
                        if (isExpanded) R.drawable.chevron_up_single else R.drawable.baseline_arrow_drop_down_24,
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (item.intentionArabic.isNotEmpty()) {
                        Text(
                            text = if (isIndonesian) "Niat / Doa Utama" else "Intention / Key Recitation",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.intentionArabic,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                lineHeight = 38.sp,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.intentionLatin,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isIndonesian) item.intentionTranslationId else item.intentionTranslationEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Text(
                        text = if (isIndonesian) "Tata Cara & Pelaksanaan" else "Steps & Guidelines",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val steps = if (isIndonesian) item.stepsId else item.stepsEn
                    steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                        ) {
                            Text(
                                text = "${index + 1}. ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    val notes = if (isIndonesian) item.notesId else item.notesEn
                    if (notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
        }
    }
}
