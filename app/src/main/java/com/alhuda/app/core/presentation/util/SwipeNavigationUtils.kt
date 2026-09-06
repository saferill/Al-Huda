package com.alhuda.app.core.presentation.util

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs

fun Modifier.swipeNavigate(
    onNext: () -> Unit,
    onPrev: () -> Unit,
    minVelocity: Dp = 600.dp,
    minDistance: Dp = 72.dp,
): Modifier =
    composed {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        pointerInput(onNext, onPrev, isRtl) {
            val minVelocityPx = minVelocity.toPx()
            val minDistancePx = minDistance.toPx()
            var total = 0f
            val tracker = VelocityTracker()
            detectHorizontalDragGestures(
                onDragStart = {
                    total = 0f
                    tracker.resetTracking()
                },
                onDragEnd = {
                    val velocity = tracker.calculateVelocity().x
                    if (abs(velocity) < minVelocityPx || abs(total) < minDistancePx) return@detectHorizontalDragGestures

                    val forward = if (isRtl) velocity > 0 else velocity < 0
                    if (forward) onNext() else onPrev()
                },
                onHorizontalDrag = { change, dragAmount ->
                    total += dragAmount
                    tracker.addPosition(change.uptimeMillis, change.position)
                },
            )
        }
    }
