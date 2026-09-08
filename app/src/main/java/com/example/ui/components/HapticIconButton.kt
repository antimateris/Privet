package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun HapticIconButton(
    onClick: () -> Unit,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val pressedScale = 0.92f

    IconButton(
        onClick = {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.ShortPress)
            onClick()
        },
        modifier = modifier
            .size(36.dp)
            .then(Modifier),
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        role = Role.Button
    ) {
        val scale = animateFloatAsState(targetValue = 1f)
        // We wrap the content inside scale so future improvements can animate on press
        androidx.compose.foundation.layout.Box(modifier = Modifier.scale(scale.value)) {
            content()
        }
    }
}
