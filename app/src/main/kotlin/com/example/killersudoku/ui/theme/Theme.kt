package com.example.killersudoku.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Forest,
    secondary = Copper,
    tertiary = Coral,
    background = Paper,
    surface = Paper,
    surfaceVariant = Mist,
    onPrimary = Paper,
    onSecondary = Ink,
    onBackground = Ink,
    onSurface = Ink,
)

private val DarkColors = darkColorScheme(
    primary = ColorTokens.DarkPrimary,
    secondary = Copper,
    tertiary = Coral,
    background = ColorTokens.DarkBackground,
    surface = ColorTokens.DarkSurface,
    surfaceVariant = ColorTokens.DarkSurfaceVariant,
)

@Composable
fun KillerSudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}

private object ColorTokens {
    val DarkPrimary = androidx.compose.ui.graphics.Color(0xFF8FC7BA)
    val DarkBackground = androidx.compose.ui.graphics.Color(0xFF101414)
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF171C1D)
    val DarkSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF243031)
}
