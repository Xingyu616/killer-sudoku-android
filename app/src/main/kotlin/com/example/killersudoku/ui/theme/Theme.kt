package com.example.killersudoku.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.killersudoku.domain.model.BoardTheme

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

private val EyeCareColors = lightColorScheme(
    primary = Color(0xFF4F7D42),
    secondary = Color(0xFF8A6B2D),
    tertiary = Color(0xFFB45F45),
    background = Color(0xFFF4FAEA),
    surface = Color(0xFFF4FAEA),
    surfaceVariant = Color(0xFFE4EFD8),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF171F15),
    onBackground = Color(0xFF172018),
    onSurface = Color(0xFF172018),
)

private val NightColors = darkColorScheme(
    primary = Color(0xFF8DB6FF),
    secondary = Color(0xFFFFC46B),
    tertiary = Color(0xFFFF8A8A),
    background = Color(0xFF111722),
    surface = Color(0xFF1D2430),
    surfaceVariant = Color(0xFF2A3445),
    onPrimary = Color(0xFF0B1020),
    onSecondary = Color(0xFF17120A),
    onBackground = Color(0xFFF6F8FB),
    onSurface = Color(0xFFF6F8FB),
)

private val PaperColors = lightColorScheme(
    primary = Color(0xFF6F4E2E),
    secondary = Color(0xFF245C7C),
    tertiary = Color(0xFFB4573A),
    background = Color(0xFFFAF6EC),
    surface = Color(0xFFFAF6EC),
    surfaceVariant = Color(0xFFEDE1CD),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF241B14),
    onSurface = Color(0xFF241B14),
)

@Composable
fun KillerSudokuTheme(
    boardTheme: BoardTheme = BoardTheme.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = when (boardTheme) {
            BoardTheme.DEFAULT -> if (darkTheme) DarkColors else LightColors
            BoardTheme.EYE_CARE -> EyeCareColors
            BoardTheme.NIGHT -> NightColors
            BoardTheme.PAPER -> PaperColors
        },
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
