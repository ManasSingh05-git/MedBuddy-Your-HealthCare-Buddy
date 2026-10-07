package com.manas.medbuddy.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToMedicines: () -> Unit, onNavigateToHealth: () -> Unit, onLogout: () -> Unit) {
    val userId = try {
        FirebaseAuth.getInstance().currentUser?.uid
    } catch (e: Exception) {
        null
    }
    
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val themeMode = LocalThemeMode.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
            ) {
                Spacer(Modifier.height(32.dp))
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
                    onClick = { /* TODO Settings */ }
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
                        try { FirebaseAuth.getInstance().signOut() } catch (e: Exception) {}
                        onLogout()
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
