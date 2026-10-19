package com.manas.medbuddy.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.manas.medbuddy.ui.components.GradientPillButton
import com.manas.medbuddy.ui.components.HeartPillLogo
import androidx.compose.runtime.LaunchedEffect

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    onAlreadyAuthenticated: () -> Unit = onFinished
) {
    LaunchedEffect(Unit) {
        val currentUser = try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Exception) {
            null
        }
        if (currentUser != null) {
            onAlreadyAuthenticated()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0D2524),
                        Color(0xFF071219),
                        Color(0xFF03070C)
                    )
                )
            )
            .padding(horizontal = 28.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo & Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeartPillLogo(size = 110.dp)

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Med",
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Buddy",
                        color = Color(0xFF1DE9B6),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Your Personal\nHealth Companion",
                    color = Color(0xFF80DEEA),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Get Started Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Gradient Get Started Button
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GradientPillButton(
                        text = "Get Started →",
                        onClick = onFinished
                    )
                }

                Text(
                    text = "Manage  •  Track  •  Stay Healthy",
                    color = Color(0xFF4FD1C5),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C3E50))
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C3E50))
                    )
                }
            }
        }
    }
}
