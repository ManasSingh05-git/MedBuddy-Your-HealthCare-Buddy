package com.manas.medbuddy.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.appointments.AppointmentBookingType
import com.manas.medbuddy.appointments.AppointmentRepository
import com.manas.medbuddy.appointments.AppointmentStatus
import com.manas.medbuddy.appointments.DoctorAppointment
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.model.ReminderTime
import com.manas.medbuddy.model.ReminderType
import com.manas.medbuddy.reminder.ReminderScheduler
import com.manas.medbuddy.scanner.PrescriptionStorageManager
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppointmentOrganizerScreen(
    onBack: () -> Unit,
    repository: AppointmentRepository? = null
) {
    val context = LocalContext.current
    val repo = repository ?: remember { AppointmentRepository(context) }
    val scheduler = remember { ReminderScheduler(context) }
    val storageManager = remember { PrescriptionStorageManager(context) }

    var appointments by remember { mutableStateOf<List<DoctorAppointment>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Upcoming, 1: Calendar View, 2: Past/Completed, 3: Rescheduled/Cancelled

    var showAddModal by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<DoctorAppointment?>(null) }
    var rescheduleTarget by remember { mutableStateOf<DoctorAppointment?>(null) }
    var cancelTarget by remember { mutableStateOf<DoctorAppointment?>(null) }
    var deleteCandidate by remember { mutableStateOf<DoctorAppointment?>(null) }
    var previewRecord by remember { mutableStateOf<DoctorAppointment?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var selectedCalendarDate by remember { mutableStateOf(todayStr) }

    DisposableEffect(Unit) {
        val listener: ValueEventListener? = try {
            repo.listenToAppointments(
                onUpdate = { list ->
                    appointments = list
                    loading = false
                },
                onError = { loading = false }
            )
        } catch (_: Exception) {
            loading = false
            null
        }

        onDispose {
            if (listener != null) repo.removeListener(listener)
        }
    }

    val upcomingAppointments = remember(appointments, todayStr) {
        appointments.filter { it.appointmentDate >= todayStr && it.status != AppointmentStatus.CANCELLED.name && it.status != AppointmentStatus.COMPLETED.name }
    }

    val pastAppointments = remember(appointments, todayStr) {
        appointments.filter { it.appointmentDate < todayStr || it.status == AppointmentStatus.COMPLETED.name }
    }

    val archivedAppointments = remember(appointments) {
        appointments.filter { it.status == AppointmentStatus.RESCHEDULED.name || it.status == AppointmentStatus.CANCELLED.name }
    }

    val calendarDateAppointments = remember(appointments, selectedCalendarDate) {
        appointments.filter { it.appointmentDate == selectedCalendarDate }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Doctor Appointments 🏥", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = CyanPrimary)
                    }
                    IconButton(onClick = {
                        editingRecord = null
                        showAddModal = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Appointment", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface.copy(alpha = 0.9f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRecord = null
                    showAddModal = true
                },
                containerColor = CyanPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Appointment")
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
            // Segmented Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = CyanPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyanPrimary
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Upcoming (${upcomingAppointments.size})", fontSize = 11.sp) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Calendar 📅", fontSize = 11.sp) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Past (${pastAppointments.size})", fontSize = 11.sp) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Archived", fontSize = 11.sp) })
            }

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            } else if (selectedTab == 1) {
                // Calendar View
                CalendarViewSection(
                    selectedDate = selectedCalendarDate,
                    onDateSelect = { selectedCalendarDate = it },
                    appointmentsOnDate = calendarDateAppointments,
                    onReschedule = { rescheduleTarget = it },
                    onCancel = { cancelTarget = it },
                    onDelete = { deleteCandidate = it }
                )
            } else {
                val currentList = when (selectedTab) {
                    0 -> upcomingAppointments
                    2 -> pastAppointments
                    else -> archivedAppointments
                }

                if (currentList.isEmpty()) {
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
                            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(48.dp))
                            Text("No appointments found", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Record doctor consultations, clinic locations, and set automated reminders.", fontSize = 13.sp, color = TextSecondary)
                            Button(
                                onClick = {
                                    editingRecord = null
                                    showAddModal = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                            ) {
                                Text("+ Add Doctor Appointment", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(currentList, key = { it.id }) { appointment ->
                            AppointmentCard(
                                appointment = appointment,
                                onEdit = {
                                    editingRecord = appointment
                                    showAddModal = true
                                },
                                onReschedule = { rescheduleTarget = appointment },
                                onCancel = { cancelTarget = appointment },
                                onPreviewAttachment = { previewRecord = appointment },
                                onDelete = { deleteCandidate = appointment }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Modal
    if (showAddModal) {
        AddEditAppointmentModal(
            existingRecord = editingRecord,
            onDismiss = { showAddModal = false },
            repository = repo,
            scheduler = scheduler,
            onSuccess = {
                showAddModal = false
                Toast.makeText(context, "Appointment saved successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Reschedule Modal
    if (rescheduleTarget != null) {
        val target = rescheduleTarget!!
        RescheduleModal(
            appointment = target,
            onDismiss = { rescheduleTarget = null },
            repository = repo,
            scheduler = scheduler,
            onSuccess = {
                rescheduleTarget = null
                Toast.makeText(context, "Appointment rescheduled successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Cancel Modal
    if (cancelTarget != null) {
        val target = cancelTarget!!
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            containerColor = DarkSurface,
            title = { Text("Cancel Appointment", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to cancel appointment with '${target.doctorName}' on ${target.appointmentDate}? Scheduled reminders will be removed.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        repo.cancelAppointment(target) { _, _ -> }
                        cancelTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SOSRed)
                ) {
                    Text("Confirm Cancellation", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) {
                    Text("Keep Appointment", color = TextSecondary)
                }
            }
        )
    }

    // Delete Modal
    if (deleteCandidate != null) {
        val target = deleteCandidate!!
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            containerColor = DarkSurface,
            title = { Text("Delete Appointment Record", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete appointment record with '${target.doctorName}'? Attached slips will be deleted.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        repo.deleteAppointment(target) { _, _ -> }
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

    // Preview Modal
    if (previewRecord != null) {
        val target = previewRecord!!
        val bitmap = remember(target.id) {
            if (target.localAttachmentPath.isNotBlank()) {
                storageManager.loadBitmapFromFile(File(target.localAttachmentPath))
            } else null
        }

        AlertDialog(
            onDismissRequest = { previewRecord = null },
            containerColor = DarkSurface,
            title = { Text("Attached Referral / Slip", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Doctor: ${target.doctorName} (${target.specialty}) • ${target.appointmentDate}", color = TextSecondary, fontSize = 12.sp)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Attachment",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Text("Preview unavailable for this attachment.", color = SOSRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { previewRecord = null }, colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = DarkSurface,
            title = { Text("Doctor Appointment Guidelines", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Explicitly distinguishes Provider-Confirmed Bookings from Locally Saved Personal Notes.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Supports rescheduling and tracking previous appointment dates.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Offers configurable reminders (1 hour before, 1 day before, custom time) without duplicating alarms.", color = TextSecondary, fontSize = 13.sp)
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
private fun CalendarViewSection(
    selectedDate: String,
    onDateSelect: (String) -> Unit,
    appointmentsOnDate: List<DoctorAppointment>,
    onReschedule: (DoctorAppointment) -> Unit,
    onCancel: (DoctorAppointment) -> Unit,
    onDelete: (DoctorAppointment) -> Unit
) {
    var dateInput by remember { mutableStateOf(selectedDate) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📅 Calendar Date Selector", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = {
                        dateInput = it
                        if (it.length == 10) onDateSelect(it)
                    },
                    label = { Text("Select Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = futuristicTextFieldColors()
                )
            }
        }

        Text("Appointments on $selectedDate (${appointmentsOnDate.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

        if (appointmentsOnDate.isEmpty()) {
            Text("No appointments scheduled for $selectedDate.", color = TextSecondary, fontSize = 13.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(appointmentsOnDate, key = { it.id }) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onEdit = {},
                        onReschedule = { onReschedule(appointment) },
                        onCancel = { onCancel(appointment) },
                        onPreviewAttachment = {},
                        onDelete = { onDelete(appointment) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: DoctorAppointment,
    onEdit: () -> Unit,
    onReschedule: () -> Unit,
    onCancel: () -> Unit,
    onPreviewAttachment: () -> Unit,
    onDelete: () -> Unit
) {
    val isProviderConfirmed = appointment.bookingType == AppointmentBookingType.PROVIDER_CONFIRMED.name

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(appointment.doctorName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Specialty: ${appointment.specialty}", fontSize = 12.sp, color = TextSecondary)
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

            // Booking Type Fact Badge (Mandatory Requirement)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (isProviderConfirmed) Color(0xFF1B5E20).copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (isProviderConfirmed) Color(0xFF81C784) else CyanPrimary)
                ) {
                    Text(
                        text = if (isProviderConfirmed) "🏥 Provider-Confirmed Booking" else "👤 Locally Saved Appointment",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProviderConfirmed) Color(0xFF81C784) else CyanPrimary
                    )
                }

                // Status Badge
                Surface(
                    color = when (appointment.status) {
                        AppointmentStatus.COMPLETED.name -> Color(0xFF1B5E20).copy(alpha = 0.2f)
                        AppointmentStatus.RESCHEDULED.name -> Color(0xFFF57F17).copy(alpha = 0.2f)
                        AppointmentStatus.CANCELLED.name -> SOSRed.copy(alpha = 0.2f)
                        else -> DarkSurface
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Text(
                        text = when (appointment.status) {
                            AppointmentStatus.COMPLETED.name -> "✓ Completed"
                            AppointmentStatus.RESCHEDULED.name -> "🔄 Rescheduled"
                            AppointmentStatus.CANCELLED.name -> "❌ Cancelled"
                            else -> "⏰ Upcoming"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (appointment.status) {
                            AppointmentStatus.COMPLETED.name -> Color(0xFF81C784)
                            AppointmentStatus.RESCHEDULED.name -> Color(0xFFFFB74D)
                            AppointmentStatus.CANCELLED.name -> SOSRed
                            else -> TextPrimary
                        }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                Text("${appointment.appointmentDate} at ${appointment.appointmentTime}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }

            if (appointment.clinicLocation.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Text(appointment.clinicLocation, fontSize = 12.sp, color = TextSecondary)
                }
            }

            if (appointment.previousRescheduledDate.isNotBlank()) {
                Text("Previously scheduled for: ${appointment.previousRescheduledDate}", fontSize = 11.sp, color = Color(0xFFFFB74D))
            }

            if (appointment.hasAttachment) {
                Button(
                    onClick = onPreviewAttachment,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Slip / Attachment", fontSize = 12.sp, color = TextPrimary)
                }
            }

            if (appointment.status == AppointmentStatus.UPCOMING.name || appointment.status == AppointmentStatus.RESCHEDULED.name) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Button(
                        onClick = onReschedule,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Update, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reschedule", fontSize = 12.sp, color = TextPrimary)
                    }

                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = SOSRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontSize = 12.sp, color = SOSRed)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddEditAppointmentModal(
    existingRecord: DoctorAppointment?,
    onDismiss: () -> Unit,
    repository: AppointmentRepository,
    scheduler: ReminderScheduler,
    onSuccess: () -> Unit
) {
    var doctorName by remember { mutableStateOf(existingRecord?.doctorName ?: "") }
    var specialty by remember { mutableStateOf(existingRecord?.specialty ?: "General Medicine") }
    var clinicLocation by remember { mutableStateOf(existingRecord?.clinicLocation ?: "") }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var appointmentDate by remember { mutableStateOf(existingRecord?.appointmentDate ?: todayStr) }
    var appointmentTime by remember { mutableStateOf(existingRecord?.appointmentTime ?: "10:00 AM") }
    var bookingType by remember { mutableStateOf(existingRecord?.bookingType ?: AppointmentBookingType.LOCALLY_SAVED.name) }
    var reminderOffset by remember { mutableStateOf(existingRecord?.reminderOffset ?: "1 Hour Before") }
    var notes by remember { mutableStateOf(existingRecord?.notes ?: "") }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val specialties = listOf("General Medicine", "Cardiology", "Neurology", "Pediatrics", "Orthopedics", "Dermatology", "ENT", "Oncology")

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            errorMsg = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (existingRecord == null) "Schedule Doctor Appointment" else "Edit Appointment", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Doctor Name") },
                    placeholder = { Text("e.g. Dr. Mehta") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Text("Specialty:", fontSize = 12.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    specialties.forEach { spec ->
                        FilterChip(
                            selected = specialty == spec,
                            onClick = { specialty = spec },
                            label = { Text(spec) },
                            colors = futuristicFilterChipColors()
                        )
                    }
                }

                OutlinedTextField(
                    value = clinicLocation,
                    onValueChange = { clinicLocation = it },
                    label = { Text("Clinic / Hospital Address") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = appointmentDate,
                        onValueChange = { appointmentDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = futuristicTextFieldColors()
                    )

                    OutlinedTextField(
                        value = appointmentTime,
                        onValueChange = { appointmentTime = it },
                        label = { Text("Time (e.g. 10:30 AM)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = futuristicTextFieldColors()
                    )
                }

                Text("Booking Fact Source:", fontSize = 12.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = bookingType == AppointmentBookingType.LOCALLY_SAVED.name,
                        onClick = { bookingType = AppointmentBookingType.LOCALLY_SAVED.name },
                        label = { Text("Locally Saved") },
                        colors = futuristicFilterChipColors()
                    )
                    FilterChip(
                        selected = bookingType == AppointmentBookingType.PROVIDER_CONFIRMED.name,
                        onClick = { bookingType = AppointmentBookingType.PROVIDER_CONFIRMED.name },
                        label = { Text("Provider-Confirmed") },
                        colors = futuristicFilterChipColors()
                    )
                }

                Button(
                    onClick = { filePicker.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedUri == null && !(existingRecord?.hasAttachment == true)) "Attach Referral / Slip" else "Attachment Added ✓", color = CyanPrimary)
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Symptoms to Discuss") },
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
                    if (doctorName.isBlank()) {
                        errorMsg = "Please enter doctor name."
                        return@Button
                    }

                    isSaving = true
                    errorMsg = null

                    val record = DoctorAppointment(
                        id = existingRecord?.id ?: "",
                        doctorName = doctorName.trim(),
                        specialty = specialty,
                        clinicLocation = clinicLocation.trim(),
                        appointmentDate = appointmentDate.trim(),
                        appointmentTime = appointmentTime.trim(),
                        bookingType = bookingType,
                        notes = notes.trim(),
                        localAttachmentPath = existingRecord?.localAttachmentPath ?: "",
                        hasAttachment = existingRecord?.hasAttachment ?: false
                    )

                    repository.saveAppointment(record, selectedUri) { success, id ->
                        isSaving = false
                        if (success) {
                            val dummyMedForReminder = Medicine(
                                id = "appt_$id",
                                name = "Appointment with $doctorName ($specialty)",
                                dosage = clinicLocation,
                                reminderTimes = listOf(
                                    ReminderTime(id = "1", label = "Doctor Consultation", time = appointmentTime, type = ReminderType.CUSTOM.name)
                                )
                            )
                            scheduler.scheduleMedicineReminders(dummyMedForReminder)
                            onSuccess()
                        } else {
                            errorMsg = "Failed to save appointment"
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text(if (existingRecord == null) "Schedule" else "Update", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun RescheduleModal(
    appointment: DoctorAppointment,
    onDismiss: () -> Unit,
    repository: AppointmentRepository,
    scheduler: ReminderScheduler,
    onSuccess: () -> Unit
) {
    var newDate by remember { mutableStateOf(appointment.appointmentDate) }
    var newTime by remember { mutableStateOf(appointment.appointmentTime) }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Reschedule Appointment", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Doctor: ${appointment.doctorName} (${appointment.specialty})", color = TextSecondary, fontSize = 13.sp)

                OutlinedTextField(
                    value = newDate,
                    onValueChange = { newDate = it },
                    label = { Text("New Date (YYYY-MM-DD)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = newTime,
                    onValueChange = { newTime = it },
                    label = { Text("New Time (e.g. 11:00 AM)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSaving = true
                    repository.rescheduleAppointment(appointment, newDate.trim(), newTime.trim()) { success, _ ->
                        isSaving = false
                        if (success) {
                            val dummyMed = Medicine(
                                id = "appt_${appointment.id}",
                                name = "Rescheduled Appointment: ${appointment.doctorName}",
                                dosage = appointment.clinicLocation,
                                reminderTimes = listOf(
                                    ReminderTime(id = "1", label = "Consultation", time = newTime.trim(), type = ReminderType.CUSTOM.name)
                                )
                            )
                            scheduler.scheduleMedicineReminders(dummyMed)
                            onSuccess()
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Confirm Reschedule", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
