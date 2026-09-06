package com.alhuda.app.core.presentation.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

class DragAndDropState {
    var payload by mutableStateOf<Any?>(null)
        private set
    private var ghost by mutableStateOf<(@Composable () -> Unit)?>(null)
    private var pointerWindow by mutableStateOf(Offset.Zero)
    private var containerOrigin by mutableStateOf(Offset.Zero)

    var hoveredTargetKey by mutableStateOf<Any?>(null)
        private set

    val pointerWindowPosition: Offset get() = pointerWindow

    private val targets = mutableStateMapOf<Any, Target>()

    private class Target(
        val bounds: Rect,
        val onDrop: (Any) -> Unit,
    )

    val isDragging: Boolean get() = payload != null

    internal fun updateContainerOrigin(coords: LayoutCoordinates) {
        containerOrigin = coords.localToWindow(Offset.Zero)
    }

    internal fun registerTarget(
        key: Any,
        bounds: Rect,
        onDrop: (Any) -> Unit,
    ) {
        targets[key] = Target(bounds, onDrop)
    }

    internal fun unregisterTarget(key: Any) {
        targets.remove(key)
    }

    internal fun start(
        payload: Any,
        ghost: @Composable () -> Unit,
        pointerWindow: Offset,
    ) {
        this.payload = payload
        this.ghost = ghost
        this.pointerWindow = pointerWindow
        this.hoveredTargetKey = targetAt(pointerWindow)?.key
    }

    internal fun drag(pointerWindow: Offset) {
        this.pointerWindow = pointerWindow
        this.hoveredTargetKey = targetAt(pointerWindow)?.key
    }

    internal fun drop() {
        val p = payload
        if (p != null) targetAt(pointerWindow)?.value?.onDrop?.invoke(p)
        cancel()
    }

    internal fun cancel() {
        payload = null
        ghost = null
        hoveredTargetKey = null
    }

    private fun targetAt(point: Offset): Map.Entry<Any, Target>? =
        targets.entries
            .filter { it.value.bounds.contains(point) }
            .minByOrNull { it.value.bounds.width * it.value.bounds.height }

    internal fun ghostContent(): (@Composable () -> Unit)? = ghost

    internal fun pointerInContainer(): IntOffset {
        val local = pointerWindow - containerOrigin
        return IntOffset(local.x.roundToInt(), local.y.roundToInt())
    }
}

@Composable
fun rememberDragAndDropState(): DragAndDropState = remember { DragAndDropState() }

@Composable
fun DragAndDropContainer(
    state: DragAndDropState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.onGloballyPositioned { state.updateContainerOrigin(it) }) {
        content()
        if (state.isDragging) {
            val ghost = state.ghostContent()

            var size by remember { mutableStateOf(IntSize.Zero) }
            val finger = state.pointerInContainer()
            val layoutDirection = LocalLayoutDirection.current

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(finger.x - size.width / 2, finger.y - size.height / 2),
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false,
                        clippingEnabled = false,
                    ),
                ) {
                    Box(
                        Modifier
                            .onSizeChanged { size = it }
                            .graphicsLayer { alpha = 0.9f },
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                            ghost?.invoke()
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.dragSource(
    state: DragAndDropState,
    payload: Any,
    ghost: @Composable () -> Unit,
): Modifier =
    composed {
        var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
        this
            .onGloballyPositioned { coords = it }
            .pointerInput(payload) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { local ->
                        val c = coords ?: return@detectDragGesturesAfterLongPress
                        state.start(payload = payload, ghost = ghost, pointerWindow = c.localToWindow(local))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        coords?.let { state.drag(it.localToWindow(change.position)) }
                    },
                    onDragEnd = { state.drop() },
                    onDragCancel = { state.cancel() },
                )
            }
    }

fun Modifier.dropTarget(
    state: DragAndDropState,
    key: Any,
    onDrop: (Any) -> Unit,
): Modifier =
    composed {
        DisposableEffect(key) {
            onDispose { state.unregisterTarget(key) }
        }
        this.onGloballyPositioned { coords ->
            state.registerTarget(key, coords.boundsInWindow(), onDrop)
        }
    }
