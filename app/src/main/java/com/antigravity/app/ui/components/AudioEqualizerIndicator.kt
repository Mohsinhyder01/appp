package com.antigravity.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antigravity.app.ui.theme.CyanAccent

@Composable
fun AudioEqualizerIndicator(
    isPlaying: Boolean,
    color: Color = CyanAccent,
    maxHeight: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_transition")
    val barMultipliers = listOf(0.45f, 0.9f, 0.6f, 0.8f)

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        barMultipliers.forEachIndexed { index, idleFraction ->
            val animatedFraction by infiniteTransition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 420 + index * 110,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "eq_bar_$index"
            )
            val fraction = if (isPlaying) animatedFraction else idleFraction * 0.45f
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(maxHeight * fraction)
                    .background(color = color, shape = RoundedCornerShape(2.dp))
            )
        }
    }
}
