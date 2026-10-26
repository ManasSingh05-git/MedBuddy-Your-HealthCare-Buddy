package com.manas.medbuddy.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.manas.medbuddy.R
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
        modifier = Modifier.fillMaxSize()
    ) {
        // High-Res Plant & Starry Sky Background Image from mad project ss/backgrounf.png
        Image(
            painter = painterResource(id = R.drawable.backgrounf),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // High-Res MedBuddy Logo Artwork Image from mad project ss/logo.png
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "MedBuddy Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(280.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

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
