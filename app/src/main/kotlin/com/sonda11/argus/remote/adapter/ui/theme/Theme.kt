package com.sonda11.argus.remote.adapter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArgusColorScheme = darkColorScheme(
    primary = Color(0xFFFFC400),
    onPrimary = Color(0xFF1B1300),
    primaryContainer = Color(0xFF4A3A00),
    onPrimaryContainer = Color(0xFFFFE082),
    secondary = Color(0xFF7CD4FD),
    onSecondary = Color(0xFF00344A),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFE6EAF2),
    surface = Color(0xFF141B2E),
    onSurface = Color(0xFFE6EAF2),
    surfaceVariant = Color(0xFF232C44),
    onSurfaceVariant = Color(0xFFB8C1D9),
    outline = Color(0xFF3A4560),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2B0000),
    errorContainer = Color(0xFF5A1A1A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun ArgusRemoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ArgusColorScheme,
        content = content,
    )
}
