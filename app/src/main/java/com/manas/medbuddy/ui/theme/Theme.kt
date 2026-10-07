package com.manas.medbuddy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MedBuddyDarkColors = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.Black,
    primaryContainer = DarkSurface,
    onPrimaryContainer = CyanPrimary,
    secondary = VioletGradient,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MedBuddyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MedBuddyDarkColors,
        content = content
    )
}

