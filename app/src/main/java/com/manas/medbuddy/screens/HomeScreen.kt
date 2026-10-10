package com.manas.medbuddy.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.data.MedicineRepository
import com.manas.medbuddy.model.DoseLog
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.reminder.ReminderScheduler
import com.manas.medbuddy.ui.components.AnimatedProgressRing
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onAddMedicine: () -> Unit,
    onOpenMedicineList: () -> Unit,
    onEditMedicine: (String) -> Unit,
    onLogout: () -> Unit,
    repository: MedicineRepository = remember { MedicineRepository() }
) {
    val context = LocalContext.current
    val scheduler = remember { ReminderScheduler(context) }

    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var doseLogs by remember { mutableStateOf<List<DoseLog>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    DisposableEffect(Unit) {
        val medListener: ValueEventListener = repository.listenToMedicines(
            onUpdate = { list ->
                medicines = list
                loading = false
            },
            onError = { loading = false }
        )
        val logListener: ValueEventListener = repository.listenToDoseLogs(
            onUpdate = { logs -> doseLogs = logs }
        )

        onDispose {
            repository.removeMedicinesListener(medListener)
            repository.removeDoseLogsListener(logListener)
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* SOS Action preserved */ },
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
                    MetricCard("Medicines", if (loading) "-" else "${medicines.size} active", "Today", Modifier.weight(1f))
                }
            }

            // Next Reminder
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your next reminder", color = CyanPrimary, fontWeight = FontWeight.Medium)
                    val nextMed = medicines.firstOrNull()
                    if (nextMed != null) {
                        val displayTime = if (nextMed.reminderTimes.isNotEmpty()) {
                            nextMed.reminderTimes.first().time
                        } else nextMed.time.ifBlank { "08:00 AM" }
                        Text(nextMed.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${nextMed.dosage} at $displayTime", color = TextSecondary)
                    } else {
                        Text(if (loading) "Loading..." else "No medicine added", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(if (loading) "Please wait" else "Add your first medicine below", color = TextSecondary)
                    }
                }
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quick actions", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                TextButton(onClick = onOpenMedicineList) {
                    Text("View All (${medicines.size})", color = CyanPrimary)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard("Add Medicine", Modifier.weight(1f), onClick = onAddMedicine)
                ActionCard("Schedules & History", Modifier.weight(1f), onClick = onOpenMedicineList)
            }

            // Medicines List
            Text("My medicines", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            when {
                loading -> Text("Loading medicines...", color = TextSecondary)
                medicines.isEmpty() -> Text("No medicines yet. Tap Add Medicine to begin.", color = TextSecondary)
                else -> medicines.forEach { medicine ->
                    MedicineCard(
                        medicine = medicine,
                        doseLogs = doseLogs,
                        todayStr = todayStr,
                        onEdit = { onEditMedicine(medicine.id) },
                        onDelete = {
                            scheduler.cancelMedicineReminders(medicine)
                            repository.deleteMedicine(medicine.id) { _, _ -> }
                        },
                        onLogStatus = { time, status ->
                            repository.logDoseStatus(medicine, time, status, todayStr)
                        }
                    )
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
private fun MedicineCard(
    medicine: Medicine,
    doseLogs: List<DoseLog>,
    todayStr: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLogStatus: (String, String) -> Unit
) {
    val primaryTime = if (medicine.reminderTimes.isNotEmpty()) {
        medicine.reminderTimes.first().time
    } else medicine.time.ifBlank { "08:00 AM" }

    val logId = "${medicine.id}_${todayStr}_${primaryTime.replace(" ", "_")}"
    val currentLog = doseLogs.find { it.id == logId || (it.medicineId == medicine.id && it.date == todayStr) }
    val currentStatus = currentLog?.status ?: "PENDING"

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = CyanPrimary)
                    Text(medicine.name, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextPrimary)
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyanPrimary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SOSRed, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Text("${medicine.dosage} • Time: $primaryTime", color = TextSecondary, fontSize = 14.sp)

            if (medicine.instructions.isNotBlank()) {
                Text(medicine.instructions, color = TextSecondary, fontSize = 12.sp)
            }

            if (medicine.reminderTimes.size > 1) {
                Text("All schedules: ${medicine.reminderTimes.joinToString(", ") { "${it.label} (${it.time})" }}", color = CyanPrimary, fontSize = 11.sp)
            }

            // Quick Taken / Skipped tracking row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onLogStatus(primaryTime, "TAKEN") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentStatus == "TAKEN") Color(0xFF2E7D32) else DarkSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (currentStatus == "TAKEN") "Taken ✓" else "Take", fontSize = 12.sp)
                }

                Button(
                    onClick = { onLogStatus(primaryTime, "SKIPPED") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentStatus == "SKIPPED") Color(0xFFC62828) else DarkSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = SOSRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (currentStatus == "SKIPPED") "Skipped ✗" else "Skip", fontSize = 12.sp)
                }
            }
        }
    }
}
