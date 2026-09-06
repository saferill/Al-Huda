package com.alhuda.app.main.settings.widget.custom

import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.alhuda.app.R
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.adhan.SHARIA_TIMES_IN_ORDER
import com.alhuda.app.core.domain.model.adhan.i18n
import com.alhuda.app.core.domain.model.widget.CustomWidgetConfig
import com.alhuda.app.core.domain.model.widget.CustomWidgetData
import com.alhuda.app.core.domain.model.widget.DateCalendar
import com.alhuda.app.core.domain.model.widget.HeaderBlock
import com.alhuda.app.core.domain.model.widget.withPrayerPlaced
import com.alhuda.app.core.domain.model.widget.withPrayerRemoved
import com.alhuda.app.core.presentation.AlHudaThemePreview
import com.alhuda.app.core.presentation.components.ACard
import com.alhuda.app.core.presentation.components.BottomSelect
import com.alhuda.app.core.presentation.components.ColorPickerField
import com.alhuda.app.core.presentation.components.DragAndDropContainer
import com.alhuda.app.core.presentation.components.DragAndDropState
import com.alhuda.app.core.presentation.components.ScreenScaffold
import com.alhuda.app.core.presentation.components.SettingHeader
import com.alhuda.app.core.presentation.components.SettingSwitch
import com.alhuda.app.core.presentation.components.dragSource
import com.alhuda.app.core.presentation.components.dropTarget
import com.alhuda.app.core.presentation.components.rememberDragAndDropState
import com.alhuda.app.core.presentation.navigation.NavigationController
import com.alhuda.app.widget.CustomWidgetRenderer
import kotlin.math.roundToInt

@Composable
fun CustomWidgetBuilderScreen(
    uiState: CustomWidgetBuilderUiState,
    onAction: (CustomWidgetBuilderUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dnd = rememberDragAndDropState()
    var pinned by rememberSaveable { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    val spacing = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding))

    DragAndDropContainer(dnd, modifier.fillMaxWidth()) {
        ScreenScaffold(
            title = stringResource(R.string.widget_section_custom_appearance),
            onBackClick = { NavigationController.navigateBack() },
            scrollable = false,
        ) {
            if (pinned) {
                PreviewSection(uiState.previewData, pinned = true, onTogglePin = { pinned = it })
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = spacing,
                ) {
                    OptionCards(uiState, onAction, dnd)
                }
            } else {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = spacing,
                ) {
                    PreviewSection(uiState.previewData, pinned = false, onTogglePin = { pinned = it })
                    OptionCards(uiState, onAction, dnd)
                }
            }
        }
    }
}

@Composable
private fun PreviewSection(
    data: CustomWidgetData?,
    pinned: Boolean,
    onTogglePin: (Boolean) -> Unit,
) {
    Column {
        FilterChip(
            selected = pinned,
            onClick = { onTogglePin(!pinned) },
            label = { Text(stringResource(R.string.custom_widget_pin_preview)) },
            leadingIcon = {
                AnimatedVisibility(
                    visible = pinned,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally(),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_push_pin_24),
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                }
            },
            modifier = Modifier.align(Alignment.End),
        )
        val previewHeight = if (data != null && data.pages.size > 1) 180.dp else 130.dp
        CustomWidgetPreview(
            data = data,
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeight)
                .clip(MaterialTheme.shapes.medium),
        )
    }
}

@Composable
private fun CustomWidgetPreview(
    data: CustomWidgetData?,
    modifier: Modifier = Modifier,
) {
    if (data == null || LocalInspectionMode.current) {
        Box(modifier.clip(MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.custom_widget_no_locations))
        }
        return
    }
    if (data.pages.size > 1) PagedPreview(data, modifier) else SingleWidgetView(data, modifier)
}

@Composable
private fun PagedPreview(
    data: CustomWidgetData,
    modifier: Modifier = Modifier,
) {
    val pageCount = data.pages.size
    var index by remember(pageCount) { mutableStateOf(0) }
    Box(modifier) {
        WidgetView(data = data, pageIndex = index, modifier = Modifier.fillMaxSize())
        Box(
            Modifier
                .matchParentSize()
                .pointerInput(Unit) { detectTapGestures { } },
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.35f)
                .height(44.dp)
                .pointerInput(pageCount) { detectTapGestures { index = (index - 1 + pageCount) % pageCount } },
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.35f)
                .height(44.dp)
                .pointerInput(pageCount) { detectTapGestures { index = (index + 1) % pageCount } },
        )
    }
}

@Composable
private fun WidgetView(
    data: CustomWidgetData,
    pageIndex: Int,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx -> FrameLayout(ctx) },
        update = { frame ->
            frame.removeAllViews()
            frame.addView(CustomWidgetRenderer.build(frame.context, data, pageIndex).apply(frame.context, frame))
        },
    )
}

@Composable
private fun SingleWidgetView(
    data: CustomWidgetData,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx -> FrameLayout(ctx) },
        update = { frame ->
            frame.removeAllViews()
            val remoteViews = CustomWidgetRenderer.build(frame.context, data)
            frame.addView(remoteViews.apply(frame.context, frame))
        },
    )
}

@Composable
private fun ColumnScope.OptionCards(
    uiState: CustomWidgetBuilderUiState,
    onAction: (CustomWidgetBuilderUiAction) -> Unit,
    dnd: DragAndDropState,
) {
    val context = LocalContext.current
    val config = uiState.config
    val defaultBg = ContextCompat.getColor(context, R.color.custom_widget_bg)
    val defaultText = ContextCompat.getColor(context, R.color.custom_widget_text)
    val defaultHighlight = ContextCompat.getColor(context, R.color.custom_widget_highlight)

    val defaultCountdown = ContextCompat.getColor(context, R.color.secondary_text_color)

    ACard { cardPadding ->
        Column(
            Modifier.padding(cardPadding),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
        ) {
            ColorPickerField(
                label = stringResource(R.string.custom_widget_background_color),
                colorArgb = config.bgColor,
                defaultArgb = defaultBg,
                onColorChanged = { onAction(CustomWidgetBuilderUiAction.OnBgColorChange(it)) },
            )
            ColorPickerField(
                label = stringResource(R.string.custom_widget_text_color),
                colorArgb = config.textColor,
                defaultArgb = defaultText,
                onColorChanged = { onAction(CustomWidgetBuilderUiAction.OnTextColorChange(it)) },
            )
            ColorPickerField(
                label = stringResource(R.string.custom_widget_highlight_color),
                colorArgb = config.highlightColor,
                defaultArgb = defaultHighlight,
                onColorChanged = { onAction(CustomWidgetBuilderUiAction.OnHighlightColorChange(it)) },
            )
        }
    }

    ACard { cardPadding ->
        Column(
            Modifier.padding(cardPadding),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
        ) {
            SettingHeader(
                title = stringResource(R.string.custom_widget_header_slots_title),
                subtitle = stringResource(R.string.custom_widget_header_help),
            )
            val swapLabel = stringResource(R.string.custom_widget_swap_header)
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding_compact)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderSlotBox(
                    dnd = dnd,
                    slotIndex = 0,
                    block = config.topStart,
                    emptyLabel = stringResource(R.string.custom_widget_header_start),
                    onDrop = { onAction(headerDropAction(config, targetSlot = 0, item = it)) },
                    onClear = { onAction(CustomWidgetBuilderUiAction.OnTopStartChange(null)) },
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { onAction(CustomWidgetBuilderUiAction.OnHeaderSlotsChange(config.topEnd, config.topStart)) },
                    enabled = config.topStart != null || config.topEnd != null,
                ) {
                    Icon(
                        painterResource(R.drawable.baseline_swap_horiz_24),
                        contentDescription = swapLabel,
                    )
                }
                HeaderSlotBox(
                    dnd = dnd,
                    slotIndex = 1,
                    block = config.topEnd,
                    emptyLabel = stringResource(R.string.custom_widget_header_end),
                    onDrop = { onAction(headerDropAction(config, targetSlot = 1, item = it)) },
                    onClear = { onAction(CustomWidgetBuilderUiAction.OnTopEndChange(null)) },
                    modifier = Modifier.weight(1f),
                )
            }

            val availableHeaderBlocks = HEADER_BLOCK_OPTIONS.filterNot { it == config.topStart || it == config.topEnd }
            val addLabel = stringResource(R.string.custom_widget_add)

            val fillHeaderSlot: (HeaderBlock) -> Unit = { block ->
                if (config.topStart == null) {
                    onAction(CustomWidgetBuilderUiAction.OnTopStartChange(block))
                } else {
                    onAction(CustomWidgetBuilderUiAction.OnTopEndChange(block))
                }
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp)

                    .dropTarget(dnd, "cw_header_palette") { payload ->
                        (payload as? CwDrag.HeaderItem)?.let {
                            when (it.fromSlot) {
                                0 -> onAction(CustomWidgetBuilderUiAction.OnTopStartChange(null))
                                1 -> onAction(CustomWidgetBuilderUiAction.OnTopEndChange(null))
                            }
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding_compact)),
            ) {
                items(availableHeaderBlocks, key = { headerBlockKey(it) }) { block ->
                    val label = headerLabel(block)
                    Box(rtlSafeAnimateItem()) {
                        DraggableChip(
                            dnd = dnd,
                            payload = CwDrag.HeaderItem(block),
                            dragLabel = label,
                            ghost = { ChipSurface { ChipText(label) } },
                            content = {
                                Box(
                                    Modifier
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable(onClickLabel = addLabel) { fillHeaderSlot(block) },
                                ) {
                                    ChipSurface { ChipText(label) }
                                }
                            },
                        )
                    }
                }
            }
            FontSizeSlider(
                scale = config.headerFontScale,
                onChange = { onAction(CustomWidgetBuilderUiAction.OnHeaderFontScaleChange(it)) },
            )
        }
    }

    ACard { cardPadding ->
        Column(
            Modifier.padding(cardPadding),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
        ) {
            SettingHeader(
                title = stringResource(R.string.custom_widget_prayers),
                subtitle = stringResource(R.string.custom_widget_prayers_help),
            )
            BottomSelect(
                modifier = Modifier.fillMaxWidth(),
                options = (1..CustomWidgetConfig.MAX_PRAYER_ROWS).toList(),
                optionKey = { it.toString() },
                optionLabel = { it.toString() },
                selectedKey = config.rows.size.toString(),
                onSelect = { onAction(CustomWidgetBuilderUiAction.OnRowCountChange(it)) },
                label = { Text(stringResource(R.string.custom_widget_rows_label)) },
                placeholder = stringResource(R.string.custom_widget_rows_label),
                supportingText = { Text(stringResource(R.string.custom_widget_rows_help)) },
            )
            config.rows.forEachIndexed { rowIndex, _ ->
                PlacedRow(
                    dnd = dnd,
                    rows = config.rows,
                    rowIndex = rowIndex,
                    times = uiState.prayerTimes,
                    label = if (config.rows.size > 1) stringResource(R.string.custom_widget_row_n, rowIndex + 1) else null,
                    onChange = { onAction(CustomWidgetBuilderUiAction.OnRowsChange(it)) },
                )
            }
            val placed = config.rows.flatten().toSet()
            PrayerPaletteRow(
                dnd = dnd,

                prayers = SHARIA_TIMES_IN_ORDER.filterNot { it in placed },
                times = uiState.prayerTimes,

                onAdd = { onAction(CustomWidgetBuilderUiAction.OnRowsChange(config.rows.withPrayerPlaced(it, config.rows.lastIndex))) },

                onRemove = { onAction(CustomWidgetBuilderUiAction.OnRowsChange(config.rows.withPrayerRemoved(it))) },
            )
            FontSizeSlider(
                scale = config.prayerFontScale,
                onChange = { onAction(CustomWidgetBuilderUiAction.OnPrayerFontScaleChange(it)) },
            )
        }
    }

    ACard { cardPadding ->
        Column(Modifier.padding(cardPadding)) {
            SettingSwitch(
                title = stringResource(R.string.show_countdown_timer),
                subtitle = null,
                checked = config.showCountdown,
                onCheckedChange = { onAction(CustomWidgetBuilderUiAction.OnCountdownToggle(it)) },
            )
            AnimatedVisibility(
                visible = config.showCountdown,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    Modifier.padding(top = dimensionResource(R.dimen.element_padding)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding)),
                ) {
                    ColorPickerField(
                        label = stringResource(R.string.custom_widget_countdown_color),
                        colorArgb = config.countdownColor,
                        defaultArgb = defaultCountdown,
                        onColorChanged = { onAction(CustomWidgetBuilderUiAction.OnCountdownColorChange(it)) },
                    )
                    FontSizeSlider(
                        scale = config.countdownFontScale,
                        onChange = { onAction(CustomWidgetBuilderUiAction.OnCountdownFontScaleChange(it)) },
                    )
                }
            }
        }
    }

    ACard { cardPadding ->
        Column(Modifier.padding(cardPadding)) {
            SettingHeader(
                title = stringResource(R.string.custom_widget_locations),
                subtitle = stringResource(R.string.custom_widget_locations_help),
            )
            if (uiState.locations.isEmpty()) {
                Text(
                    stringResource(R.string.custom_widget_no_locations),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.element_padding)),
                )
            } else {

                val enabled = uiState.locations.filter { it.enabled }

                val travelLabel = stringResource(R.string.location_traveling_mode)
                val display = { loc: LocationToggle -> if (loc.isTravelMode) travelLabel else loc.name }
                val triggerLabel = when {

                    enabled.isEmpty() -> stringResource(R.string.default_value)

                    enabled.size == 1 -> display(enabled.first())

                    else -> stringResource(R.string.custom_widget_multiple_locations)
                }
                BottomSelect(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.element_padding)),
                    options = uiState.locations,
                    optionKey = { it.id },
                    optionLabel = { display(it) },
                    selectedKey = null,
                    searchable = true,
                    selectedLabelOverride = triggerLabel,
                    itemContent = { entry, _, _, _ ->
                        val location = entry.value.first
                        LocationCheckItem(
                            name = display(location),
                            selected = location.enabled,
                            onToggle = {
                                onAction(
                                    CustomWidgetBuilderUiAction.OnLocationToggle(location.id, !location.enabled),
                                )
                            },
                        )
                    },
                )

                val chipGap = dimensionResource(R.dimen.element_padding_compact)
                AnimatedVisibility(
                    visible = enabled.size > 1,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = chipGap),
                    ) {
                        uiState.locations.forEach { location ->
                            AnimatedVisibility(
                                visible = location.enabled,
                                enter = fadeIn() + expandHorizontally(),
                                exit = fadeOut() + shrinkHorizontally(),
                            ) {
                                Box(Modifier.padding(end = chipGap, bottom = chipGap)) {
                                    RemovableChip(
                                        onRemove = {
                                            onAction(CustomWidgetBuilderUiAction.OnLocationToggle(location.id, false))
                                        },
                                    ) {
                                        ChipText(display(location))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FontSizeSlider(
    scale: Float,
    onChange: (Float) -> Unit,
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.custom_widget_font_size))
            Text(
                "${(scale * 100).roundToInt()}%",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = scale,
            onValueChange = onChange,
            valueRange = CustomWidgetConfig.FONT_SCALE_RANGE,

            steps = 14,
        )
    }
}

@Composable
private fun LocationCheckItem(
    name: String,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(name, style = MaterialTheme.typography.bodyMedium) },
        onClick = onToggle,
        modifier = Modifier.semantics { toggleableState = ToggleableState(selected) },
        trailingIcon = {
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.baseline_check_24),
                    contentDescription = null,
                    tint = colorResource(R.color.checkmark_green),
                )
            }
        },
    )
}

@Composable
private fun LazyItemScope.rtlSafeAnimateItem(): Modifier {
    val ltr = LocalLayoutDirection.current == LayoutDirection.Ltr
    return Modifier.animateItem(fadeOutSpec = if (ltr) spring(stiffness = Spring.StiffnessMediumLow) else null)
}

@Composable
private fun PlacedRow(
    dnd: DragAndDropState,
    rows: List<List<Prayer>>,
    rowIndex: Int,
    times: Map<Prayer, String>,
    label: String?,
    onChange: (List<List<Prayer>>) -> Unit,
) {
    val committed = rows[rowIndex]
    val draggedPrayer = (dnd.payload as? CwDrag.PrayerItem)?.prayer
    val listState = rememberLazyListState()
    var rowBounds by remember { mutableStateOf(Rect.Zero) }
    var lazyBounds by remember { mutableStateOf(Rect.Zero) }

    var preview by remember { mutableStateOf<List<Prayer>?>(null) }
    val density = LocalDensity.current

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    LaunchedEffect(dnd.isDragging, draggedPrayer, rowIndex) {
        if (!dnd.isDragging || draggedPrayer == null) {
            preview = null
            return@LaunchedEffect
        }
        val edge = with(density) { 40.dp.toPx() }
        val maxStep = with(density) { 14.dp.toPx() }
        while (dnd.isDragging) {
            withFrameNanos { }
            val f = dnd.pointerWindowPosition
            val overRow = f.y >= rowBounds.top && f.y <= rowBounds.bottom
            if (!overRow) {

                preview = null
                continue
            }

            val leftPen = ((rowBounds.left + edge - f.x) / edge).coerceIn(0f, 1f)
            val rightPen = ((f.x - (rowBounds.right - edge)) / edge).coerceIn(0f, 1f)
            val startPen = if (isRtl) rightPen else leftPen
            val endPen = if (isRtl) leftPen else rightPen
            val dir = when {
                startPen > 0f -> -startPen
                endPen > 0f -> endPen
                else -> 0f
            }
            if (dir != 0f) listState.scrollBy(dir * maxStep)

            val current = preview?.takeIf { draggedPrayer in it }
                ?: if (draggedPrayer in committed) committed else committed + draggedPrayer
            val draggedIndex = current.indexOf(draggedPrayer)

            val centerX = if (isRtl) lazyBounds.right - f.x else f.x - lazyBounds.left
            val target = listState.layoutInfo.visibleItemsInfo.firstOrNull {
                it.index != draggedIndex && centerX >= it.offset && centerX <= it.offset + it.size
            }?.index
            preview = if (target != null && target != draggedIndex) {
                current.toMutableList().apply { add(target, removeAt(draggedIndex)) }
            } else {
                current
            }
        }
    }

    fun commitPreview() {
        val p = preview ?: return
        val dragged = draggedPrayer ?: return
        onChange(rows.withPrayerPlaced(dragged, rowIndex, before = p.getOrNull(p.indexOf(dragged) + 1)))
    }

    val display = preview ?: committed
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding_compact))) {
        if (label != null) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clip(MaterialTheme.shapes.medium)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                .onGloballyPositioned { rowBounds = it.boundsInWindow() }
                .dropTarget(dnd, "cw_row_$rowIndex") { payload ->
                    (payload as? CwDrag.PrayerItem)?.let {
                        if (preview != null) commitPreview() else onChange(rows.withPrayerPlaced(it.prayer, rowIndex))
                    }
                }
                .padding(dimensionResource(R.dimen.element_padding_compact)),
            contentAlignment = if (display.isEmpty()) Alignment.Center else Alignment.CenterStart,
        ) {
            if (display.isEmpty()) {
                Text(
                    stringResource(R.string.custom_widget_drop_prayers),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyRow(
                    state = listState,
                    modifier = Modifier.onGloballyPositioned { lazyBounds = it.boundsInWindow() },
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding_compact)),
                ) {
                    items(display, key = { it }) { prayer ->
                        val name = prayer.i18n()
                        val time = times[prayer] ?: PLACEHOLDER_TIME
                        Box(
                            rtlSafeAnimateItem()

                                .graphicsLayer { alpha = if (prayer == draggedPrayer) 0.3f else 1f }
                                .dropTarget(dnd, prayer) { payload ->
                                    (payload as? CwDrag.PrayerItem)?.let {

                                        if (preview != null) {
                                            commitPreview()
                                        } else {
                                            onChange(rows.withPrayerPlaced(it.prayer, rowIndex, prayer))
                                        }
                                    }
                                },
                        ) {
                            DraggableChip(
                                dnd = dnd,
                                payload = CwDrag.PrayerItem(prayer),
                                dragLabel = "$name $time",

                                ghost = { ChipWithRemove { ChipColumn(name, time) } },
                                content = {
                                    RemovableChip(onRemove = { onChange(rows.withPrayerRemoved(prayer)) }) {
                                        ChipColumn(name, time)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerPaletteRow(
    dnd: DragAndDropState,
    prayers: List<Prayer>,
    times: Map<Prayer, String>,
    onAdd: (Prayer) -> Unit,
    onRemove: (Prayer) -> Unit,
) {
    val addLabel = stringResource(R.string.custom_widget_add)
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxSize()

                .dropTarget(dnd, "cw_prayer_palette") { payload ->
                    (payload as? CwDrag.PrayerItem)?.let { onRemove(it.prayer) }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_padding_compact)),
        ) {
            items(prayers, key = { it }) { prayer ->
                val name = prayer.i18n()
                val time = times[prayer] ?: PLACEHOLDER_TIME
                Box(rtlSafeAnimateItem()) {
                    DraggableChip(
                        dnd = dnd,
                        payload = CwDrag.PrayerItem(prayer),
                        dragLabel = "$name $time",
                        ghost = { ChipSurface { ChipColumn(name, time) } },
                    ) {
                        Box(
                            Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .clickable(onClickLabel = addLabel) { onAdd(prayer) },
                        ) {
                            ChipSurface { ChipColumn(name, time) }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = prayers.isEmpty(),
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.custom_widget_all_prayers_used),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HeaderSlotBox(
    dnd: DragAndDropState,
    slotIndex: Int,
    block: HeaderBlock?,
    emptyLabel: String,
    onDrop: (CwDrag.HeaderItem) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clearLabel = stringResource(R.string.custom_widget_remove)
    val label = block?.let { headerLabel(it) }
    Box(
        modifier
            .heightIn(min = 56.dp)
            .clip(MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .dropTarget(dnd, "cw_slot_$slotIndex") { payload ->
                (payload as? CwDrag.HeaderItem)?.let { onDrop(it) }
            }

            .then(
                if (block != null && label != null) {
                    Modifier
                        .dragSource(
                            dnd,
                            CwDrag.HeaderItem(block, fromSlot = slotIndex),
                            ghost = { ChipSurface { ChipText(label) } },
                        )
                        .clickable(onClickLabel = clearLabel, onClick = onClear)
                } else {
                    Modifier
                },
            )
            .padding(dimensionResource(R.dimen.element_padding_compact)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label ?: emptyLabel,
            color = if (block == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

private fun headerDropAction(
    config: CustomWidgetConfig,
    targetSlot: Int,
    item: CwDrag.HeaderItem,
): CustomWidgetBuilderUiAction =
    if (targetSlot == 0) {
        val newEnd = if (item.fromSlot == 1) config.topStart else config.topEnd
        CustomWidgetBuilderUiAction.OnHeaderSlotsChange(topStart = item.block, topEnd = newEnd)
    } else {
        val newStart = if (item.fromSlot == 0) config.topEnd else config.topStart
        CustomWidgetBuilderUiAction.OnHeaderSlotsChange(topStart = newStart, topEnd = item.block)
    }

@Composable
private fun DraggableChip(
    dnd: DragAndDropState,
    payload: CwDrag,
    dragLabel: String,
    ghost: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier
            .dragSource(dnd, payload, ghost = ghost)
            .semantics(mergeDescendants = true) { contentDescription = dragLabel },
    ) { content() }
}

@Composable
private fun ChipSurface(content: @Composable () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Box(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) { content() }
    }
}

@Composable
private fun ChipColumn(
    name: String,
    time: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, style = MaterialTheme.typography.bodyMedium)
        Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ChipText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun ChipRemoveIcon() {
    Text(
        text = "✕",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 6.dp),
    )
}

@Composable
private fun ChipWithRemove(content: @Composable () -> Unit) {
    ChipSurface {
        Row(verticalAlignment = Alignment.CenterVertically) {
            content()
            ChipRemoveIcon()
        }
    }
}

@Composable
private fun RemovableChip(
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val removeLabel = stringResource(R.string.custom_widget_remove)
    Box(
        modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClickLabel = removeLabel, onClick = onRemove),
    ) {
        ChipWithRemove(content)
    }
}

private sealed interface CwDrag {
    data class PrayerItem(
        val prayer: Prayer,
    ) : CwDrag

    data class HeaderItem(
        val block: HeaderBlock,
        val fromSlot: Int? = null,
    ) : CwDrag
}

private const val PLACEHOLDER_TIME = "--:--"

private val HEADER_BLOCK_OPTIONS: List<HeaderBlock> = buildList {
    add(HeaderBlock.LocationName)

    for (calendar in DateCalendar.entries) {
        add(HeaderBlock.Date(calendar, withDayName = false))
        add(HeaderBlock.Date(calendar, withDayName = true))
    }
}

private fun headerBlockKey(block: HeaderBlock): String =
    when (block) {
        is HeaderBlock.LocationName -> "location"
        is HeaderBlock.Date -> "date:${block.calendar.name}:${block.withDayName}"
    }

@StringRes
private fun calendarNameRes(calendar: DateCalendar): Int =
    when (calendar) {
        DateCalendar.Hijri -> R.string.calendar_lunar
        DateCalendar.Gregorian -> R.string.calendar_gregorian
        DateCalendar.Persian -> R.string.calendar_persian
        DateCalendar.Ethiopic -> R.string.calendar_ethiopic
        DateCalendar.Buddhist -> R.string.calendar_buddhist
    }

@Composable
private fun headerLabel(block: HeaderBlock): String =
    when (block) {
        is HeaderBlock.LocationName -> stringResource(R.string.custom_widget_header_location)

        is HeaderBlock.Date -> {
            val name = stringResource(calendarNameRes(block.calendar))
            if (block.withDayName) {
                stringResource(R.string.custom_widget_header_date_day, name)
            } else {
                stringResource(R.string.custom_widget_header_date, name)
            }
        }
    }

@Preview
@Composable
private fun CustomWidgetBuilderScreenPreview() {
    AlHudaThemePreview {
        CustomWidgetBuilderScreen(uiState = CustomWidgetBuilderUiState(), onAction = {})
    }
}
