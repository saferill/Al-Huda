package com.alhuda.app.main.quran

import androidx.activity.compose.BackHandler
import com.alhuda.app.main.quran.data.QuranDataSource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alhuda.app.R
import com.alhuda.app.core.presentation.components.ScreenScaffold
import com.alhuda.app.core.presentation.navigation.Route
import com.alhuda.app.main.quran.model.JuzInfo
import com.alhuda.app.main.quran.model.QuranBookmark
import com.alhuda.app.main.quran.model.Surah
import kotlinx.coroutines.launch

@Composable
fun QuranRoute(
    uiState: QuranUiState,
    onAction: (QuranUiAction) -> Unit,
) {
    QuranScreen(
        uiState = uiState,
        onAction = onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    uiState: QuranUiState,
    onAction: (QuranUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerState) {
                Text(
                    stringResource(R.string.app_name),
                    modifier = Modifier.padding(dimensionResource(R.dimen.page_padding)),
                )
                HorizontalDivider()
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    NavigationDrawerItem(
                        icon = {
                            Icon(painterResource(R.drawable.alarm), contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.reminders_title)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAction(QuranUiAction.OnNavigate(Route.Main.Reminder))
                        },
                    )
                    NavigationDrawerItem(
                        icon = {
                            Icon(painterResource(R.drawable.compass_outline), contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.qibla)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAction(QuranUiAction.OnNavigate(Route.Main.Qibla))
                        },
                    )
                    NavigationDrawerItem(
                        icon = {
                            Icon(painterResource(R.drawable.counter), contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.counter)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAction(QuranUiAction.OnNavigate(Route.Main.Counter))
                        },
                    )
                    NavigationDrawerItem(
                        icon = {
                            Icon(painterResource(R.drawable.calendar_month_outline), contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.monthly_view_title)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAction(QuranUiAction.OnNavigate(Route.Main.MonthlyView))
                        },
                    )
                    NavigationDrawerItem(
                        icon = {
                            Icon(painterResource(R.drawable.settings), contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.settings)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAction(QuranUiAction.OnNavigate(Route.Main.Settings))
                        },
                    )
                    NavigationDrawerItem(
                        icon = {
                            Icon(painterResource(R.drawable.outline_menu_book_24), contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.prayer_guide)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAction(QuranUiAction.OnNavigate(Route.Main.PrayerGuide))
                        },
                    )
                    NavigationDrawerItem(
                        icon = {
                            Icon(painterResource(R.drawable.info_variant_outline), contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.about)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAction(QuranUiAction.OnNavigate(Route.Main.About))
                        },
                    )
                }
            }
        },
    ) {
        ScreenScaffold(
            title = stringResource(R.string.quran),
            onBackClick = {},
            navigationIcon = {
                IconButton(
                    onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                ) {
                    Icon(painterResource(R.drawable.menu), contentDescription = stringResource(R.string.menu))
                }
            },
            scrollable = false,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { onAction(QuranUiAction.OnBackClick) },
                        icon = {
                            Icon(painterResource(R.drawable.alarm), contentDescription = stringResource(R.string.nav_prayer_times))
                        },
                        label = { Text(stringResource(R.string.nav_prayer_times)) },
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = {
                            Icon(painterResource(R.drawable.quran), contentDescription = stringResource(R.string.quran))
                        },
                        label = { Text(stringResource(R.string.quran)) },
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                if (uiState.bookmark != null && uiState.searchQuery.isEmpty()) {
                    LastReadCard(
                        bookmark = uiState.bookmark,
                        onClick = { onAction(QuranUiAction.OnBookmarkClick(uiState.bookmark)) },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onAction(QuranUiAction.OnSearchQueryChange(it)) },
                    placeholder = { Text(stringResource(R.string.search_surah)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.outline_location_searching_24),
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onAction(QuranUiAction.OnSearchQueryChange("")) }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_close_24),
                                    contentDescription = null,
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = Color.Transparent,
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { onAction(QuranUiAction.OnTabSelected(0)) },
                        text = { Text(stringResource(R.string.surah)) },
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { onAction(QuranUiAction.OnTabSelected(1)) },
                        text = { Text(stringResource(R.string.juz)) },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.selectedTab == 0) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.surahs, key = { it.number }) { surah ->
                            SurahListItem(
                                surah = surah,
                                onClick = { onAction(QuranUiAction.OnSurahClick(surah.number)) },
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.juzList, key = { it.number }) { juz ->
                            JuzListItem(
                                juz = juz,
                                onClick = { onAction(QuranUiAction.OnJuzClick(juz)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LastReadCard(
    bookmark: QuranBookmark,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.last_read),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bookmark.surahName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "${stringResource(R.string.page)} ${bookmark.pageNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
            Icon(
                painter = painterResource(R.drawable.quran),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun SurahListItem(
    surah: Surah,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = surah.number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.nameLatin,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${if (surah.isMeccan) stringResource(R.string.revelation_meccan) else stringResource(R.string.revelation_medinan)} • ${surah.versesCount} ${stringResource(R.string.ayah)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = surah.nameArabic,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun JuzListItem(
    juz: JuzInfo,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = juz.number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Juz ${juz.number}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = QuranDataSource.getJuzDescription(juz.number),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = juz.nameArabic,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.End,
            )
        }
    }
}
