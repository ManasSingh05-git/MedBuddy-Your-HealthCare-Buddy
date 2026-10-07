package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.ui.components.AnimatedProgressRing
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*

@Composable
fun HomeScreen(onAddMedicine: () -> Unit, onLogout: () -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    DisposableEffect(userId) {
        if (userId == null) {
            loading = false
            onDispose { }
        } else {
            val reference = FirebaseDatabase.getInstance().reference.child("medicines").child(userId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    medicines = snapshot.children.mapNotNull { child: DataSnapshot -> child.getValue(Medicine::class.java)?.copy(id = child.key ?: "") }
                    loading = false
                }
                override fun onCancelled(error: DatabaseError) { loading = false }
            }
            reference.addValueEventListener(listener)
            onDispose { reference.removeEventListener(listener) }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO SOS Action */ },
                containerColor = SOSRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = "SOS", modifier = Modifier.size(28.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface.copy(alpha = 0.9f),
                contentColor = CyanPrimary,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = CyanPrimary, unselectedIconColor = TextSecondary, indicatorColor = DarkSurface)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = CyanPrimary, unselectedIconColor = TextSecondary)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = CyanPrimary, unselectedIconColor = TextSecondary)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Good morning,", color = TextSecondary, fontSize = 16.sp)
                    Text("Your Health Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                TextButton(onClick = { FirebaseAuth.getInstance().signOut(); onLogout() }) { 
                    Text("Log out", color = SOSRed) 
                }
            }

            // Health Score Ring
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AnimatedProgressRing(
                    progress = 0.85f,
                    modifier = Modifier.size(160.dp),
                    colors = listOf(CyanPrimary, VioletGradient),
                    strokeWidth = 24f
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("85", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Health Score", fontSize = 14.sp, color = TextSecondary)
                }
            }

            // Metrics Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard("Steps", "8,432", "Goal: 10k", Modifier.weight(1f))
                    MetricCard("Distance", "5.2 km", "Daily", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard("Calories", "420 kcal", "Active", Modifier.weight(1f))
                    MetricCard("Water", "1.5 L", "Goal: 2.5 L", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard("Sleep", "7h 20m", "Restful", Modifier.weight(1f))
                    MetricCard("Medicines", if (loading) "-" else "${medicines.size} scheduled", "Today", Modifier.weight(1f))
                }
            }

            // Next Reminder
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your next reminder", color = CyanPrimary, fontWeight = FontWeight.Medium)
                    val nextMed = medicines.firstOrNull()
                    if (nextMed != null) {
                        Text(nextMed.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${nextMed.dosage} at ${nextMed.time}", color = TextSecondary)
                    } else {
                        Text(if (loading) "Loading..." else "No medicine added", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(if (loading) "Please wait" else "Add your first medicine below", color = TextSecondary)
                    }
                }
            }

            // Actions
            Text("Quick actions", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard("Add Medicine", Modifier.weight(1f), onClick = onAddMedicine)
                ActionCard("Health Log", Modifier.weight(1f), onClick = {})
            }

            // Medicines List
            Text("My medicines", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            when {
                loading -> Text("Loading medicines...", color = TextSecondary)
                medicines.isEmpty() -> Text("No medicines yet. Tap Add Medicine to begin.", color = TextSecondary)
                else -> medicines.forEach { MedicineCard(it) }
            }
            
            Spacer(modifier = Modifier.height(40.dp)) // Space for FAB
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, subtitle: String, modifier: Modifier) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = TextSecondary, fontSize = 14.sp)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, color = CyanPrimary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ActionCard(title: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(BlueGradient.copy(alpha=0.2f), VioletGradient.copy(alpha=0.2f))))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(title, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
    }
}

@Composable
private fun MedicineCard(medicine: Medicine) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(medicine.name, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextPrimary)
            Text("${medicine.dosage} - ${medicine.time}", color = TextSecondary)
        }
    }
}
