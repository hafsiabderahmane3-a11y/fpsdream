package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = RogCrimson,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF93001E),
    onSecondaryContainer = Color(0xFFFFDAD9),
    tertiary = RazerGreen,
    onTertiary = Color(0xFF003918),
    tertiaryContainer = Color(0xFF005325),
    onTertiaryContainer = Color(0xFF6CFF91),
    background = CyberBackground,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder,
    outlineVariant = Color(0xFF1E293B)
)

@Composable
fun GameTurboTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GameDarkColorScheme,
        typography = Typography,
        content = content
    )
}
