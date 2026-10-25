package com.manas.medbuddy.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.data.model.Hospital
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.ui.theme.BlueGradient
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.DarkSurface
import com.manas.medbuddy.ui.theme.DarkSurfaceLight
import com.manas.medbuddy.ui.theme.EmeraldPrimary
import com.manas.medbuddy.ui.theme.GlassBackgroundDark
import com.manas.medbuddy.ui.theme.GlassBackgroundLight
import com.manas.medbuddy.ui.theme.GlassBorderDark
import com.manas.medbuddy.ui.theme.GlassBorderLight
import com.manas.medbuddy.ui.theme.GoldYellow
import com.manas.medbuddy.ui.theme.LocalThemeMode
import com.manas.medbuddy.ui.theme.MagentaAccent
import com.manas.medbuddy.ui.theme.MintAccent
import com.manas.medbuddy.ui.theme.SOSRed
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary
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
    shape: RoundedCornerShape = RoundedCornerShape(22.dp),
    content: @Composable () -> Unit
) {
    val (glassBg, glassBorder) = getGlassColors()
    
    Surface(
        modifier = modifier,
        shape = shape,
        color = glassBg,
        border = BorderStroke(1.dp, glassBorder)
    ) {
        content()
    }
}

@Composable
fun MedBuddyMascot(
    modifier: Modifier = Modifier,
    size: Dp = 90.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "robotFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "robotY"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(1f + (floatOffset / 100f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2, h / 2 + floatOffset)

            // Outer cyan aura glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x8800E5FF),
                        Color(0x3300E676),
                        Color.Transparent
                    ),
                    center = center,
                    radius = w * 0.65f
                )
            )

            // Robot Head Shell
            val headRadius = w * 0.32f
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
                    start = Offset(center.x - headRadius, center.y - headRadius),
                    end = Offset(center.x + headRadius, center.y + headRadius)
                ),
                radius = headRadius,
                center = center
            )
            drawCircle(
                color = CyanPrimary,
                radius = headRadius,
                center = center,
                style = Stroke(width = 3f)
            )

            // Robot Antennas / Ears
            drawCircle(
                color = EmeraldPrimary,
                radius = w * 0.07f,
                center = Offset(center.x - headRadius * 0.9f, center.y - headRadius * 0.4f)
            )
            drawCircle(
                color = CyanPrimary,
                radius = w * 0.07f,
                center = Offset(center.x + headRadius * 0.9f, center.y - headRadius * 0.4f)
            )

            // Face Screen (Dark Inner Oval)
            val screenWidth = headRadius * 1.3f
            val screenHeight = headRadius * 0.85f
            drawRoundRect(
                color = Color(0xFF050B14),
                topLeft = Offset(center.x - screenWidth / 2, center.y - screenHeight / 2),
                size = Size(screenWidth, screenHeight),
                cornerRadius = CornerRadius(screenWidth * 0.3f, screenWidth * 0.3f)
            )

            // Glowing Eyes (Cyan Arcs / Happy Eyes)
            val eyeWidth = screenWidth * 0.22f
            val eyeOffsetY = center.y - screenHeight * 0.1f

            // Left Eye
            drawArc(
                color = CyanPrimary,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - screenWidth * 0.32f, eyeOffsetY),
                size = Size(eyeWidth, eyeWidth),
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )

            // Right Eye
            drawArc(
                color = CyanPrimary,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x + screenWidth * 0.10f, eyeOffsetY),
                size = Size(eyeWidth, eyeWidth),
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )

            // Smile Mouth
            drawArc(
                color = EmeraldPrimary,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - screenWidth * 0.15f, center.y + screenHeight * 0.1f),
                size = Size(screenWidth * 0.3f, screenHeight * 0.25f),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
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
                cubicTo(w * 0.12f, h * 0.58f, 0f, h * 0.35f, w * 0.22f, h * 0.15f)
                cubicTo(w * 0.38f, h * 0.02f, w * 0.5f, h * 0.22f, w * 0.5f, h * 0.28f)
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

            // Right pill half overlay
            val rightPillClip = Path().apply {
                moveTo(w * 0.25f, 0f)
                lineTo(w * 1.2f, 0f)
                lineTo(w * 1.2f, h * 1.2f)
                lineTo(w * 0.52f, h * 0.85f)
                close()
            }

            drawPath(
                path = Path.combine(
                    PathOperation.Intersect,
                    heartPath,
                    rightPillClip
                ),
                brush = Brush.linearGradient(
                    colors = listOf(EmeraldPrimary, CyanPrimary),
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
                            EmeraldPrimary,
                            CyanPrimary,
                            BlueGradient
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

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = DarkSurface.copy(alpha = 0.95f),
        contentColor = CyanPrimary,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(if (selectedTab == "home") Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            selected = selectedTab == "home",
            onClick = { onTabSelected("home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                selectedTextColor = EmeraldPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Widgets, contentDescription = "Tools") },
            label = { Text("Tools", fontSize = 11.sp) },
            selected = selectedTab == "tools",
            onClick = { onTabSelected("tools") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanPrimary,
                selectedTextColor = CyanPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(if (selectedTab == "assistant") Icons.Default.SmartToy else Icons.Outlined.SmartToy, contentDescription = "Assistant") },
            label = { Text("Assistant", fontSize = 11.sp) },
            selected = selectedTab == "assistant",
            onClick = { onTabSelected("assistant") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanPrimary,
                selectedTextColor = CyanPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Analytics, contentDescription = "Insights") },
            label = { Text("Insights", fontSize = 11.sp) },
            selected = selectedTab == "insights",
            onClick = { onTabSelected("insights") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanPrimary,
                selectedTextColor = CyanPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(if (selectedTab == "profile") Icons.Default.Person else Icons.Outlined.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 11.sp) },
            selected = selectedTab == "profile",
            onClick = { onTabSelected("profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                selectedTextColor = EmeraldPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(text = actionText, color = CyanPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
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
            
            drawCircle(
                color = glassBg,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            
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
fun SOSButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(76.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SOSRed,
                        Color(0xFFB71C1C)
                    )
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Warning, contentDescription = "SOS Emergency", tint = Color.White, modifier = Modifier.size(30.dp))
            Text("SOS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        }
    }
}
