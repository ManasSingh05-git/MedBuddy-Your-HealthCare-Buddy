package com.manas.medbuddy.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.data.model.HospitalSearchResult
import com.manas.medbuddy.data.repository.EmergencyContactRepository
import com.manas.medbuddy.data.repository.HospitalRepository
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.ui.components.AnimatedProgressRing
import com.manas.medbuddy.ui.components.BottomNavigationBar
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.MedBuddyMascot
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.BlueGradient
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.DarkBackground
import com.manas.medbuddy.ui.theme.DarkSurface
import com.manas.medbuddy.ui.theme.EmeraldPrimary
import com.manas.medbuddy.ui.theme.SOSRed
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary
import com.manas.medbuddy.util.EmergencyCallHelper
import com.manas.medbuddy.util.SosTapDetector
import kotlinx.coroutines.launch

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
    onNavigateToHealthTimeline: () -> Unit = {},
    onNavigateToHealthCard: () -> Unit = {},
    onNavigateToAppointmentOrganizer: () -> Unit = {},
    onNavigateToSymptomJournal: () -> Unit = {},
    onNavigateToDoctorPrep: () -> Unit = {},
    onNavigateToMedicalGlossary: () -> Unit = {},
    onNavigateToWellnessCoach: () -> Unit = {},
    onNavigateToReportComparison: () -> Unit = {},
    onNavigateToFamilyProfiles: () -> Unit = {},
    onNavigateToCaregiverDashboard: () -> Unit = {},
    onNavigateToFamilyCalendar: () -> Unit = {},
    onNavigateToAdherenceReports: () -> Unit = {},
    onNavigateToDocumentSharing: () -> Unit = {},
    onNavigateToMenstrualTracker: () -> Unit = {},
    onNavigateToMovementCoach: () -> Unit = {},
    onNavigateToMoreHealthOptions: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val contactRepo = remember { EmergencyContactRepository.getInstance(context) }
    val hospitalRepo = remember { HospitalRepository(context) }

    val userId = try {
        FirebaseAuth.getInstance().currentUser?.uid
    } catch (_: Exception) {
        null
    }
    val userEmail = try {
        FirebaseAuth.getInstance().currentUser?.email ?: "Manas 👋"
    } catch (_: Exception) {
        "Manas 👋"
    }
    val userName = if (userEmail.contains("@")) userEmail.substringBefore("@") + " 👋" else userEmail

    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // Dialog states for SOS
    var showSingleTapDialog by remember { mutableStateOf(false) }
    var showNoContactDialog by remember { mutableStateOf(false) }
    var showSearchingHospitalDialog by remember { mutableStateOf(false) }
    var hospitalSearchResult by remember { mutableStateOf<HospitalSearchResult?>(null) }

    fun executeHospitalSearch() {
        showSearchingHospitalDialog = true
        hospitalSearchResult = null
        coroutineScope.launch {
            val result = hospitalRepo.findNearestHospital()
            showSearchingHospitalDialog = false
            hospitalSearchResult = result
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            executeHospitalSearch()
        } else {
            hospitalSearchResult = HospitalSearchResult.PermissionDenied
        }
    }

    val onSingleTap: () -> Unit = {
        val primary = contactRepo.getPrimaryContact()
        if (primary != null) {
            showSingleTapDialog = true
        } else {
            showNoContactDialog = true
        }
    }

    val onTripleTap: () -> Unit = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            executeHospitalSearch()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
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

    DisposableEffect(userId) {
        if (userId == null) {
            loading = false
            onDispose { }
        } else {
            try {
                val reference = FirebaseDatabase.getInstance().reference.child("medicines").child(userId)
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        medicines = snapshot.children.mapNotNull { child: DataSnapshot ->
                            child.getValue(Medicine::class.java)?.copy(id = child.key ?: "")
                        }
                        loading = false
                    }
                    override fun onCancelled(error: DatabaseError) { loading = false }
                }
                reference.addValueEventListener(listener)
                onDispose { reference.removeEventListener(listener) }
            } catch (_: Exception) {
                loading = false
                onDispose { }
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            BottomNavigationBar(
                selectedTab = "home",
                onTabSelected = { tab ->
                    when (tab) {
                        "tools" -> onNavigateToMedicines()
                        "assistant" -> onNavigateToReportExplainer()
                        "insights" -> onNavigateToHealth()
                        "profile" -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header: Avatar + Greeting + Bell Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(EmeraldPrimary, CyanPrimary)))
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .clickable { onNavigateToSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = CyanPrimary)
                    }

                    Column {
                        Text("Good Morning,", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            text = userName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextPrimary)
                }
            }

            // Health Score Card with MedBuddy Mascot
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Your Health Score", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(72.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedProgressRing(
                                    progress = 0.86f,
                                    modifier = Modifier.size(72.dp),
                                    colors = listOf(EmeraldPrimary, CyanPrimary),
                                    strokeWidth = 10f
                                )
                                Text("86", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                            }

                            Column {
                                Text("Great!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                Text("Keep it up", fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }

                    MedBuddyMascot(size = 85.dp)
                }
            }

            // 4 Grid Stat Cards (Steps, Sleep, Calories, Water)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardGridStatCard(
                        icon = "👟",
                        title = "Steps",
                        value = "5,420",
                        sublabel = "70% of goal",
                        accentColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHealth
                    )
                    DashboardGridStatCard(
                        icon = "🌙",
                        title = "Sleep",
                        value = "7h 40m",
                        sublabel = "Good",
                        accentColor = CyanPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSleepTracker
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardGridStatCard(
                        icon = "🔥",
                        title = "Calories",
                        value = "320 kcal",
                        sublabel = "Today",
                        accentColor = SOSRed,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHealth
                    )
                    DashboardGridStatCard(
                        icon = "💧",
                        title = "Water",
                        value = "1.8 L",
                        sublabel = "60% of goal",
                        accentColor = CyanPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHealth
                    )
                }
            }

            // Quick Health Insight Banner
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSleepTracker() },
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(BlueGradient.copy(alpha = 0.3f), CyanPrimary.copy(alpha = 0.3f)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 20.sp)
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Quick Health Insight ✨", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "Your sleep improved by 20% this week. Keep a consistent sleep schedule!",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                }
            }

            // Quick Actions Row (Medicines, Appointments, Health Map, SOS)
            SectionHeader(title = "Quick Actions")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile("Medicines", Icons.Default.Medication, EmeraldPrimary, Modifier.weight(1f), onClick = onNavigateToMedicines)
                ActionTile("Appointments", Icons.Default.CalendarMonth, CyanPrimary, Modifier.weight(1f), onClick = onNavigateToAppointmentOrganizer)
                ActionTile("Health Map", Icons.Default.LocationOn, BlueGradient, Modifier.weight(1f), onClick = onNavigateToHealthMap)
                ActionTile("SOS", Icons.Default.Warning, SOSRed, Modifier.weight(1f), onClick = { sosTapDetector.registerTap() })
            }

            // Prominent "More Health Options" Banner Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMoreHealthOptions() },
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(EmeraldPrimary, CyanPrimary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 22.sp)
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("More Health Options", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Access 20+ specialized medical tools & AI features", color = TextSecondary, fontSize = 12.sp)
                    }

                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Single Tap Confirmation Dialog
    if (showSingleTapDialog) {
        val primary = contactRepo.getPrimaryContact()
        AlertDialog(
            onDismissRequest = { showSingleTapDialog = false },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = SOSRed)
                    Text("Confirm Emergency Call", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Initiate emergency call to '${primary?.name ?: "Contact"}' (${primary?.phoneNumber ?: ""})?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSingleTapDialog = false
                        if (primary != null) {
                            EmergencyCallHelper.initiateCall(context, primary.phoneNumber)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SOSRed, contentColor = Color.White)
                ) {
                    Text("Call Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSingleTapDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // No Contact Dialog
    if (showNoContactDialog) {
        AlertDialog(
            onDismissRequest = { showNoContactDialog = false },
            containerColor = DarkSurface,
            title = { Text("No Primary Contact Set", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("No primary emergency contact configured. Would you like to add one in settings?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showNoContactDialog = false
                        onNavigateToSosSettings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                ) {
                    Text("Configure SOS", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoContactDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Searching Hospital Dialog
    if (showSearchingHospitalDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = DarkSurface,
            title = { Text("Locating Nearest Hospital...", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(color = CyanPrimary)
                    Text("Finding nearest hospital with emergency contact...", color = TextSecondary, fontSize = 14.sp)
                }
            },
            confirmButton = { }
        )
    }

    // Search Result Dialog
    hospitalSearchResult?.let { result ->
        when (result) {
            is HospitalSearchResult.Success -> {
                val hospital = result.hospital
                AlertDialog(
                    onDismissRequest = { hospitalSearchResult = null },
                    containerColor = DarkSurface,
                    title = { Text("Nearest Hospital Found", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = { Text("${hospital.name}\n${hospital.address}\nPhone: ${hospital.phoneNumber}", color = TextSecondary) },
                    confirmButton = {
                        Button(
                            onClick = {
                                hospitalSearchResult = null
                                EmergencyCallHelper.initiateCall(context, hospital.phoneNumber)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SOSRed)
                        ) {
                            Text("Call Hospital")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { hospitalSearchResult = null }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    }
                )
            }
            else -> {
                AlertDialog(
                    onDismissRequest = { hospitalSearchResult = null },
                    containerColor = DarkSurface,
                    title = { Text("Hospital Search Failed", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = { Text("Unable to locate nearby hospital. Call 112 directly if urgent.", color = TextSecondary) },
                    confirmButton = {
                        Button(
                            onClick = { hospitalSearchResult = null },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                        ) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DashboardGridStatCard(
    icon: String,
    title: String,
    value: String,
    sublabel: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(modifier = modifier.clickable { onClick() }) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(icon, fontSize = 18.sp)
                Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(sublabel, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ActionTile(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(modifier = modifier.clickable { onClick() }) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Text(label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
