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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.DarkBackground
import com.manas.medbuddy.ui.theme.DarkSurface
import com.manas.medbuddy.ui.theme.EmeraldPrimary
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiHealthInsightsScreen(
    onBack: () -> Unit = {}
) {
    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("✨ AI Health Insights", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Health Summary Card with Robot Mascot
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Health Summary", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "You are doing great! Keep your steps and water intake consistent.",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    // Robot Mascot Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(EmeraldPrimary.copy(alpha = 0.3f), CyanPrimary.copy(alpha = 0.3f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 32.sp)
                    }
                }
            }

            // Today's Insights Section
            SectionHeader(title = "Today's Insights")

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightCard(
                    icon = "👟",
                    iconBg = Color(0xFF00E676).copy(alpha = 0.2f),
                    title = "Step Goal",
                    description = "You are 54% close to your daily goal. Keep going!"
                )

                InsightCard(
                    icon = "💧",
                    iconBg = Color(0xFF00E5FF).copy(alpha = 0.2f),
                    title = "Water Intake",
                    description = "Try to drink 1.2 L more water today."
                )

                InsightCard(
                    icon = "💊",
                    iconBg = Color(0xFF2979FF).copy(alpha = 0.2f),
                    title = "Medicine Adherence",
                    description = "100% on time this week! 👏"
                )

                InsightCard(
                    icon = "💡",
                    iconBg = Color(0xFFFFD700).copy(alpha = 0.2f),
                    title = "Healthy Tip",
                    description = "A short 10-minute walk after meals can help in better digestion."
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InsightCard(
    icon: String,
    iconBg: Color,
    title: String,
    description: String
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text(description, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}
