package com.antigravity.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

private val AntigravityColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Navy950,
    secondary = EmeraldAccent,
    onSecondary = Navy950,
    tertiary = AmberAccent,
    background = Navy950,
    onBackground = TextPrimary,
    surface = Slate850,
    onSurface = TextPrimary,
    surfaceVariant = Slate800,
    onSurfaceVariant = TextSecondary,
    error = RoseAccent,
    onError = TextPrimary,
    outline = GlassBorder
)

@Composable
fun AntigravityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AntigravityColorScheme,
        typography = AntigravityTypography,
        content = content
    )
}

@Composable
fun AntigravityBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Navy950, Navy900, Slate850),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 2200f)
                )
            )
    ) {
        content()
    }
}
