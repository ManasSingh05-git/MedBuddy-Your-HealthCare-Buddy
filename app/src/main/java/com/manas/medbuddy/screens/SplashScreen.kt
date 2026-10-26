package com.manas.medbuddy.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.manas.medbuddy.ui.components.HeartPillLogo
import com.manas.medbuddy.ui.theme.TextPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    onAlreadyAuthenticated: () -> Unit = onFinished
) {
    LaunchedEffect(Unit) {
        val currentUser = withContext(Dispatchers.IO) {
            try {
                FirebaseAuth.getInstance().currentUser
            } catch (_: Exception) {
                null
            }
        }
        if (currentUser != null) {
            onAlreadyAuthenticated()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF02140D),
                        Color(0xFF042217),
                        Color(0xFF02120C)
                    )
                )
            )
    ) {
        // Decorative Dark Leaf Accents in Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top-left dark leaf curve
            val leaf1 = Path().apply {
                moveTo(0f, 0f)
                cubicTo(w * 0.4f, h * 0.05f, w * 0.3f, h * 0.25f, 0f, h * 0.28f)
                close()
            }
            drawPath(leaf1, color = Color(0x2200E676))

            // Top-right dark leaf curve
            val leaf2 = Path().apply {
                moveTo(w, 0f)
                cubicTo(w * 0.6f, h * 0.08f, w * 0.7f, h * 0.22f, w, h * 0.25f)
                close()
            }
            drawPath(leaf2, color = Color(0x111DE9B6))

            // Bottom-right dark leaf curve
            val leaf3 = Path().apply {
                moveTo(w, h)
                cubicTo(w * 0.5f, h * 0.85f, w * 0.6f, h * 0.75f, w, h * 0.7f)
                close()
            }
            drawPath(leaf3, color = Color(0x2200E676))

            // Bottom-left dark leaf curve
            val leaf4 = Path().apply {
                moveTo(0f, h)
                cubicTo(w * 0.35f, h * 0.9f, w * 0.25f, h * 0.82f, 0f, h * 0.78f)
                close()
            }
            drawPath(leaf4, color = Color(0x151DE9B6))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Center Logo & Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeartPillLogo(size = 145.dp)

                Text(
                    text = "MedBuddy",
                    color = TextPrimary,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Your Healthcare Buddy",
                    color = Color(0xFFC8E6C9),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Middle Dark Glass Card Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .border(1.dp, Color(0x3300E676), RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                color = Color(0xCC052016)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Progress/Indicator bar
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF80E5A3),
                                        Color(0xFF00E676),
                                        Color(0xFF1DE9B6)
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Better Health\nBrighter Tomorrow",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Neon Green Get Started Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    onClick = onFinished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(CircleShape),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00E676),
                                        Color(0xFF1DE9B6)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Get Started →",
                            color = Color(0xFF02160E),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Bottom Home Bar Indicator
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0x66FFFFFF))
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
