package com.example.rttracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

private val DarkColorScheme = darkColorScheme(
    primary = ObsidianPurple,
    secondary = ObsidianAccent,
    tertiary = ObsidianTertiary,
    background = ObsidianBackground,
    surface = ObsidianSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = ObsidianText,
    onSurface = ObsidianText
)

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = OpenDyslexic),
    displayMedium = TextStyle(fontFamily = OpenDyslexic),
    displaySmall = TextStyle(fontFamily = OpenDyslexic),
    headlineLarge = TextStyle(fontFamily = OpenDyslexic),
    headlineMedium = TextStyle(fontFamily = OpenDyslexic),
    headlineSmall = TextStyle(fontFamily = OpenDyslexic),
    titleLarge = TextStyle(fontFamily = OpenDyslexic),
    titleMedium = TextStyle(fontFamily = OpenDyslexic),
    titleSmall = TextStyle(fontFamily = OpenDyslexic),
    bodyLarge = TextStyle(fontFamily = OpenDyslexic),
    bodyMedium = TextStyle(fontFamily = OpenDyslexic),
    bodySmall = TextStyle(fontFamily = OpenDyslexic),
    labelLarge = TextStyle(fontFamily = OpenDyslexic),
    labelMedium = TextStyle(fontFamily = OpenDyslexic),
    labelSmall = TextStyle(fontFamily = OpenDyslexic)
)

@Composable
fun RtTrackerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
