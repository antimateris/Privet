package com.example.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom modifier to add a visible, vibrant colored drop shadow matching the element's accent color.
 * Combines hardware-accelerated Compose elevation shadow (spotColor & ambientColor)
 * with a soft directional glow for a modern, tactile depth effect.
 */
fun Modifier.coloredShadow(
    color: Color,
    alpha: Float = 0.38f,
    borderRadius: Dp = 16.dp,
    shadowRadius: Dp = 8.dp,
    offsetY: Dp = 4.dp,
    offsetX: Dp = 0.dp,
    elevation: Dp = 6.dp,
    shape: Shape = RoundedCornerShape(borderRadius)
): Modifier {
    val spotAlpha = (alpha * 1.5f).coerceIn(0.2f, 0.95f)
    val ambientAlpha = (alpha * 0.7f).coerceIn(0.1f, 0.6f)

    return this
        .drawBehind {
            val glowH = offsetY.toPx() + shadowRadius.toPx()
            val radiusPx = borderRadius.toPx()

            // Smooth downward colored glow matching the element color
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = (alpha * 0.45f).coerceIn(0f, 0.5f)),
                        color.copy(alpha = 0f)
                    ),
                    startY = size.height - 2.dp.toPx(),
                    endY = size.height + glowH
                ),
                topLeft = Offset(-1.dp.toPx(), size.height - 4.dp.toPx()),
                size = Size(size.width + 2.dp.toPx(), glowH + 4.dp.toPx()),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
        }
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = color.copy(alpha = ambientAlpha),
            spotColor = color.copy(alpha = spotAlpha)
        )
}
