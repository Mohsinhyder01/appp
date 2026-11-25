package com.antigravity.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antigravity.app.ui.theme.GlassBorder
import com.antigravity.app.ui.theme.GlassSurface
import com.antigravity.app.ui.theme.GlassSurfaceElevated

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    borderColor: Color = GlassBorder,
    elevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .clip(shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Surface(
        modifier = baseModifier,
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    colors = if (elevated) {
                        listOf(GlassSurfaceElevated, GlassSurface)
                    } else {
                        listOf(GlassSurface, GlassSurface.copy(alpha = 0.58f))
                    }
                )
            ),
            content = content
        )
    }
}
