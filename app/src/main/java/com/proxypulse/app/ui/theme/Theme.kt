package com.proxypulse.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

private val DarkColors = darkColorScheme(
    primary = Cyan,
    onPrimary = BgBottom,
    secondary = Indigo,
    background = BgBottom,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = Outline,
    error = PingBad
)

object Brand {
    val accent = Brush.linearGradient(listOf(Cyan, Indigo))
    val accentWide = Brush.linearGradient(listOf(Cyan, Violet, Indigo))
    fun background() = Brush.verticalGradient(listOf(BgTop, BgBottom))
    fun radialGlow(center: Offset) =
        Brush.radialGradient(
            colors = listOf(Indigo.copy(alpha = 0.22f), BgBottom.copy(alpha = 0f)),
            center = center,
            radius = 900f
        )
}

@Composable
fun ProxyPulseTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // The app is intentionally dark-only for the "live console" aesthetic.
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}
