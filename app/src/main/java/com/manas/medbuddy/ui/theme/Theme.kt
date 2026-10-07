package com.manas.medbuddy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MedBuddyColors = lightColorScheme(
    primary = Color(0xFF147D72),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F4ED),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF48665F),
    background = Color(0xFFF9FBFA),
    surface = Color.White,
    onSurface = Color(0xFF191C1B)
)

@Composable
fun MedBuddyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MedBuddyColors, content = content)
}
