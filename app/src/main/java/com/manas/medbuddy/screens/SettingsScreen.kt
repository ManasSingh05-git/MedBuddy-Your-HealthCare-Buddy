package com.manas.medbuddy.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.manas.medbuddy.data.repository.EmergencyContactRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToSosSettings: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { EmergencyContactRepository.getInstance(context) }
    val contacts = repository.getContacts()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "User"

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Account Info Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = CyanPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Signed in as", color = TextSecondary, fontSize = 12.sp)
                        Text(userEmail, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Text("Safety & Emergency", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

            // Emergency Contacts Option
            SettingsOptionCard(
                title = "Emergency Contacts & SOS",
                subtitle = "${contacts.size} contact(s) saved • Configured for Single & Triple Tap SOS",
                icon = Icons.Default.Warning,
                iconTint = SOSRed,
                onClick = onNavigateToSosSettings
            )

            Text("Preferences & Account", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

            // Logout Option
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Surface(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    },
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", tint = SOSRed)
                        Text("Log Out", color = SOSRed, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = onClick,
            color = androidx.compose.ui.graphics.Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(subtitle, color = TextSecondary, fontSize = 12.sp)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
            }
        }
    }
}
