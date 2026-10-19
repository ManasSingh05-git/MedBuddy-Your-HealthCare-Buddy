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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.manas.medbuddy.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToMedicines: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSosSettings: () -> Unit,
    onNavigateToHospitalFinder: () -> Unit,
    onNavigateToHealthMap: () -> Unit,
    onNavigateToSleepTracker: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val contactRepo = remember { EmergencyContactRepository.getInstance(context) }
    val hospitalRepo = remember { HospitalRepository(context) }
    val sleepRepo = remember { com.manas.medbuddy.data.repository.SleepRepository.getInstance(context) }
    val sleepRecords by sleepRepo.recordsFlow.collectAsState()

    val userId = try {
        FirebaseAuth.getInstance().currentUser?.uid
    } catch (e: Exception) {
        null
    }
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val themeMode = LocalThemeMode.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    // User name greeting
    val userName = "User"
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
<<<<<<< HEAD
                        medicines = snapshot.children.mapNotNull { child -> child.getValue(Medicine::class.java)?.copy(id = child.key ?: "") }
=======
                        medicines = snapshot.children.mapNotNull { child: DataSnapshot -> child.getValue(Medicine::class.java)?.copy(id = child.key ?: "") }
>>>>>>> origin/feat/sleep-tracking
                        loading = false
                    }
                    override fun onCancelled(error: DatabaseError) { loading = false }
                }
                reference.addValueEventListener(listener)
                onDispose { reference.removeEventListener(listener) }
<<<<<<< HEAD
            } catch (e: Exception) {
=======
            } catch (_: Exception) {
>>>>>>> origin/feat/sleep-tracking
                loading = false
                onDispose { }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
            ) {
<<<<<<< HEAD
                Spacer(Modifier.height(32.dp))
=======
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
                    val latestSleep = sleepRecords.firstOrNull()
                    MetricCard(
                        title = "Sleep",
                        value = latestSleep?.getFormattedDuration() ?: "Track Sleep",
                        subtitle = if (latestSleep != null) "Wake: ${latestSleep.getFormattedEndTime()}" else "Tap to log",
                        modifier = Modifier.weight(1f)
                    )
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard("Sleep Tracker", Modifier.weight(1f), onClick = onNavigateToSleepTracker)
                    ActionCard("Nearby Hospitals", Modifier.weight(1f), onClick = onNavigateToHospitalFinder)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard("Health Map", Modifier.weight(1f), onClick = onNavigateToHealthMap)
                    ActionCard("SOS Contacts", Modifier.weight(1f), onClick = onNavigateToSosSettings)
                }
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
>>>>>>> origin/feat/sleep-tracking
                Text(
                    "Hello, $userName",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile & Details") },
                    selected = false,
                    onClick = { /* TODO Edit profile */ }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("App Settings") },
                    selected = false,
                    onClick = onNavigateToSettings
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Warning, contentDescription = "SOS Settings") },
                    label = { Text("SOS Settings") },
                    selected = false,
                    onClick = onNavigateToSosSettings
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                    label = { Text("About MedBuddy") },
                    selected = false,
                    onClick = { /* TODO Info */ }
                )
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error) },
                    label = { Text("Log out", color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
<<<<<<< HEAD
                        try { FirebaseAuth.getInstance().signOut() } catch (e: Exception) {}
                        onLogout()
=======
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
            HospitalSearchResult.NoHospitalFound -> {
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
            is HospitalSearchResult.SuccessList -> {
                val hospital = result.hospitals.firstOrNull()
                if (hospital != null) {
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
>>>>>>> origin/feat/sleep-tracking
                    }
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("$greeting, $userName", fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            themeMode.value = if (themeMode.value == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT
                        }) {
                            Icon(
                                imageVector = if (themeMode.value == ThemeMode.LIGHT) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
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
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

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

                // Health Section (Clickable)
                Surface(
                    onClick = onNavigateToHealth,
                    color = Color.Transparent,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Health Overview", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                                Text("View All >", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                            Text("See your steps, water intake, sleep, and heart rate.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }
            
                Spacer(modifier = Modifier.height(40.dp)) // Space for FAB
            }
        }
    }
}
