package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun HapticIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ShortPress)
            onClick()
        },
        modifier = modifier.size(36.dp),
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        role = Role.Button
    ) {
        val scale = animateFloatAsState(targetValue = 1f)
        Box(modifier = Modifier.scale(scale.value)) {
            content()
        }
    }
}
