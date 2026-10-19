package com.manas.medbuddy.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.ui.theme.BlueGradient
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.GlassBackgroundDark
import com.manas.medbuddy.ui.theme.GlassBackgroundLight
import com.manas.medbuddy.ui.theme.GlassBorderDark
import com.manas.medbuddy.ui.theme.GlassBorderLight
import com.manas.medbuddy.ui.theme.LocalThemeMode
import com.manas.medbuddy.ui.theme.ThemeMode

@Composable
fun getGlassColors(): Pair<Color, Color> {
    val themeMode = LocalThemeMode.current.value
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemDark
    }
    return if (isDark) {
        Pair(GlassBackgroundDark, GlassBorderDark)
    } else {
        Pair(GlassBackgroundLight, GlassBorderLight)
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val (glassBg, glassBorder) = getGlassColors()
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = glassBg,
        border = BorderStroke(1.dp, glassBorder)
    ) {
        content()
    }
}

@Composable
fun AnimatedProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(CyanPrimary, BlueGradient),
    strokeWidth: Float = 20f
) {
    val (glassBg, _) = getGlassColors()
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) progress else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - strokeWidth / 2
            
            // Background track
            drawCircle(
                color = glassBg,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            
            // Foreground animated progress
            drawArc(
                brush = Brush.sweepGradient(colors),
                startAngle = -90f,
                sweepAngle = 360 * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun HeartPillLogo(
    modifier: Modifier = Modifier,
    size: Dp = 90.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2, h / 2)

            // Radial ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x881DE9B6),
                        Color(0x4400E5FF),
                        Color.Transparent
                    ),
                    center = center,
                    radius = w * 0.75f
                )
            )

            // Heart shape split diagonally into two pill-halves
            val heartPath = Path().apply {
                moveTo(w * 0.5f, h * 0.82f)
                // Left curve
                cubicTo(w * 0.12f, h * 0.58f, 0f, h * 0.35f, w * 0.22f, h * 0.15f)
                cubicTo(w * 0.38f, h * 0.02f, w * 0.5f, h * 0.22f, w * 0.5f, h * 0.28f)
                // Right curve
                cubicTo(w * 0.5f, h * 0.22f, w * 0.62f, h * 0.02f, w * 0.78f, h * 0.15f)
                cubicTo(w * 1.0f, h * 0.35f, w * 0.88f, h * 0.58f, w * 0.5f, h * 0.82f)
                close()
            }

            // Left half clip (white to light cyan gradient)
            drawPath(
                path = heartPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFFFFFFFF)),
                    start = Offset(0f, h),
                    end = Offset(w, 0f)
                )
            )

            // Right pill half overlay with diagonal cut
            val rightPillClip = Path().apply {
                moveTo(w * 0.25f, 0f)
                lineTo(w * 1.2f, 0f)
                lineTo(w * 1.2f, h * 1.2f)
                lineTo(w * 0.52f, h * 0.85f)
                close()
            }

            drawPath(
                path = Path.combine(
                    androidx.compose.ui.graphics.PathOperation.Intersect,
                    heartPath,
                    rightPillClip
                ),
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00E676), Color(0xFF00E5FF)),
                    start = Offset(w * 0.3f, 0f),
                    end = Offset(w, h)
                )
            )
        }
    }
}

@Composable
fun GradientPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(CircleShape)
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF00E676), // Vibrant Mint Green
                            Color(0xFF00E5FF), // Bright Cyan
                            Color(0xFF2979FF)  // Electric Blue
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF374151), Color(0xFF4B5563))
                    )
                }
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

