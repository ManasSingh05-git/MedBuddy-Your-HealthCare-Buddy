package com.manas.medbuddy.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Visibility
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
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.model.ReminderTime
import com.manas.medbuddy.model.ReminderType
import com.manas.medbuddy.reminder.ReminderScheduler
import com.manas.medbuddy.scanner.PrescriptionStorageManager
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.vaccination.VaccinationRecord
import com.manas.medbuddy.vaccination.VaccinationRepository
import com.manas.medbuddy.vaccination.VaccinationStatus
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationManagerScreen(
    onBack: () -> Unit,
    repository: VaccinationRepository? = null
) {
    val context = LocalContext.current
    val repo = repository ?: remember { VaccinationRepository(context) }
    val scheduler = remember { ReminderScheduler(context) }
    val storageManager = remember { PrescriptionStorageManager(context) }

    var records by remember { mutableStateOf<List<VaccinationRecord>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: ALL, 1: COMPLETED, 2: PLANNED, 3: UNVERIFIED

    var showAddModal by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<VaccinationRecord?>(null) }
    var deleteCandidate by remember { mutableStateOf<VaccinationRecord?>(null) }
    var previewRecord by remember { mutableStateOf<VaccinationRecord?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener: ValueEventListener? = try {
            repo.listenToVaccinations(
                onUpdate = { list ->
                    records = list
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

    val filteredRecords = remember(records, selectedTab) {
        when (selectedTab) {
            1 -> records.filter { it.status == VaccinationStatus.COMPLETED.name }
            2 -> records.filter { it.status == VaccinationStatus.PLANNED.name }
            3 -> records.filter { it.status == VaccinationStatus.UNVERIFIED.name }
            else -> records
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Vaccination Records 💉", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                        Icon(Icons.Default.Add, contentDescription = "Add Vaccination", tint = CyanPrimary)
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
                Icon(Icons.Default.Add, contentDescription = "Add Vaccination")
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
            // Segmented Status Tabs
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
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("All (${records.size})", fontSize = 12.sp) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Completed", fontSize = 12.sp) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Planned", fontSize = 12.sp) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Unverified", fontSize = 12.sp) })
            }

            // List View
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            } else if (filteredRecords.isEmpty()) {
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
                        Icon(Icons.Default.Vaccines, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(48.dp))
                        Text("No vaccination records found", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Record completed, planned, or unverified vaccines and securely attach certificates.", fontSize = 13.sp, color = TextSecondary)
                        Button(
                            onClick = {
                                editingRecord = null
                                showAddModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("+ Add Vaccination Record", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { record ->
                        VaccinationCard(
                            record = record,
                            onEdit = {
                                editingRecord = record
                                showAddModal = true
                            },
                            onPreviewCertificate = { previewRecord = record },
                            onDelete = { deleteCandidate = record }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Modal Dialog
    if (showAddModal) {
        AddEditVaccinationModal(
            existingRecord = editingRecord,
            onDismiss = { showAddModal = false },
            repository = repo,
            scheduler = scheduler,
            onSuccess = {
                showAddModal = false
                Toast.makeText(context, "Vaccination record saved successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Preview Certificate Modal
    if (previewRecord != null) {
        val target = previewRecord!!
        val bitmap = remember(target.id) {
            if (target.localCertificatePath.isNotBlank()) {
                storageManager.loadBitmapFromFile(File(target.localCertificatePath))
            } else null
        }

        AlertDialog(
            onDismissRequest = { previewRecord = null },
            containerColor = DarkSurface,
            title = { Text("Certificate Preview", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${target.vaccineName} (${target.doseNumber}) • ${target.providerName}", color = TextSecondary, fontSize = 12.sp)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Certificate",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Text("No image preview available for this attachment.", color = SOSRed, fontSize = 12.sp)
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

    // Delete Candidate Dialog
    if (deleteCandidate != null) {
        val target = deleteCandidate!!
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            containerColor = DarkSurface,
            title = { Text("Delete Vaccination Record", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${target.vaccineName}' (${target.doseNumber})? Attached certificates and scheduled reminders will be removed.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        repo.deleteVaccinationRecord(target) { _, _ -> }
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
            title = { Text("Vaccination Record Guidelines", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Chronologically records completed, planned, and unverified vaccine doses.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Certificates are stored securely in app private storage.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Configurable reminders alert you on custom planned booster dates.", color = TextSecondary, fontSize = 13.sp)
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
private fun VaccinationCard(
    record: VaccinationRecord,
    onEdit: () -> Unit,
    onPreviewCertificate: () -> Unit,
    onDelete: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Vaccines, contentDescription = null, tint = CyanPrimary)
                    Column {
                        Text(record.vaccineName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${record.doseNumber} • Date: ${record.date.ifBlank { "Unspecified" }}", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SOSRed)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Status Badge
                Surface(
                    color = when (record.status) {
                        VaccinationStatus.COMPLETED.name -> Color(0xFF1B5E20).copy(alpha = 0.2f)
                        VaccinationStatus.PLANNED.name -> CyanPrimary.copy(alpha = 0.15f)
                        else -> Color(0xFFF57F17).copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        when (record.status) {
                            VaccinationStatus.COMPLETED.name -> Color(0xFF81C784)
                            VaccinationStatus.PLANNED.name -> CyanPrimary
                            else -> Color(0xFFFFB74D)
                        }
                    )
                ) {
                    Text(
                        text = when (record.status) {
                            VaccinationStatus.COMPLETED.name -> "✓ COMPLETED"
                            VaccinationStatus.PLANNED.name -> "⏰ PLANNED"
                            else -> "⚠️ UNVERIFIED"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (record.status) {
                            VaccinationStatus.COMPLETED.name -> Color(0xFF81C784)
                            VaccinationStatus.PLANNED.name -> CyanPrimary
                            else -> Color(0xFFFFB74D)
                        }
                    )
                }

                if (record.providerName.isNotBlank()) {
                    Text("• ${record.providerName}", fontSize = 12.sp, color = TextSecondary)
                }
            }

            if (record.hasCertificate) {
                Button(
                    onClick = onPreviewCertificate,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Certificate", fontSize = 12.sp, color = TextPrimary)
                }
            }

            if (record.reminderEnabled && record.reminderDate.isNotBlank()) {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                        Text("Reminder set for ${record.reminderDate} at ${record.reminderTime}", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            if (record.notes.isNotBlank()) {
                Text("Notes: ${record.notes}", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddEditVaccinationModal(
    existingRecord: VaccinationRecord?,
    onDismiss: () -> Unit,
    repository: VaccinationRepository,
    scheduler: ReminderScheduler,
    onSuccess: () -> Unit
) {
    var vaccineName by remember { mutableStateOf(existingRecord?.vaccineName ?: "") }
    var doseNumber by remember { mutableStateOf(existingRecord?.doseNumber ?: "Dose 1") }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(existingRecord?.date ?: todayStr) }
    var providerName by remember { mutableStateOf(existingRecord?.providerName ?: "") }
    var selectedStatus by remember { mutableStateOf(existingRecord?.status ?: VaccinationStatus.COMPLETED.name) }
    var notes by remember { mutableStateOf(existingRecord?.notes ?: "") }

    var reminderEnabled by remember { mutableStateOf(existingRecord?.reminderEnabled ?: false) }
    var reminderDate by remember { mutableStateOf(existingRecord?.reminderDate ?: todayStr) }
    var reminderTime by remember { mutableStateOf(existingRecord?.reminderTime ?: "09:00 AM") }

    var selectedCertificateUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedCertificateUri = uri
            errorMsg = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (existingRecord == null) "Add Vaccination Record" else "Edit Vaccination Record", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = vaccineName,
                    onValueChange = { vaccineName = it },
                    label = { Text("Vaccine Name (e.g. Covishield, Flu Shot)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = doseNumber,
                    onValueChange = { doseNumber = it },
                    label = { Text("Dose Number (e.g. Dose 1, Dose 2, Booster)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date Administered / Scheduled (YYYY-MM-DD)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = providerName,
                    onValueChange = { providerName = it },
                    label = { Text("Healthcare Provider / Hospital") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Text("Status:", fontSize = 12.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(VaccinationStatus.COMPLETED, VaccinationStatus.PLANNED, VaccinationStatus.UNVERIFIED).forEach { st ->
                        FilterChip(
                            selected = selectedStatus == st.name,
                            onClick = { selectedStatus = st.name },
                            label = { Text(st.name) },
                            colors = futuristicFilterChipColors()
                        )
                    }
                }

                Button(
                    onClick = { filePicker.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedCertificateUri == null && !(existingRecord?.hasCertificate == true)) "Attach Certificate (PDF / Image)" else "Certificate Attached ✓", color = CyanPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Configurable Reminder for Upcoming Dose", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                    )
                }

                if (reminderEnabled) {
                    OutlinedTextField(
                        value = reminderDate,
                        onValueChange = { reminderDate = it },
                        label = { Text("Reminder Date (YYYY-MM-DD)") },
                        singleLine = true,
                        colors = futuristicTextFieldColors()
                    )

                    OutlinedTextField(
                        value = reminderTime,
                        onValueChange = { reminderTime = it },
                        label = { Text("Reminder Time (e.g. 09:00 AM)") },
                        singleLine = true,
                        colors = futuristicTextFieldColors()
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Medical Remarks") },
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
                    if (vaccineName.isBlank()) {
                        errorMsg = "Please enter vaccine name."
                        return@Button
                    }

                    isSaving = true
                    errorMsg = null

                    val record = VaccinationRecord(
                        id = existingRecord?.id ?: "",
                        vaccineName = vaccineName.trim(),
                        doseNumber = doseNumber.trim(),
                        date = date.trim(),
                        providerName = providerName.trim(),
                        status = selectedStatus,
                        reminderEnabled = reminderEnabled,
                        reminderDate = reminderDate.trim(),
                        reminderTime = reminderTime.trim(),
                        notes = notes.trim(),
                        localCertificatePath = existingRecord?.localCertificatePath ?: "",
                        hasCertificate = existingRecord?.hasCertificate ?: false
                    )

                    repository.saveVaccinationRecord(record, selectedCertificateUri) { success, id ->
                        isSaving = false
                        if (success) {
                            if (reminderEnabled && reminderDate.isNotBlank()) {
                                val dummyMedForReminder = Medicine(
                                    id = "vac_$id",
                                    name = "Vaccination Dose: $vaccineName ($doseNumber)",
                                    dosage = providerName,
                                    reminderTimes = listOf(
                                        ReminderTime(id = "1", label = "Vaccine Booster", time = reminderTime, type = ReminderType.CUSTOM.name)
                                    )
                                )
                                scheduler.scheduleMedicineReminders(dummyMedForReminder)
                            }
                            onSuccess()
                        } else {
                            errorMsg = "Failed to save vaccination record"
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text(if (existingRecord == null) "Save Record" else "Update Record", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
