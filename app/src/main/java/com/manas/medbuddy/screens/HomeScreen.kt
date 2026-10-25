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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShowChart
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.BlueGradient
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.DarkBackground
import com.manas.medbuddy.ui.theme.DarkSurface
import com.manas.medbuddy.ui.theme.EmeraldPrimary
import com.manas.medbuddy.ui.theme.MintAccent
import com.manas.medbuddy.ui.theme.SOSRed
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary
import com.manas.medbuddy.util.EmergencyCallHelper
import com.manas.medbuddy.util.SosTapDetector
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToMedicines: () -> Unit = {},
    onNavigateToHealth: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSosSettings: () -> Unit = {},
    onNavigateToHospitalFinder: () -> Unit = {},
    onNavigateToHealthMap: () -> Unit = {},
    onNavigateToSleepTracker: () -> Unit = {},
    onNavigateToSymptomJournal: () -> Unit = {},
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
        FirebaseAuth.getInstance().currentUser?.email ?: "Manas !"
    } catch (_: Exception) {
        "Manas !"
    }
    val userName = if (userEmail.contains("@")) userEmail.substringBefore("@") else userEmail

    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // Dialog states for SOS actions
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

    // Pulse animation for SOS
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

    val currentDateStr = remember {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(Date())
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            // Central Add / SOS Action
            FloatingActionButton(
                onClick = { sosTapDetector.registerTap() },
                containerColor = SOSRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .scale(sosPulseScale)
                    .size(58.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = "SOS Emergency", modifier = Modifier.size(28.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface.copy(alpha = 0.95f),
                contentColor = CyanPrimary,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Medication, contentDescription = "Medicine") },
                    label = { Text("Medicine", fontSize = 11.sp) },
                    selected = false,
                    onClick = onNavigateToMedicines,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                
                // Placeholder for central FAB spacing
                NavigationBarItem(
                    icon = { Box(modifier = Modifier.size(24.dp)) },
                    label = { Text("", fontSize = 11.sp) },
                    selected = false,
                    onClick = { },
                    enabled = false
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Health") },
                    label = { Text("Health", fontSize = 11.sp) },
                    selected = false,
                    onClick = onNavigateToHealth,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    selected = false,
                    onClick = onNavigateToSettings,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: Greeting + Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Good Morning", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = "$userName !",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(currentDateStr, color = TextSecondary, fontSize = 12.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextPrimary)
                    }

                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
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
                }
            }

            // Top Banner Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF003828),
                                    Color(0xFF004D40),
                                    Color(0xFF005F73)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Take your medicine\non time, stay healthy!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 24.sp
                            )
                        }
                        Text("🌿💊", fontSize = 38.sp)
                    }
                }
            }

            // Today's Medicines Section
            SectionHeader(
                title = "Today's Medicines",
                actionText = "View All",
                onActionClick = onNavigateToMedicines
            )

            // Sample / Scheduled Medicines Checklist
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (loading) {
                    CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (medicines.isEmpty()) {
                    // Pre-populated default list matching reference image
                    TodayMedicineRow("Paracetamol", "500 mg", "09:00 AM", isTaken = true)
                    TodayMedicineRow("Vitamin D3", "1000 IU", "01:00 PM", isTaken = false)
                    TodayMedicineRow("BP Tablet", "10 mg", "09:00 PM", isTaken = false)
                } else {
                    medicines.take(3).forEachIndexed { index, med ->
                        TodayMedicineRow(
                            name = med.name,
                            dosage = med.dosage,
                            time = med.time,
                            isTaken = index == 0
                        )
                    }
                }
            }

            // Metrics Grid (3 Stat Cards side by side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetricCard(
                    icon = "👟",
                    label = "Steps",
                    value = "5,420",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHealth
                )
                DashboardMetricCard(
                    icon = "💧",
                    label = "Water",
                    value = "1.8 L",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHealth
                )
                DashboardMetricCard(
                    icon = "🔥",
                    label = "Calories",
                    value = "320 kcal",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHealth
                )
            }

            // Quick Navigation Shortcuts
            SectionHeader(title = "Quick Navigation")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionPill("Sleep Tracker", Modifier.weight(1f), onClick = onNavigateToSleepTracker)
                QuickActionPill("Symptom Journal", Modifier.weight(1f), onClick = onNavigateToSymptomJournal)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionPill("Hospital Finder", Modifier.weight(1f), onClick = onNavigateToHospitalFinder)
                QuickActionPill("Health Map", Modifier.weight(1f), onClick = onNavigateToHealthMap)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionPill("SOS Contacts", Modifier.weight(1f), onClick = onNavigateToSosSettings)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // 1. Single Tap Confirmation Dialog
    if (showSingleTapDialog) {
        val primaryContact = contactRepo.getPrimaryContact()
        if (primaryContact != null) {
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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Initiate call to your primary emergency contact?", color = TextSecondary)
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(primaryContact.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(primaryContact.phoneNumber, color = CyanPrimary, fontSize = 16.sp)
                                if (primaryContact.relationship.isNotBlank()) {
                                    Text("Relationship: ${primaryContact.relationship}", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSingleTapDialog = false
                            EmergencyCallHelper.initiateCall(context, primaryContact.phoneNumber)
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
    }

    // 2. No Primary Contact Dialog
    if (showNoContactDialog) {
        AlertDialog(
            onDismissRequest = { showNoContactDialog = false },
            containerColor = DarkSurface,
            title = { Text("No Primary Contact Set", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You haven't added a primary emergency contact yet. Please configure your emergency contacts in settings.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNoContactDialog = false
                        onNavigateToSosSettings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                ) {
                    Text("Configure SOS Settings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoContactDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // 3. Searching Nearest Hospital Progress Dialog
    if (showSearchingHospitalDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = DarkSurface,
            title = { Text("Locating Nearest Hospital...", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(color = CyanPrimary)
                    Text("Fetching location and finding nearest hospital with phone number...", color = TextSecondary, fontSize = 14.sp)
                }
            },
            confirmButton = { }
        )
    }

    // 4. Hospital Search Result Dialog
    hospitalSearchResult?.let { result ->
        when (result) {
            is HospitalSearchResult.Success -> {
                val hospital = result.hospital
                AlertDialog(
                    onDismissRequest = { hospitalSearchResult = null },
                    containerColor = DarkSurface,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SOSRed)
                            Text("Nearest Hospital Found", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Triple tap detected. Closest hospital:", color = TextSecondary)
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(hospital.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("Distance: ${hospital.getFormattedDistance()}", color = CyanPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text("Phone: ${hospital.phoneNumber}", color = TextPrimary, fontSize = 15.sp)
                                }
                            }
                            Text("Do you want to call this hospital now?", color = TextSecondary, fontSize = 13.sp)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                hospitalSearchResult = null
                                EmergencyCallHelper.initiateCall(context, hospital.phoneNumber)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SOSRed, contentColor = Color.White)
                        ) {
                            Text("Call Hospital", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { hospitalSearchResult = null }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    }
                )
            }
            HospitalSearchResult.PermissionDenied -> {
                AlertDialog(
                    onDismissRequest = { hospitalSearchResult = null },
                    containerColor = DarkSurface,
                    title = { Text("Location Permission Required", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = { Text("Location permission is required to search for nearest hospitals.", color = TextSecondary) },
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
            else -> {
                AlertDialog(
                    onDismissRequest = { hospitalSearchResult = null },
                    containerColor = DarkSurface,
                    title = { Text("Hospital Search Result", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = { Text("Could not locate hospital or phone number. Please call 112.", color = TextSecondary) },
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
private fun TodayMedicineRow(
    name: String,
    dosage: String,
    time: String,
    isTaken: Boolean
) {
    var takenState by remember { mutableStateOf(isTaken) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (takenState) Color(0xFF00E676).copy(alpha = 0.2f)
                        else Color(0xFF2979FF).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(if (takenState) "💊" else "💊", fontSize = 18.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text(dosage, color = TextSecondary, fontSize = 13.sp)
            }

            Text(time, color = CyanPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (takenState) EmeraldPrimary else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (takenState) EmeraldPrimary else TextSecondary,
                        shape = CircleShape
                    )
                    .clickable { takenState = !takenState },
                contentAlignment = Alignment.Center
            ) {
                if (takenState) {
                    Icon(Icons.Default.Check, contentDescription = "Taken", tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(modifier = modifier.clickable { onClick() }) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icon, fontSize = 20.sp)
            Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(value, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickActionPill(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(BlueGradient.copy(alpha = 0.2f), CyanPrimary.copy(alpha = 0.2f))))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
        }
    }
}
