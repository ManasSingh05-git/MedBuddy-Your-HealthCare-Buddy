package com.manas.medbuddy.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.appointments.AppointmentRepository
import com.manas.medbuddy.appointments.DoctorAppointment
import com.manas.medbuddy.checklist.ChecklistItem
import com.manas.medbuddy.checklist.ChecklistItemCategory
import com.manas.medbuddy.checklist.ChecklistRepository
import com.manas.medbuddy.checklist.ChecklistTemplateProvider
import com.manas.medbuddy.checklist.HospitalVisitChecklist
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HospitalChecklistScreen(
    onBack: () -> Unit,
    checklistRepository: ChecklistRepository? = null,
    appointmentRepository: AppointmentRepository? = null
) {
    val context = LocalContext.current
    val repo = checklistRepository ?: remember { ChecklistRepository(context) }
    val apptRepo = appointmentRepository ?: remember { AppointmentRepository(context) }
    val templateProvider = remember { ChecklistTemplateProvider() }

    var checklists by remember { mutableStateOf<List<HospitalVisitChecklist>>(emptyList()) }
    var appointments by remember { mutableStateOf<List<DoctorAppointment>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var showAddModal by remember { mutableStateOf(false) }
    var editingChecklist by remember { mutableStateOf<HospitalVisitChecklist?>(null) }
    var addItemTarget by remember { mutableStateOf<HospitalVisitChecklist?>(null) }
    var deleteCandidate by remember { mutableStateOf<HospitalVisitChecklist?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val checklistListener: ValueEventListener? = try {
            repo.listenToChecklists(
                onUpdate = { list ->
                    checklists = list
                    loading = false
                },
                onError = { loading = false }
            )
        } catch (_: Exception) {
            loading = false
            null
        }

        val apptListener: ValueEventListener? = try {
            apptRepo.listenToAppointments(
                onUpdate = { list -> appointments = list },
                onError = {}
            )
        } catch (_: Exception) { null }

        onDispose {
            if (checklistListener != null) repo.removeListener(checklistListener)
            if (apptListener != null) apptRepo.removeListener(apptListener)
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Hospital Visit Checklist 📋", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                        editingChecklist = null
                        showAddModal = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Checklist", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface.copy(alpha = 0.9f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingChecklist = null
                    showAddModal = true
                },
                containerColor = CyanPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Checklist")
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
            // Template Fast-Start Row
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🚀 Quick Template Generators", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val templateItems = templateProvider.getOutpatientConsultationTemplate()
                                val checklist = HospitalVisitChecklist(
                                    title = "Doctor Consultation Checklist",
                                    items = templateItems,
                                    completionPercentage = repo.calculateProgress(templateItems)
                                )
                                repo.saveChecklist(checklist) { success, _ ->
                                    if (success) Toast.makeText(context, "Consultation checklist created!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ OPD Consultation", fontSize = 11.sp, color = TextPrimary)
                        }

                        Button(
                            onClick = {
                                val templateItems = templateProvider.getHospitalAdmissionTemplate()
                                val checklist = HospitalVisitChecklist(
                                    title = "Hospital Surgery/Admission Checklist",
                                    items = templateItems,
                                    completionPercentage = repo.calculateProgress(templateItems)
                                )
                                repo.saveChecklist(checklist) { success, _ ->
                                    if (success) Toast.makeText(context, "Admission checklist created!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Admission / Procedure", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }

            // List View
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            } else if (checklists.isEmpty()) {
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
                        Icon(Icons.Default.PlaylistAddCheck, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(48.dp))
                        Text("No visit checklists created", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Prepare document, report, prescription, and doctor question checklists for upcoming appointments.", fontSize = 13.sp, color = TextSecondary)
                        Button(
                            onClick = {
                                editingChecklist = null
                                showAddModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("+ Create Visit Checklist", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(checklists, key = { it.id }) { checklist ->
                        val linkedAppt = appointments.find { it.id == checklist.linkedAppointmentId }

                        ChecklistCard(
                            checklist = checklist,
                            linkedAppt = linkedAppt,
                            onToggleItem = { itemId ->
                                repo.toggleItemCompletion(checklist, itemId) { _, _ -> }
                            },
                            onRemoveItem = { itemId ->
                                repo.removeItemFromChecklist(checklist, itemId) { _, _ -> }
                            },
                            onAddItem = { addItemTarget = checklist },
                            onEdit = {
                                editingChecklist = checklist
                                showAddModal = true
                            },
                            onDelete = { deleteCandidate = checklist }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Modal Dialog
    if (showAddModal) {
        AddEditChecklistModal(
            existingChecklist = editingChecklist,
            appointments = appointments,
            onDismiss = { showAddModal = false },
            repository = repo,
            templateProvider = templateProvider,
            onSuccess = {
                showAddModal = false
                Toast.makeText(context, "Checklist saved successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Add Custom Item Dialog
    if (addItemTarget != null) {
        val target = addItemTarget!!
        AddCustomItemModal(
            checklist = target,
            onDismiss = { addItemTarget = null },
            repository = repo,
            onSuccess = {
                addItemTarget = null
                Toast.makeText(context, "Custom item added!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Modal
    if (deleteCandidate != null) {
        val target = deleteCandidate!!
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            containerColor = DarkSurface,
            title = { Text("Delete Visit Checklist", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete checklist '${target.title}'? All checklist items will be removed.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        repo.deleteChecklist(target.id) { _, _ -> }
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
            title = { Text("Hospital Visit Checklist Guidelines", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Create customizable checklists for outpatient visits, admissions, and procedures.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Links directly to scheduled doctor appointments.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Groups items into Reports, Prescriptions, Documents, and Doctor Questions.", color = TextSecondary, fontSize = 13.sp)
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
private fun ChecklistCard(
    checklist: HospitalVisitChecklist,
    linkedAppt: DoctorAppointment?,
    onToggleItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onAddItem: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val completedCount = checklist.items.count { it.isCompleted }
    val totalCount = checklist.items.size
    val progress = checklist.completionPercentage / 100f

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
                    Text(checklist.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    if (checklist.doctorOrHospitalName.isNotBlank() || checklist.visitDate.isNotBlank()) {
                        Text("${checklist.doctorOrHospitalName} • ${checklist.visitDate}", fontSize = 12.sp, color = TextSecondary)
                    }
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

            // Linked Appointment Badge
            if (linkedAppt != null) {
                Surface(
                    color = Color(0xFF1B5E20).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF81C784))
                ) {
                    Text(
                        text = "🏥 Linked Appointment: ${linkedAppt.doctorName} (${linkedAppt.appointmentDate})",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81C784)
                    )
                }
            }

            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Completion Progress: $completedCount / $totalCount Items", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("${checklist.completionPercentage.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                }
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (checklist.completionPercentage >= 100f) Color(0xFF81C784) else CyanPrimary,
                    trackColor = DarkSurface
                )
            }

            // Checklist Items
            if (checklist.items.isEmpty()) {
                Text("No items added yet. Tap '+ Add Item' to insert custom items.", fontSize = 12.sp, color = TextSecondary)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    checklist.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleItem(item.id) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = item.isCompleted,
                                    onCheckedChange = { onToggleItem(item.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                                )
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    color = if (item.isCompleted) TextSecondary else TextPrimary,
                                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }

                            IconButton(
                                onClick = { onRemoveItem(item.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove Item", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onAddItem,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ Add Custom Checklist Item", fontSize = 12.sp, color = TextPrimary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddEditChecklistModal(
    existingChecklist: HospitalVisitChecklist?,
    appointments: List<DoctorAppointment>,
    onDismiss: () -> Unit,
    repository: ChecklistRepository,
    templateProvider: ChecklistTemplateProvider,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf(existingChecklist?.title ?: "Hospital Visit Checklist") }
    var selectedApptId by remember { mutableStateOf(existingChecklist?.linkedAppointmentId ?: "") }
    var doctorOrHospitalName by remember { mutableStateOf(existingChecklist?.doctorOrHospitalName ?: "") }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var visitDate by remember { mutableStateOf(existingChecklist?.visitDate ?: todayStr) }
    var applyTemplateType by remember { mutableStateOf("NONE") } // NONE, OPD, ADMISSION

    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (existingChecklist == null) "Create Visit Checklist" else "Edit Checklist Details", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                    label = { Text("Checklist Title") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                if (appointments.isNotEmpty()) {
                    Text("Link Scheduled Appointment:", fontSize = 12.sp, color = TextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        appointments.forEach { appt ->
                            FilterChip(
                                selected = selectedApptId == appt.id,
                                onClick = {
                                    selectedApptId = appt.id
                                    doctorOrHospitalName = appt.doctorName
                                    visitDate = appt.appointmentDate
                                },
                                label = { Text("${appt.doctorName} (${appt.appointmentDate})") },
                                colors = futuristicFilterChipColors()
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = doctorOrHospitalName,
                    onValueChange = { doctorOrHospitalName = it },
                    label = { Text("Doctor / Hospital Name") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = visitDate,
                    onValueChange = { visitDate = it },
                    label = { Text("Visit Date (YYYY-MM-DD)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                if (existingChecklist == null) {
                    Text("Pre-populate Items from Template:", fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = applyTemplateType == "OPD",
                            onClick = { applyTemplateType = "OPD" },
                            label = { Text("Outpatient Visit") },
                            colors = futuristicFilterChipColors()
                        )
                        FilterChip(
                            selected = applyTemplateType == "ADMISSION",
                            onClick = { applyTemplateType = "ADMISSION" },
                            label = { Text("Surgery/Admission") },
                            colors = futuristicFilterChipColors()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSaving = true
                    val baseItems = existingChecklist?.items ?: emptyList()
                    val templateItems = when (applyTemplateType) {
                        "OPD" -> templateProvider.getOutpatientConsultationTemplate()
                        "ADMISSION" -> templateProvider.getHospitalAdmissionTemplate()
                        else -> emptyList()
                    }

                    val finalItems = if (existingChecklist == null && templateItems.isNotEmpty()) templateItems else baseItems

                    val checklist = HospitalVisitChecklist(
                        id = existingChecklist?.id ?: "",
                        title = title.trim(),
                        linkedAppointmentId = selectedApptId,
                        doctorOrHospitalName = doctorOrHospitalName.trim(),
                        visitDate = visitDate.trim(),
                        items = finalItems
                    )

                    repository.saveChecklist(checklist) { success, _ ->
                        isSaving = false
                        if (success) onSuccess()
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text(if (existingChecklist == null) "Create" else "Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCustomItemModal(
    checklist: HospitalVisitChecklist,
    onDismiss: () -> Unit,
    repository: ChecklistRepository,
    onSuccess: () -> Unit
) {
    var itemTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ChecklistItemCategory.REPORTS.name) }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Add Custom Checklist Item", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = itemTitle,
                    onValueChange = { itemTitle = it },
                    label = { Text("Item Title") },
                    placeholder = { Text("e.g. Bring X-Ray CD, Doctor Question") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Text("Category:", fontSize = 12.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChecklistItemCategory.entries.toTypedArray().forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(cat.name.lowercase(Locale.getDefault()).replace("_", " ").capitalize(Locale.getDefault())) },
                            colors = futuristicFilterChipColors()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemTitle.isBlank()) return@Button
                    isSaving = true
                    val newItem = ChecklistItem(title = itemTitle.trim(), category = selectedCategory)
                    repository.addItemToChecklist(checklist, newItem) { success, _ ->
                        isSaving = false
                        if (success) onSuccess()
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Add Item", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
