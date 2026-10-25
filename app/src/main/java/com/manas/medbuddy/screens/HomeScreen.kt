package com.manas.medbuddy.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.data.MedicineRepository
import com.manas.medbuddy.data.repository.EmergencyContactRepository
import com.manas.medbuddy.model.DoseLog
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.reminder.ReminderScheduler
import com.manas.medbuddy.ui.components.AnimatedProgressRing
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.util.EmergencyCallHelper
import com.manas.medbuddy.util.SosTapDetector
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddMedicine: () -> Unit = {},
    onOpenMedicineList: () -> Unit = {},
    onEditMedicine: (String) -> Unit = {},
    onNavigateToMedicines: () -> Unit = {},
    onNavigateToHealth: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSosSettings: () -> Unit = {},
    onNavigateToHospitalFinder: () -> Unit = {},
    onNavigateToHealthMap: () -> Unit = {},
    onNavigateToSleepTracker: () -> Unit = {},
    onNavigateToPrescriptionScanner: () -> Unit = {},
    onNavigateToReportExplainer: () -> Unit = {},
    onNavigateToMedicalVault: () -> Unit = {},
    onNavigateToAllergyRegistry: () -> Unit = {},
    onNavigateToVaccinationManager: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val scheduler = remember { ReminderScheduler(context) }
    val repository = remember { MedicineRepository(context) }

    val contactRepo = remember { EmergencyContactRepository.getInstance(context) }

    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var doseLogs by remember { mutableStateOf<List<DoseLog>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var showSingleTapDialog by remember { mutableStateOf(false) }
    var showNoContactDialog by remember { mutableStateOf(false) }

    val themeMode = LocalThemeMode.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val onSingleTap: () -> Unit = {
        val primary = contactRepo.getPrimaryContact()
        if (primary != null) {
            showSingleTapDialog = true
        } else {
            showNoContactDialog = true
        }
    }

    val onTripleTap: () -> Unit = {
        onNavigateToHospitalFinder()
    }

    val sosTapDetector = remember(coroutineScope) {
        SosTapDetector(
            scope = coroutineScope,
            timeWindowMs = 1200L,
            singleTapDelayMs = 350L,
            onSingleTap = onSingleTap,
            onTripleTap = onTripleTap
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val sosPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sosScale"
    )

    DisposableEffect(Unit) {
        val medListener: ValueEventListener? = try {
            repository.listenToMedicines(
                onUpdate = { list ->
                    medicines = list
                    loading = false
                },
                onError = { loading = false }
            )
        } catch (_: Exception) {
            loading = false
            null
        }

        val logListener: ValueEventListener? = try {
            repository.listenToDoseLogs(
                onUpdate = { logs -> doseLogs = logs }
            )
        } catch (_: Exception) {
            null
        }

        onDispose {
            if (medListener != null) repository.removeMedicinesListener(medListener)
            if (logListener != null) repository.removeDoseLogsListener(logListener)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkSurface
            ) {
                Spacer(Modifier.height(32.dp))
                Text(
                    "MedBuddy Dashboard",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), color = GlassBorder)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = CyanPrimary) },
                    label = { Text("Profile & Settings", color = TextPrimary) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToSettings()
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Vaccines, contentDescription = "Vaccinations", tint = CyanPrimary) },
                    label = { Text("Vaccination Records 💉", color = TextPrimary) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToVaccinationManager()
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Allergy Registry", tint = CyanPrimary) },
                    label = { Text("Allergy Registry ⚠️", color = TextPrimary) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToAllergyRegistry()
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "Medical Vault", tint = CyanPrimary) },
                    label = { Text("Medical Records Vault 🔒", color = TextPrimary) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToMedicalVault()
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.DocumentScanner, contentDescription = "AI Scanner", tint = CyanPrimary) },
                    label = { Text("AI Prescription Scanner", color = TextPrimary) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToPrescriptionScanner()
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Lab Report Explainer", tint = CyanPrimary) },
                    label = { Text("AI Medical Report Explainer", color = TextPrimary) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToReportExplainer()
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Medicines", tint = CyanPrimary) },
                    label = { Text("Medicine Management", color = TextPrimary) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onOpenMedicineList()
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Nightlight, contentDescription = "Sleep Tracker", tint = CyanPrimary) },
                    label = { Text("Sleep Tracker", color = TextPrimary) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToSleepTracker()
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.LocalHospital, contentDescription = "Nearby Hospitals", tint = CyanPrimary) },
                    label = { Text("Nearby Hospitals", color = TextPrimary) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToHospitalFinder()
                    }
                )
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = SOSRed) },
                    label = { Text("Log out", color = SOSRed) },
                    selected = false,
                    onClick = {
                        try { FirebaseAuth.getInstance().signOut() } catch (_: Exception) {}
                        onLogout()
                    }
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = DarkBackground,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("$greeting, MedBuddy", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = TextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = CyanPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            themeMode.value = if (themeMode.value == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT
                        }) {
                            Icon(
                                imageVector = if (themeMode.value == ThemeMode.LIGHT) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle Theme",
                                tint = CyanPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface.copy(alpha = 0.9f))
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { sosTapDetector.registerTap() },
                    containerColor = SOSRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .scale(sosPulseScale),
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "SOS Emergency", modifier = Modifier.size(26.dp))
                        Text("SOS", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 1.sp)
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
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
                    ActionCard("Vaccination Records 💉", Modifier.weight(1f), onClick = onNavigateToVaccinationManager)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard("Allergy Registry ⚠️", Modifier.weight(1f), onClick = onNavigateToAllergyRegistry)
                    ActionCard("Medical Vault 🔒", Modifier.weight(1f), onClick = onNavigateToMedicalVault)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard("AI Rx Scanner 📷", Modifier.weight(1f), onClick = onNavigateToPrescriptionScanner)
                    ActionCard("Report Explainer 📑", Modifier.weight(1f), onClick = onNavigateToReportExplainer)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard("Sleep Tracker 🌙", Modifier.weight(1f), onClick = onNavigateToSleepTracker)
                    ActionCard("Find Hospitals 🏥", Modifier.weight(1f), onClick = onNavigateToHospitalFinder)
                }

                // Medicines List
                Text("My medicines", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                when {
                    loading -> Text("Loading medicines...", color = TextSecondary)
                    medicines.isEmpty() -> Text("No medicines yet. Tap Add Medicine or AI Rx Scanner to begin.", color = TextSecondary)
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

    if (showSingleTapDialog) {
        val primary = contactRepo.getPrimaryContact()
        AlertDialog(
            onDismissRequest = { showSingleTapDialog = false },
            containerColor = DarkSurface,
            title = { Text("Emergency Call", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Call primary emergency contact '${primary?.name ?: "Contact"}' (${primary?.phoneNumber ?: ""})?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showSingleTapDialog = false
                        if (primary != null) {
                            EmergencyCallHelper.initiateCall(context, primary.phoneNumber)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SOSRed)
                ) {
                    Text("Call Now", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSingleTapDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showNoContactDialog) {
        AlertDialog(
            onDismissRequest = { showNoContactDialog = false },
            containerColor = DarkSurface,
            title = { Text("No Emergency Contact", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("No primary emergency contact configured. Would you like to add one now?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showNoContactDialog = false
                        onNavigateToSosSettings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Configure SOS", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoContactDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
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
