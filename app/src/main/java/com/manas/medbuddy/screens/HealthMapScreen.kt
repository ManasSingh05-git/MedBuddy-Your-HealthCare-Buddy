package com.manas.medbuddy.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.manas.medbuddy.data.model.Hospital
import com.manas.medbuddy.data.model.HospitalSearchResult
import com.manas.medbuddy.data.repository.HospitalRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary
import com.manas.medbuddy.util.EmergencyCallHelper
import com.manas.medbuddy.util.PhoneNumberValidator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { HospitalRepository(context) }
    val keyboardController = LocalSoftwareKeyboardController.current

    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var selectedCategory by remember { mutableStateOf("Hospitals") } // "Hospitals", "Pharmacies", "Clinics"
    var searchQuery by remember { mutableStateOf("") }

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var hospitals by remember { mutableStateOf<List<Hospital>>(emptyList()) }
    var selectedHospital by remember { mutableStateOf<Hospital?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val defaultLatLng = LatLng(20.5937, 78.9629) // Centroid fallback
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 12f)
    }

    fun loadMapData() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            val loc = repository.getUserLocation()
            userLocation = loc

            val res = repository.searchNearbyHospitalsList(radiusMeters = 15000)
            isLoading = false
            when (res) {
                is HospitalSearchResult.SuccessList -> {
                    hospitals = res.hospitals
                    if (loc != null) {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 13f)
                        )
                    } else if (hospitals.isNotEmpty()) {
                        val first = hospitals.first()
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), 13f)
                        )
                    }
                }
                is HospitalSearchResult.Success -> {
                    hospitals = listOf(res.hospital)
                }
                HospitalSearchResult.PermissionDenied -> {
                    errorMessage = "Location permission is required to center map on your position."
                }
                HospitalSearchResult.LocationUnavailable -> {
                    errorMessage = "GPS location unavailable. You can search manually using the search bar."
                }
                HospitalSearchResult.NoHospitalFound -> {
                    hospitals = emptyList()
                    errorMessage = "No nearby medical places found for this category."
                }
                is HospitalSearchResult.Error -> {
                    errorMessage = res.message
                }
            }
        }
    }

    fun searchManualLocation(query: String) {
        if (query.isBlank()) return
        keyboardController?.hide()
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            val res = repository.searchHospitalsByManualQuery(query)
            isLoading = false
            when (res) {
                is HospitalSearchResult.SuccessList -> {
                    hospitals = res.hospitals
                    if (hospitals.isNotEmpty()) {
                        val first = hospitals.first()
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), 13f)
                        )
                    }
                }
                is HospitalSearchResult.Error -> errorMessage = res.message
                else -> errorMessage = "Location '$query' not found."
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fine || coarse) {
            loadMapData()
        } else {
            errorMessage = "Location permission denied."
        }
    }

    LaunchedEffect(Unit) {
        if (repository.hasLocationPermission()) {
            loadMapData()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Interactive Health Map", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { loadMapData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Map", tint = CyanPrimary)
                    }
                    IconButton(onClick = {
                        mapType = when (mapType) {
                            MapType.NORMAL -> MapType.SATELLITE
                            MapType.SATELLITE -> MapType.TERRAIN
                            else -> MapType.NORMAL
                        }
                    }) {
                        Icon(Icons.Default.Layers, contentDescription = "Change Map Style", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Google Map View
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = mapType,
                    isMyLocationEnabled = repository.hasLocationPermission()
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                )
            ) {
                // User Location Marker if available
                userLocation?.let { loc ->
                    Marker(
                        state = MarkerState(position = LatLng(loc.latitude, loc.longitude)),
                        title = "Your Current Location",
                        snippet = "You are here"
                    )
                }

                // Hospital & Medical Facility Markers
                hospitals.forEach { hospital ->
                    Marker(
                        state = MarkerState(position = LatLng(hospital.latitude, hospital.longitude)),
                        title = hospital.name,
                        snippet = hospital.getFormattedDistance(),
                        onClick = {
                            selectedHospital = hospital
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLng(LatLng(hospital.latitude, hospital.longitude))
                                )
                            }
                            true
                        }
                    )
                }
            }

            // Top Search & Category Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Manual Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search location or address...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { searchManualLocation(searchQuery) }),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchManualLocation(searchQuery) }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = CyanPrimary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface.copy(alpha = 0.95f),
                        unfocusedContainerColor = DarkSurface.copy(alpha = 0.9f),
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = DarkSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipItem("Hospitals", Icons.Default.MedicalServices, selectedCategory == "Hospitals") {
                        selectedCategory = "Hospitals"
                        loadMapData()
                    }
                    FilterChipItem("Pharmacies", Icons.Default.LocalPharmacy, selectedCategory == "Pharmacies") {
                        selectedCategory = "Pharmacies"
                        loadMapData()
                    }
                    FilterChipItem("Clinics", Icons.Default.LocationOn, selectedCategory == "Clinics") {
                        selectedCategory = "Clinics"
                        loadMapData()
                    }
                }

                // Error / Info Banner if present
                errorMessage?.let { msg ->
                    Surface(
                        color = DarkSurface.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SOSRed.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SOSRed)
                            Text(msg, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { errorMessage = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Map Floating Controls (Zoom in, Zoom out, My Location)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                        }
                    },
                    containerColor = DarkSurface.copy(alpha = 0.9f),
                    contentColor = CyanPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }

                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                        }
                    },
                    containerColor = DarkSurface.copy(alpha = 0.9f),
                    contentColor = CyanPrimary
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }

                FloatingActionButton(
                    onClick = {
                        if (repository.hasLocationPermission()) {
                            loadMapData()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }
                    },
                    containerColor = CyanPrimary,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Center on My Location")
                }
            }

            // Loading Indicator Overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = CyanPrimary)
                            Text("Loading Map Places...", color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Selected Hospital Bottom Card Overlay
            AnimatedVisibility(
                visible = selectedHospital != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                selectedHospital?.let { hospital ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(hospital.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                                    if (hospital.address.isNotBlank()) {
                                        Text(hospital.address, color = TextSecondary, fontSize = 13.sp)
                                    }
                                    Text(hospital.getFormattedDistance(), color = CyanPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }

                                IconButton(onClick = { selectedHospital = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Card", tint = TextSecondary)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { launchDirections(context, hospital) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Directions", fontSize = 12.sp)
                                }

                                val hasPhone = PhoneNumberValidator.isValid(hospital.phoneNumber)
                                Button(
                                    onClick = { EmergencyCallHelper.initiateCall(context, hospital.phoneNumber) },
                                    enabled = hasPhone,
                                    colors = ButtonDefaults.buttonColors(containerColor = SOSRed, contentColor = Color.White),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call", fontSize = 12.sp)
                                }

                                IconButton(
                                    onClick = { shareHospitalLocation(context, hospital) },
                                    modifier = Modifier.background(DarkSurface, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share Location", tint = CyanPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CyanPrimary,
            selectedLabelColor = Color.Black,
            selectedLeadingIconColor = Color.Black,
            containerColor = DarkSurface.copy(alpha = 0.9f),
            labelColor = TextPrimary,
            iconColor = CyanPrimary
        )
    )
}

private fun launchDirections(context: Context, hospital: Hospital) {
    val geoUri = Uri.parse("google.navigation:q=${hospital.latitude},${hospital.longitude}")
    val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${hospital.latitude},${hospital.longitude}")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(webIntent)
        } catch (_: Exception) {
            // Ignore if no browser
        }
    }
}

private fun shareHospitalLocation(context: Context, hospital: Hospital) {
    val shareText = "Emergency Facility: ${hospital.name}\n" +
            "Address: ${hospital.address}\n" +
            "Distance: ${hospital.getFormattedDistance()}\n" +
            "Maps Link: https://www.google.com/maps/search/?api=1&query=${hospital.latitude},${hospital.longitude}"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Medical Facility Location - ${hospital.name}")
        putExtra(Intent.EXTRA_TEXT, shareText)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Share Hospital Location").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (_: Exception) {
        // Ignore if no share handler
    }
}
