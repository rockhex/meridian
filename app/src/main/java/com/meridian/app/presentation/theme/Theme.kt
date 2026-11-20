package com.meridian.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2D4F5E), onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4EA), onPrimaryContainer = Color(0xFF12303D),
    secondary = Color(0xFF4F6D7A), onSecondary = Color.White,
    background = Color(0xFFF4F6F7), onBackground = Color(0xFF1B2327),
    surface = Color.White, onSurface = Color(0xFF1B2327),
    surfaceVariant = Color(0xFFE6ECEF), onSurfaceVariant = Color(0xFF4A575E),
    outline = Color(0xFF8A979E), outlineVariant = Color(0xFFD3DBDF),
    error = Color(0xFFB3261E), errorContainer = Color(0xFFFCE8E6), onErrorContainer = Color(0xFF5C1410),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CC5D6), onPrimary = Color(0xFF0E2A36),
    primaryContainer = Color(0xFF244757), onPrimaryContainer = Color(0xFFD6E4EA),
    secondary = Color(0xFF9DB7C2), onSecondary = Color(0xFF0E2A36),
    background = Color(0xFF111A1E), onBackground = Color(0xFFE2E8EB),
    surface = Color(0xFF182227), onSurface = Color(0xFFE2E8EB),
    surfaceVariant = Color(0xFF26343A), onSurfaceVariant = Color(0xFFB5C2C8),
    outline = Color(0xFF75848B), outlineVariant = Color(0xFF3A4A51),
    error = Color(0xFFF2B8B5), errorContainer = Color(0xFF601410), onErrorContainer = Color(0xFFF9DEDC),
)

@Composable
fun MeridianTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
