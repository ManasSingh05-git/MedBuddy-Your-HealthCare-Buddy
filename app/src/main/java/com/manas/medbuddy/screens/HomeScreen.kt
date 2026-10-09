package com.manas.medbuddy.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
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
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.util.EmergencyCallHelper
import com.manas.medbuddy.util.SosTapDetector
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onAddMedicine: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSosSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val contactRepo = remember { EmergencyContactRepository.getInstance(context) }
    val hospitalRepo = remember { HospitalRepository(context) }

    val userId = try {
        FirebaseAuth.getInstance().currentUser?.uid
    } catch (e: Exception) {
        null
    }
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

    // Gentle pulse animation for SOS button
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
                        medicines = snapshot.children.mapNotNull { child: DataSnapshot -> child.getValue(Medicine::class.java)?.copy(id = child.key ?: "") }
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
                    onClick = onNavigateToSettings,
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
                TextButton(onClick = {
                    try { FirebaseAuth.getInstance().signOut() } catch (_: Exception) {}
                    onLogout()
                }) { 
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
                ActionCard("SOS Contacts", Modifier.weight(1f), onClick = onNavigateToSosSettings)
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
                    Text("Fetching current location and searching for nearest hospital with a valid phone number...", color = TextSecondary, fontSize = 14.sp)
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
                            Text("A triple tap was detected. Found closest emergency hospital:", color = TextSecondary)
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
                    text = { Text("Location permission is required to search for nearest hospitals during a triple-tap SOS emergency.", color = TextSecondary) },
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
            HospitalSearchResult.LocationUnavailable -> {
                AlertDialog(
                    onDismissRequest = { hospitalSearchResult = null },
                    containerColor = DarkSurface,
                    title = { Text("Location Unavailable", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = { Text("Unable to determine current location. Please verify that GPS location services are turned on.", color = TextSecondary) },
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
            HospitalSearchResult.NoHospitalWithPhoneFound -> {
                AlertDialog(
                    onDismissRequest = { hospitalSearchResult = null },
                    containerColor = DarkSurface,
                    title = { Text("No Hospital Found", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = { Text("Could not find a nearby hospital with a valid contact phone number within range.", color = TextSecondary) },
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
            is HospitalSearchResult.Error -> {
                AlertDialog(
                    onDismissRequest = { hospitalSearchResult = null },
                    containerColor = DarkSurface,
                    title = { Text("Search Error", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = { Text(result.message, color = TextSecondary) },
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
