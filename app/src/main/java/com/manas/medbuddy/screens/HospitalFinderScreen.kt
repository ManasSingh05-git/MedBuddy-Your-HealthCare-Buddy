package com.manas.medbuddy.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.manas.medbuddy.data.model.Hospital
import com.manas.medbuddy.data.model.HospitalSearchResult
import com.manas.medbuddy.data.repository.HospitalRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.util.EmergencyCallHelper
import com.manas.medbuddy.util.PhoneNumberValidator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalFinderScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { HospitalRepository(context) }
    val keyboardController = LocalSoftwareKeyboardController.current

    var isMapView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<HospitalSearchResult?>(null) }
    var selectedHospital by remember { mutableStateOf<Hospital?>(null) }
    var showEmergencyCallDialog by remember { mutableStateOf(false) }

    fun loadHospitalsWithGPS() {
        isLoading = true
        searchResult = null
        coroutineScope.launch {
            val res = repository.searchNearbyHospitalsList(radiusMeters = 15000)
            isLoading = false
            searchResult = res
        }
    }

    fun loadHospitalsWithManualSearch(query: String) {
        if (query.isBlank()) return
        keyboardController?.hide()
        isLoading = true
        searchResult = null
        coroutineScope.launch {
            val res = repository.searchHospitalsByManualQuery(query)
            isLoading = false
            searchResult = res
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            loadHospitalsWithGPS()
        } else {
            searchResult = HospitalSearchResult.PermissionDenied
        }
    }

    LaunchedEffect(Unit) {
        if (repository.hasLocationPermission()) {
            loadHospitalsWithGPS()
        } else {
            searchResult = HospitalSearchResult.PermissionDenied
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Nearby Hospital Finder", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (searchQuery.isNotBlank()) {
                            loadHospitalsWithManualSearch(searchQuery)
                        } else if (repository.hasLocationPermission()) {
                            loadHospitalsWithGPS()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CyanPrimary)
                    }
                    IconButton(onClick = { isMapView = !isMapView }) {
                        if (isMapView) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "List View", tint = CyanPrimary)
                        } else {
                            Icon(Icons.Default.Map, contentDescription = "Map View", tint = CyanPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Emergency Disclaimer Banner
            Surface(
                color = SOSRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SOSRed.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = SOSRed, modifier = Modifier.size(24.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("For life-threatening emergencies, call 112 immediately.", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("This tool provides hospital directory info. Always verify facility suitability.", color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { showEmergencyCallDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SOSRed, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Call 112", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Search & GPS Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search city, address, or region...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { loadHospitalsWithManualSearch(searchQuery) }),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { loadHospitalsWithManualSearch(searchQuery) }) {
                                Icon(Icons.Default.Search, contentDescription = "Submit Search", tint = CyanPrimary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextSecondary,
                        focusedLabelColor = CyanPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        searchQuery = ""
                        if (repository.hasLocationPermission()) {
                            loadHospitalsWithGPS()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }
                    },
                    modifier = Modifier.background(DarkSurface, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Use My Location", tint = CyanPrimary)
                }
            }

            // Main Content Area
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(color = CyanPrimary)
                        Text("Searching for nearby hospitals...", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                when (val result = searchResult) {
                    is HospitalSearchResult.SuccessList -> {
                        val hospitals = result.hospitals
                        if (hospitals.isEmpty()) {
                            EmptyHospitalsView { loadHospitalsWithGPS() }
                        } else if (isMapView) {
                            MapViewContent(
                                hospitals = hospitals,
                                onSelectHospital = { selectedHospital = it }
                            )
                        } else {
                            ListViewContent(
                                hospitals = hospitals,
                                onSelectHospital = { selectedHospital = it },
                                onCallHospital = { EmergencyCallHelper.initiateCall(context, it.phoneNumber) },
                                onGetDirections = { launchGoogleMapsDirections(context, it) }
                            )
                        }
                    }
                    is HospitalSearchResult.Success -> {
                        val hospitals = listOf(result.hospital)
                        if (isMapView) {
                            MapViewContent(
                                hospitals = hospitals,
                                onSelectHospital = { selectedHospital = it }
                            )
                        } else {
                            ListViewContent(
                                hospitals = hospitals,
                                onSelectHospital = { selectedHospital = it },
                                onCallHospital = { EmergencyCallHelper.initiateCall(context, it.phoneNumber) },
                                onGetDirections = { launchGoogleMapsDirections(context, it) }
                            )
                        }
                    }
                    HospitalSearchResult.PermissionDenied -> {
                        PermissionDeniedView(
                            onRequestPermission = {
                                locationPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                )
                            }
                        )
                    }
                    HospitalSearchResult.LocationUnavailable -> {
                        ErrorView(
                            title = "Location Unavailable",
                            message = "Could not detect your GPS location. Ensure location service is enabled or enter a location manually above.",
                            onRetry = { loadHospitalsWithGPS() }
                        )
                    }
                    HospitalSearchResult.NoHospitalFound -> {
                        EmptyHospitalsView { loadHospitalsWithGPS() }
                    }
                    is HospitalSearchResult.Error -> {
                        ErrorView(
                            title = "Search Error",
                            message = result.message,
                            onRetry = { loadHospitalsWithGPS() }
                        )
                    }
                    null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Enter a location or tap refresh to find hospitals.", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    // Hospital Detail Modal
    selectedHospital?.let { hospital ->
        HospitalDetailModal(
            hospital = hospital,
            onDismiss = { selectedHospital = null },
            onCall = { EmergencyCallHelper.initiateCall(context, hospital.phoneNumber) },
            onDirections = { launchGoogleMapsDirections(context, hospital) }
        )
    }

    // 112 Emergency Confirmation Dialog
    if (showEmergencyCallDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyCallDialog = false },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = SOSRed)
                    Text("Call Emergency Services 112", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = { Text("Are you sure you want to dial emergency services (112)?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showEmergencyCallDialog = false
                        EmergencyCallHelper.initiateCall(context, "112")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SOSRed, contentColor = Color.White)
                ) {
                    Text("Call 112 Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyCallDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun MapViewContent(
    hospitals: List<Hospital>,
    onSelectHospital: (Hospital) -> Unit
) {
    val firstHosp = hospitals.firstOrNull()
    val initialPosition = if (firstHosp != null) {
        LatLng(firstHosp.latitude, firstHosp.longitude)
    } else {
        LatLng(20.5937, 78.9629) // Default India centroid fallback
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 12f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false)
        ) {
            hospitals.forEach { hospital ->
                Marker(
                    state = MarkerState(position = LatLng(hospital.latitude, hospital.longitude)),
                    title = hospital.name,
                    snippet = hospital.getFormattedDistance(),
                    onClick = {
                        onSelectHospital(hospital)
                        true
                    }
                )
            }
        }
    }
}

@Composable
private fun ListViewContent(
    hospitals: List<Hospital>,
    onSelectHospital: (Hospital) -> Unit,
    onCallHospital: (Hospital) -> Unit,
    onGetDirections: (Hospital) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = hospitals, key = { item -> item.id }) { hospitalItem ->
            HospitalCard(
                hospital = hospitalItem,
                onClick = { onSelectHospital(hospitalItem) },
                onCall = { onCallHospital(hospitalItem) },
                onDirections = { onGetDirections(hospitalItem) }
            )
        }
    }
}

@Composable
private fun HospitalCard(
    hospital: Hospital,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onDirections: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = onClick,
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(26.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(hospital.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                        if (hospital.address.isNotBlank()) {
                            Text(hospital.address, color = TextSecondary, fontSize = 13.sp)
                        }
                        Text(hospital.getFormattedDistance(), color = CyanPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    hospital.rating?.let { rating ->
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                Text(
                                    String.format(java.util.Locale.US, "%.1f", rating),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDirections,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Directions", fontSize = 13.sp)
                    }

                    val hasValidPhone = PhoneNumberValidator.isValid(hospital.phoneNumber)
                    Button(
                        onClick = onCall,
                        enabled = hasValidPhone,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SOSRed,
                            contentColor = Color.White,
                            disabledContainerColor = DarkSurface,
                            disabledContentColor = TextSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (hasValidPhone) "Call" else "No Phone", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun HospitalDetailModal(
    hospital: Hospital,
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onDirections: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = CyanPrimary)
                Text("Hospital Details", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(hospital.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (hospital.address.isNotBlank()) {
                            Text("Address: ${hospital.address}", color = TextSecondary, fontSize = 13.sp)
                        }
                        Text("Distance: ${hospital.getFormattedDistance()}", color = CyanPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)

                        hospital.rating?.let { rating ->
                            Text("Rating: ⭐ $rating (${hospital.userRatingsTotal ?: 0} reviews)", color = TextPrimary, fontSize = 13.sp)
                        }

                        if (PhoneNumberValidator.isValid(hospital.phoneNumber)) {
                            Text("Phone: ${hospital.phoneNumber}", color = CyanPrimary, fontSize = 14.sp)
                        } else {
                            Text("Phone: Not available from public records", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }

                Text(
                    "Disclaimer: Hospital status, open hours, and specialized treatment capabilities should be verified with emergency services.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (PhoneNumberValidator.isValid(hospital.phoneNumber)) {
                    Button(
                        onClick = {
                            onDismiss()
                            onCall()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SOSRed, contentColor = Color.White)
                    ) {
                        Text("Call")
                    }
                }
                Button(
                    onClick = {
                        onDismiss()
                        onDirections()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                ) {
                    Text("Directions")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun PermissionDeniedView(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SOSRed, modifier = Modifier.size(48.dp))
            Text("Location Permission Needed", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                "MedBuddy requires location permission to automatically find nearby hospitals around your current position. You can also search manually using the search bar above.",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
            ) {
                Text("Grant Location Permission", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyHospitalsView(onRefresh: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text("No Hospitals Found", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("No hospital facilities were returned for this location. Try entering a city/location name in the search bar above.", color = TextSecondary, fontSize = 13.sp)
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
            ) {
                Text("Refresh Search")
            }
        }
    }
}

@Composable
private fun ErrorView(title: String, message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = SOSRed, modifier = Modifier.size(40.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(message, color = TextSecondary, fontSize = 13.sp)
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
            ) {
                Text("Try Again")
            }
        }
    }
}

private fun launchGoogleMapsDirections(context: Context, hospital: Hospital) {
    val geoUri = Uri.parse("google.navigation:q=${hospital.latitude},${hospital.longitude}")
    val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback to browser or generic map view intent
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${hospital.latitude},${hospital.longitude}")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(webIntent)
        } catch (_: Exception) {
            // Ignore if no web browser
        }
    }
}
