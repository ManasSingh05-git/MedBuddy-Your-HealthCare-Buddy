package com.manas.medbuddy.screens

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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.data.repository.EmergencyContactRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SOSButton
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.DarkBackground
import com.manas.medbuddy.ui.theme.DarkSurface
import com.manas.medbuddy.ui.theme.EmeraldPrimary
import com.manas.medbuddy.ui.theme.SOSRed
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary
import com.manas.medbuddy.util.EmergencyCallHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosScreen(
    onBack: () -> Unit = {},
    onNavigateToSosSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val contactRepo = remember { EmergencyContactRepository.getInstance(context) }
    val contacts by contactRepo.contactsFlow.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("SOS Emergency", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Central SOS Orb
            SOSButton(
                onClick = {
                    val primary = contactRepo.getPrimaryContact()
                    if (primary != null) {
                        EmergencyCallHelper.initiateCall(context, primary.phoneNumber)
                    } else {
                        EmergencyCallHelper.initiateCall(context, "112")
                    }
                },
                modifier = Modifier.size(140.dp)
            )

            Text(
                text = "Tap to Call Emergency",
                color = TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            // 3 Action Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SosActionCard("Call\nFamily", Icons.Default.Call, SOSRed, Modifier.weight(1f)) {
                    val primary = contactRepo.getPrimaryContact()
                    if (primary != null) EmergencyCallHelper.initiateCall(context, primary.phoneNumber)
                }
                SosActionCard("Share\nLive Location", Icons.Default.LocationOn, CyanPrimary, Modifier.weight(1f)) { }
                SosActionCard("Alert\nContacts", Icons.Default.Notifications, EmeraldPrimary, Modifier.weight(1f)) { }
            }

            // Emergency Contacts Header
            SectionHeader(
                title = "Emergency Contacts",
                actionText = "Add",
                onActionClick = onNavigateToSosSettings
            )

            if (contacts.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SOSRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = SOSRed)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dad", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text("+91 98765 43210", color = TextSecondary, fontSize = 13.sp)
                        }
                        IconButton(onClick = { EmergencyCallHelper.initiateCall(context, "+91 98765 43210") }) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = EmeraldPrimary)
                        }
                    }
                }
            } else {
                contacts.forEach { contact ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SOSRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = SOSRed)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                Text("${contact.phoneNumber} • ${contact.relationship}", color = TextSecondary, fontSize = 13.sp)
                            }
                            IconButton(onClick = { EmergencyCallHelper.initiateCall(context, contact.phoneNumber) }) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = EmeraldPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SosActionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(modifier = modifier.clickable { onClick() }) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Text(title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}
