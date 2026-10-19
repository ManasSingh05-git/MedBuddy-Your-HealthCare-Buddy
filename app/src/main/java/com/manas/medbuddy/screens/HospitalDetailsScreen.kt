package com.manas.medbuddy.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.data.model.Hospital
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.GradientPillButton
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.DarkBackground
import com.manas.medbuddy.ui.theme.DarkSurface
import com.manas.medbuddy.ui.theme.EmeraldPrimary
import com.manas.medbuddy.ui.theme.GoldYellow
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary
import com.manas.medbuddy.util.EmergencyCallHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalDetailsScreen(
    hospital: Hospital = Hospital(
        id = "1",
        name = "Hiranandani Hospital",
        address = "Hiranandani Estate, Thane West, Maharashtra 400607",
        distanceInMeters = 1200f,
        phoneNumber = "+91 22 2586 6000",
        rating = 4.5,
        userRatingsTotal = 1200,
        latitude = 19.255,
        longitude = 72.981
    ),
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Hospital Details", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
            // Header Image Placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(22.dp),
                color = DarkSurface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0D2524),
                                    Color(0xFF13192B)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏥 Hospital Facility", fontSize = 48.sp)
                }
            }

            // Title & Info
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(hospital.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("Multi Speciality Hospital", color = TextSecondary, fontSize = 14.sp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldYellow, modifier = Modifier.size(18.dp))
                    Text("${hospital.rating ?: 4.5}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("(${hospital.userRatingsTotal ?: 1200} reviews)", color = TextSecondary, fontSize = 13.sp)
                }

                Text(
                    text = "${hospital.getFormattedDistance()} • Open 24x7",
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // 4 Circular Action Buttons (Call, Directions, Website, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                HospitalActionButton("Call", Icons.Default.Call) {
                    EmergencyCallHelper.initiateCall(context, hospital.phoneNumber)
                }
                HospitalActionButton("Directions", Icons.Default.Navigation) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${hospital.latitude},${hospital.longitude}"))
                    context.startActivity(intent)
                }
                HospitalActionButton("Website", Icons.Default.Language) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${hospital.name}"))
                    context.startActivity(intent)
                }
                HospitalActionButton("Share", Icons.Default.Share) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${hospital.name}\n${hospital.address}\nPhone: ${hospital.phoneNumber}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Hospital"))
                }
            }

            // Address Details Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(28.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Address", color = TextSecondary, fontSize = 12.sp)
                        Text(hospital.address, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Get Directions Button
            GradientPillButton(
                text = "📍 Get Directions",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${hospital.latitude},${hospital.longitude}"))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun HospitalActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = CyanPrimary, modifier = Modifier.size(24.dp))
        }
        Text(label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
