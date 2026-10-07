package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import java.util.Calendar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun HomeScreen(onNavigateToMedicines: () -> Unit, onLogout: () -> Unit) {
    val userId = try {
        FirebaseAuth.getInstance().currentUser?.uid
    } catch (e: Exception) {
        null // Firebase is not initialized, fallback to null for UI testing
    }
    
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var waterIntake by remember { mutableStateOf(0) }

    val themeMode = LocalThemeMode.current

    // Time-based greeting
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    DisposableEffect(userId) {
        if (userId == null) {
            loading = false
            onDispose { }
        } else {
            try {
                val reference = FirebaseDatabase.getInstance().reference.child("medicines").child(userId)
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        medicines = snapshot.children.mapNotNull { child -> child.getValue(Medicine::class.java)?.copy(id = child.key ?: "") }
                        loading = false
                    }
                    override fun onCancelled(error: DatabaseError) { loading = false }
                }
                reference.addValueEventListener(listener)
                onDispose { reference.removeEventListener(listener) }
            } catch (e: Exception) {
                loading = false
                onDispose { }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO SOS Action */ },
                containerColor = MaterialTheme.colorScheme.error,
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
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surface
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    Text("$greeting,", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                    Text("User", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                
                // Theme Toggle
                IconButton(onClick = {
                    themeMode.value = if (themeMode.value == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT
                }) {
                    Icon(
                        imageVector = if (themeMode.value == ThemeMode.LIGHT) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                TextButton(onClick = { 
                    try { FirebaseAuth.getInstance().signOut() } catch (e: Exception) {}
                    onLogout() 
                }) { 
                    Text("Log out", color = MaterialTheme.colorScheme.error) 
                }
            }

            // Medicines Section (Clickable)
            Surface(
                onClick = onNavigateToMedicines,
                color = Color.Transparent,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Your Medicines", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            Text("View All >", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        
                        val nextMed = medicines.firstOrNull()
                        if (nextMed != null) {
                            Text("Next up: ${nextMed.name}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("${nextMed.dosage} at ${nextMed.time}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(if (loading) "Loading..." else "No medicine scheduled", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(if (loading) "Please wait" else "Tap here to add medicines", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Health Section
            Text("Health Overview", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard("Steps", "8,432", "Goal: 10k", Modifier.weight(1f))
                    MetricCard("Distance", "5.2 km", "Daily", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard("Calories", "420 kcal", "Active", Modifier.weight(1f))
                    // Water Intake with Manual Adder
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Water", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            Text("$waterIntake / 8", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Glasses", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                Row {
                                    IconButton(
                                        onClick = { if (waterIntake > 0) waterIntake-- },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { waterIntake++ },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard("Sleep", "7h 20m", "Restful", Modifier.weight(1f))
                    MetricCard("Heart Rate", "72 bpm", "Resting", Modifier.weight(1f))
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp)) // Space for FAB
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, subtitle: String, modifier: Modifier) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
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
            Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun MedicineCard(medicine: Medicine) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(medicine.name, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("${medicine.dosage} - ${medicine.time}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
