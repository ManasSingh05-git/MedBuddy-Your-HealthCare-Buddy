package com.manas.medbuddy.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Vaccines
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
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.data.MedicineRepository
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.navigation.Routes
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.vault.MedicalRecord
import com.manas.medbuddy.vault.MedicalVaultRepository
import com.manas.medbuddy.vaccination.VaccinationRecord
import com.manas.medbuddy.vaccination.VaccinationRepository
import com.manas.medbuddy.timeline.HealthTimelineRepository
import com.manas.medbuddy.timeline.TimelineDateFilter
import com.manas.medbuddy.timeline.TimelineEvent
import com.manas.medbuddy.timeline.TimelineEventType
import com.manas.medbuddy.timeline.TimelineSourceType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HealthTimelineScreen(
    onBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {},
    timelineRepository: HealthTimelineRepository? = null,
    vaultRepository: MedicalVaultRepository? = null,
    vaccinationRepository: VaccinationRepository? = null,
    medicineRepository: MedicineRepository? = null
) {
    val context = LocalContext.current
    val timelineRepo = timelineRepository ?: remember { HealthTimelineRepository(context) }
    val vaultRepo = vaultRepository ?: remember { MedicalVaultRepository(context) }
    val vacRepo = vaccinationRepository ?: remember { VaccinationRepository(context) }
    val medRepo = medicineRepository ?: remember { MedicineRepository(context) }

    var customEvents by remember { mutableStateOf<List<TimelineEvent>>(emptyList()) }
    var vaultRecords by remember { mutableStateOf<List<MedicalRecord>>(emptyList()) }
    var vaccinations by remember { mutableStateOf<List<VaccinationRecord>>(emptyList()) }
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf(TimelineEventType.ALL.name) }
    var selectedDateFilter by remember { mutableStateOf(TimelineDateFilter.ALL_TIME) }

    var showAddEventModal by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<TimelineEvent?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val customListener: ValueEventListener? = try {
            timelineRepo.listenToCustomEvents(
                onUpdate = { list -> customEvents = list },
                onError = {}
            )
        } catch (_: Exception) { null }

        val vaultListener: ValueEventListener? = try {
            vaultRepo.listenToRecords(
                onUpdate = { list -> vaultRecords = list },
                onError = {}
            )
        } catch (_: Exception) { null }

        val vacListener: ValueEventListener? = try {
            vacRepo.listenToVaccinations(
                onUpdate = { list -> vaccinations = list },
                onError = {}
            )
        } catch (_: Exception) { null }

        val medListener: ValueEventListener? = try {
            medRepo.listenToMedicines(
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

        onDispose {
            if (customListener != null) timelineRepo.removeListener(customListener)
            if (vaultListener != null) vaultRepo.removeListener(vaultListener)
            if (vacListener != null) vacRepo.removeListener(vacListener)
            if (medListener != null) medRepo.removeMedicinesListener(medListener)
        }
    }

    val allAggregatedEvents = remember(customEvents, vaultRecords, vaccinations, medicines) {
        timelineRepo.aggregateAllTimelineEvents(customEvents, vaultRecords, vaccinations, medicines)
    }

    val filteredEvents = remember(allAggregatedEvents, selectedCategory, selectedDateFilter) {
        val byCategory = if (selectedCategory == TimelineEventType.ALL.name) {
            allAggregatedEvents
        } else {
            allAggregatedEvents.filter { it.eventType == selectedCategory }
        }
        timelineRepo.filterEventsByDate(byCategory, selectedDateFilter)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Medical History Timeline 📅", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = CyanPrimary)
                    }
                    IconButton(onClick = { showAddEventModal = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Event", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface.copy(alpha = 0.9f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventModal = true },
                containerColor = CyanPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Custom Event")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TimelineEventType.entries.toTypedArray()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.name,
                        onClick = { selectedCategory = cat.name },
                        label = { Text(formatCategoryLabel(cat.name)) },
                        colors = futuristicFilterChipColors()
                    )
                }
            }

            // Date Range Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TimelineDateFilter.entries.toTypedArray()) { filter ->
                    FilterChip(
                        selected = selectedDateFilter == filter,
                        onClick = { selectedDateFilter = filter },
                        label = { Text(formatDateFilterLabel(filter)) },
                        colors = futuristicFilterChipColors()
                    )
                }
            }

            // Timeline List View
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            } else if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(48.dp))
                        Text("No timeline events found", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Add custom events or record prescriptions, vaccinations, and lab reports to view your medical timeline.", fontSize = 13.sp, color = TextSecondary)
                        Button(
                            onClick = { showAddEventModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("+ Add Custom Event", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        TimelineEventCard(
                            event = event,
                            onClick = {
                                if (event.targetRoute.isNotBlank()) {
                                    onNavigateToRoute(event.targetRoute)
                                }
                            },
                            onDelete = { deleteCandidate = event }
                        )
                    }
                }
            }
        }
    }

    // Add Custom Event Modal
    if (showAddEventModal) {
        AddCustomEventModal(
            onDismiss = { showAddEventModal = false },
            repository = timelineRepo,
            onSuccess = {
                showAddEventModal = false
                Toast.makeText(context, "Custom health event added to timeline!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Candidate Dialog
    if (deleteCandidate != null) {
        val target = deleteCandidate!!
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            containerColor = DarkSurface,
            title = { Text("Delete Timeline Event", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${target.title}' from your personal timeline?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        timelineRepo.deleteCustomEvent(target.id) { _, _ -> }
                        deleteCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SOSRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = DarkSurface,
            title = { Text("Medical Timeline Guidelines", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Aggregates medical events chronologically from all modules.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Clearly distinguishes Clinician-Documented Facts (e.g. verified reports, hospital vaccinations) from Self-Reported User Events.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Tap any event to navigate directly to its source record.", color = TextSecondary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)) {
                    Text("Understood", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun TimelineEventCard(
    event: TimelineEvent,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isClinicianFact = event.sourceType == TimelineSourceType.CLINICIAN_FACT.name

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Event Icon Column
            Surface(
                color = if (isClinicianFact) Color(0xFF1B5E20).copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getEventIcon(event.eventType),
                        contentDescription = null,
                        tint = if (isClinicianFact) Color(0xFF81C784) else CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Event Content Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.eventDate.ifBlank { "Undated" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )

                    if (event.isCustomUserEvent) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SOSRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Text(event.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                if (event.providerOrLocation.isNotBlank()) {
                    Text("Provider/Location: ${event.providerOrLocation}", fontSize = 12.sp, color = TextSecondary)
                }

                if (event.description.isNotBlank()) {
                    Text(event.description, fontSize = 12.sp, color = TextSecondary)
                }

                // Mandatory Fact Source Badge
                Surface(
                    color = if (isClinicianFact) Color(0xFF1B5E20).copy(alpha = 0.2f) else DarkSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (isClinicianFact) Color(0xFF81C784) else GlassBorder)
                ) {
                    Text(
                        text = if (isClinicianFact) "🏥 Clinician-Documented Fact" else "👤 Self-Reported / User Event",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isClinicianFact) Color(0xFF81C784) else TextSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCustomEventModal(
    onDismiss: () -> Unit,
    repository: HealthTimelineRepository,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("") }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var eventDate by remember { mutableStateOf(todayStr) }
    var description by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Add Personal Health Event", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title (e.g. Doctor Consultation)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("Provider / Location (e.g. Apollo Clinic)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    label = { Text("Event Date (YYYY-MM-DD)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Remarks") },
                    colors = futuristicTextFieldColors()
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = SOSRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMsg = "Please enter event title."
                        return@Button
                    }

                    isSaving = true
                    errorMsg = null

                    val event = TimelineEvent(
                        title = title.trim(),
                        eventDate = eventDate.trim(),
                        providerOrLocation = provider.trim(),
                        description = description.trim(),
                        isCustomUserEvent = true,
                        sourceType = TimelineSourceType.USER_REPORTED.name,
                        eventType = TimelineEventType.USER_EVENT.name
                    )

                    repository.saveCustomEvent(event) { success, err ->
                        isSaving = false
                        if (success) {
                            onSuccess()
                        } else {
                            errorMsg = err ?: "Failed to save event"
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Add to Timeline", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

private fun formatCategoryLabel(catName: String): String = when (catName) {
    "ALL" -> "All Events"
    "REPORT" -> "Reports 📑"
    "VACCINATION" -> "Vaccinations 💉"
    "MEDICINE" -> "Medicines 💊"
    "USER_EVENT" -> "User Events 👤"
    else -> catName
}

private fun formatDateFilterLabel(filter: TimelineDateFilter): String = when (filter) {
    TimelineDateFilter.ALL_TIME -> "All Time"
    TimelineDateFilter.LAST_30_DAYS -> "Last 30 Days"
    TimelineDateFilter.LAST_6_MONTHS -> "Last 6 Months"
    TimelineDateFilter.LAST_YEAR -> "Last 1 Year"
}

private fun getEventIcon(eventType: String) = when (eventType) {
    "REPORT" -> Icons.Default.FolderSpecial
    "VACCINATION" -> Icons.Default.Vaccines
    "MEDICINE" -> Icons.Default.MedicalServices
    else -> Icons.Default.Person
}
