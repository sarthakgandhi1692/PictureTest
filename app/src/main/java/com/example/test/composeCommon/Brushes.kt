package com.example.test.composeCommon

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    return Brush.Companion.linearGradient(
        colors = listOf(
            Color.Companion.LightGray.copy(alpha = 0.6f),
            Color.Companion.LightGray.copy(alpha = 0.2f),
            Color.Companion.LightGray.copy(alpha = 0.6f)
        ),
        start = Offset.Companion.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
}