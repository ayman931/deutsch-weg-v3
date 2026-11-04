package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DeutschWegColorScheme = lightColorScheme(
    primary = PrimaryEspresso,
    onPrimary = OnPrimaryWarm,
    primaryContainer = PrimaryEspresso,
    onPrimaryContainer = OnPrimaryWarm,
    secondary = AccentAmber,
    onSecondary = Color.White,
    secondaryContainer = SurfaceContainerHigh,
    onSecondaryContainer = TextPrimary,
    tertiary = FeedbackCorrect,
    onTertiary = Color.White,
    tertiaryContainer = FeedbackCorrectSubtle,
    onTertiaryContainer = FeedbackCorrect,
    error = FeedbackIncorrect,
    onError = Color.White,
    errorContainer = FeedbackIncorrectSubtle,
    onErrorContainer = FeedbackIncorrect,
    background = WarmBackground,
    onBackground = TextPrimary,
    surface = WarmBackground,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = TextSecondary,
    outline = BorderActive,
    outlineVariant = BorderSubtle,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceBright = SurfaceBright
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DeutschWegColorScheme,
        typography = Typography,
        content = content
    )
}
