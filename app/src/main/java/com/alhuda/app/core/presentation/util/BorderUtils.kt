package com.alhuda.app.core.presentation.util

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@SuppressLint("UnnecessaryComposedModifier")
@Stable
fun Modifier.unifiedBorder(width: Dp = OutlinedTextFieldDefaults.UnfocusedBorderThickness) =
    composed {
        val color = MaterialTheme.colorScheme.onSurfaceVariant
        val shape = MaterialTheme.shapes.small
        this.border(width, color, shape)
    }

fun Modifier.bottomBorder(
    color: Color,
    thickness: Dp,
    horizontalPadding: Dp = 0.dp,
) = this.then(
    Modifier.drawWithContent {

        drawContent()

        val thicknessPx = thickness.toPx()
        val horizontalPaddingPx = horizontalPadding.toPx()

        val borderTopLeft = Offset(
            x = horizontalPaddingPx,
            y = size.height - thicknessPx,
        )
        val borderSize = Size(
            width = size.width - 2 * horizontalPaddingPx,
            height = thicknessPx,
        )

        drawRect(
            color = color,
            topLeft = borderTopLeft,
            size = borderSize,
        )
    },
)

private fun DrawScope.shapeBorderPath(shape: Shape, strokeWidth: Float): Path {
    val inset = strokeWidth / 2
    val outline = shape.createOutline(
        size = Size(size.width - strokeWidth, size.height - strokeWidth),
        layoutDirection = layoutDirection,
        density = this,
    )
    return Path().apply {
        addOutline(outline)
        translate(Offset(inset, inset))
    }
}

@Stable
fun Modifier.dashedBorder(
    borderColor: Color,
    shape: Shape,
    strokeWidth: Float = 3f,
    dashWidth: Float = 20f,
    dashGap: Float = 20f,
): Modifier =
    this.drawBehind {
        val path = shapeBorderPath(shape, strokeWidth)

        val perimeter = android.graphics.PathMeasure(path.asAndroidPath(), true).length
        val period = dashWidth + dashGap
        val ratio = dashWidth / period
        val periods = (perimeter / period).roundToInt().coerceAtLeast(1)
        val snapped = perimeter / periods
        val on = snapped * ratio
        val off = snapped - on
        drawPath(
            path = path,
            color = borderColor,
            style = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(on, off), 0f),
            ),
        )
    }

@Stable
fun Modifier.solidBorder(
    borderColor: Color,
    shape: Shape,
    strokeWidth: Float = 3f,
): Modifier =
    this.drawBehind {
        drawPath(
            path = shapeBorderPath(shape, strokeWidth),
            color = borderColor,
            style = Stroke(width = strokeWidth),
        )
    }
