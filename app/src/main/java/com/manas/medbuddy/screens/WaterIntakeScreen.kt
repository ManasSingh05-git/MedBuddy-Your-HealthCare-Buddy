package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.ui.components.AnimatedProgressRing
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.GradientPillButton
import com.manas.medbuddy.ui.theme.BlueGradient
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.DarkBackground
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.manas.medbuddy.ui.theme.DarkSurface
import com.manas.medbuddy.tracker.WaterRepository
import com.manas.medbuddy.ui.theme.EmeraldPrimary
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterIntakeScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val waterRepo = remember { WaterRepository.getInstance(context) }
    val waterData by waterRepo.waterDataFlow.collectAsState()

    val currentWaterLiters = waterData.currentLiters
    val goalWaterLiters = waterData.goalLiters
    val progress = waterData.getProgressFraction()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Water Intake", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Circular Hydration Progress Ring
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedProgressRing(
                    progress = progress,
                    modifier = Modifier.size(220.dp),
                    colors = listOf(CyanPrimary, BlueGradient),
                    strokeWidth = 22f
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("💧", fontSize = 36.sp)
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f L", currentWaterLiters),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "of ${goalWaterLiters.toInt()} L • ${(progress * 100).toInt()}%",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            // Main Action Button: + Add 250 ml
            GradientPillButton(
                text = "+ Add 250 ml",
                onClick = {
                    waterRepo.addWater(0.25f)
                }
            )

            // Preset Quick Add Chips (250 ml, 500 ml, 750 ml, 1 L)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PresetWaterChip("250 ml", Modifier.weight(1f)) { waterRepo.addWater(0.25f) }
                PresetWaterChip("500 ml", Modifier.weight(1f)) { waterRepo.addWater(0.50f) }
                PresetWaterChip("750 ml", Modifier.weight(1f)) { waterRepo.addWater(0.75f) }
                PresetWaterChip("1 L", Modifier.weight(1f)) { waterRepo.addWater(1.00f) }
            }

            // Today's Progress Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Today's Progress", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = CyanPrimary,
                        trackColor = DarkSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format("%.1f L / %.1f L", currentWaterLiters, goalWaterLiters),
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (progress >= 1f) "Goal Reached! 🎉" else "Stay Hydrated",
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetWaterChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}
