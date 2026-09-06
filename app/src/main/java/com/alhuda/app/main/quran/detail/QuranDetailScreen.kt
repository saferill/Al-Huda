package com.alhuda.app.main.quran.detail

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.alhuda.app.R
import kotlinx.coroutines.launch

@Composable
fun QuranDetailRoute(
    uiState: QuranDetailUiState,
    onAction: (QuranDetailUiAction) -> Unit,
    loadPageBitmap: suspend (Int) -> Bitmap?,
    initialPage: Int = uiState.currentPage,
) {
    QuranDetailScreen(
        uiState = uiState,
        onAction = onAction,
        loadPageBitmap = loadPageBitmap,
        initialPage = initialPage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranDetailScreen(
    uiState: QuranDetailUiState,
    onAction: (QuranDetailUiAction) -> Unit,
    loadPageBitmap: suspend (Int) -> Bitmap?,
    initialPage: Int = 1,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val startPage = remember(initialPage) { (initialPage - 1).coerceIn(0, 603) }
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { 604 },
    )
    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpInputText by remember { mutableStateOf("") }

    LaunchedEffect(uiState.currentPage) {
        val targetIndex = uiState.currentPage - 1
        if (pagerState.currentPage != targetIndex && !pagerState.isScrollInProgress) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { index ->
            val pageNum = index + 1
            if (uiState.currentPage != pageNum) {
                onAction(QuranDetailUiAction.OnPageChanged(pageNum))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        TopAppBar(
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.surah?.nameLatin ?: stringResource(R.string.quran),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (uiState.surah != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  ${uiState.surah.nameArabic}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Text(
                        text = "Juz ${uiState.currentJuz}  •  ${stringResource(R.string.page)} ${uiState.currentPage} / 604",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { onAction(QuranDetailUiAction.OnBackClick) }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = stringResource(R.string.back_button),
                    )
                }
            },
            actions = {
                IconButton(onClick = { onAction(QuranDetailUiAction.OnPlaySurahAudio) }) {
                    if (uiState.isBufferingAudio) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            painter = painterResource(
                                if (uiState.isPlayingAudio) R.drawable.outline_stop_24 else R.drawable.outline_play_arrow_24
                            ),
                            contentDescription = null,
                            tint = if (uiState.isPlayingAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = {
                    jumpInputText = uiState.currentPage.toString()
                    showJumpDialog = true
                }) {
                    Icon(
                        painter = painterResource(R.drawable.outline_location_searching_24),
                        contentDescription = stringResource(R.string.search_surah),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = {
                    onAction(QuranDetailUiAction.OnBookmarkCurrentPage)
                    Toast.makeText(context, R.string.bookmark_saved, Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        painter = painterResource(
                            if (uiState.isBookmarked) R.drawable.baseline_check_24 else R.drawable.outline_push_pin_24
                        ),
                        contentDescription = null,
                        tint = if (uiState.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        HorizontalPager(
            state = pagerState,
            reverseLayout = false,
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->
            val pageNumber = pageIndex + 1
            MushafPageView(
                page = pageNumber,
                loadPageBitmap = loadPageBitmap,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (showJumpDialog) {
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text(stringResource(R.string.page)) },
            text = {
                Column {
                    Text(
                        text = "1 - 604",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jumpInputText,
                        onValueChange = { jumpInputText = it.filter { ch -> ch.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val pageNum = jumpInputText.toIntOrNull()
                    if (pageNum != null && pageNum in 1..604) {
                        scope.launch {
                            pagerState.scrollToPage(pageNum - 1)
                        }
                    }
                    showJumpDialog = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun MushafPageView(
    page: Int,
    loadPageBitmap: suspend (Int) -> Bitmap?,
    modifier: Modifier = Modifier,
) {
    val pageBgColor = Color(0xFFFAF7F0)
    val bitmapState by produceState<Bitmap?>(initialValue = null, key1 = page) {
        value = loadPageBitmap(page)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pageBgColor)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        val bitmap = bitmapState
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Page $page",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${stringResource(R.string.page)} $page",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
