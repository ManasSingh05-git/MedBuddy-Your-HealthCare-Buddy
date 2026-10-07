package com.manas.medbuddy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

private val MedBuddyDarkColors = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.Black,
    primaryContainer = DarkSurface,
    onPrimaryContainer = CyanPrimary,
    secondary = VioletGradient,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    error = SOSRed
)

private val MedBuddyLightColors = lightColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = LightSurface,
    onPrimaryContainer = CyanPrimaryDark,
    secondary = VioletGradient,
    background = LightBackground,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    error = SOSRed
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

val LocalThemeMode = compositionLocalOf { mutableStateOf(ThemeMode.SYSTEM) }

@Composable
fun MedBuddyTheme(content: @Composable () -> Unit) {
    val themeModeState = remember { mutableStateOf(ThemeMode.SYSTEM) }
    
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeModeState.value) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemDark
    }

    val colorScheme = if (isDark) MedBuddyDarkColors else MedBuddyLightColors

    CompositionLocalProvider(LocalThemeMode provides themeModeState) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
