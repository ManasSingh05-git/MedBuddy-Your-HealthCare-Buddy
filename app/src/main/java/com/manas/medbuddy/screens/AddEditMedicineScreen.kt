package com.manas.medbuddy.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.data.MedicineRepository
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.model.ReminderTime
import com.manas.medbuddy.model.ReminderType
import com.manas.medbuddy.model.ScheduleType
import com.manas.medbuddy.reminder.ReminderScheduler
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicineScreen(
    medicineId: String? = null,
    onBack: () -> Unit,
    repository: MedicineRepository = remember { MedicineRepository() }
) {
    val context = LocalContext.current
    val scheduler = remember { ReminderScheduler(context) }

    val isEditing = !medicineId.isNullOrBlank()

    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var startDate by remember { mutableStateOf(todayStr) }
    var endDate by remember { mutableStateOf("Ongoing") }

    var scheduleType by remember { mutableStateOf(ScheduleType.DAILY.name) }
    var selectedDays by remember { mutableStateOf(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")) }

    var reminderTimes by remember { mutableStateOf<List<ReminderTime>>(emptyList()) }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCustomTimeDialog by remember { mutableStateOf(false) }

    val daysOfWeek = remember { listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") }

    // Fetch existing medicine if editing
    LaunchedEffect(medicineId) {
        if (!medicineId.isNullOrBlank()) {
            repository.getMedicinesReference()?.child(medicineId)?.get()?.addOnSuccessListener { snapshot ->
                val med = snapshot.getValue(Medicine::class.java)
                if (med != null) {
                    name = med.name
                    dosage = med.dosage
                    instructions = med.instructions
                    if (med.startDate.isNotBlank()) startDate = med.startDate
                    if (med.endDate.isNotBlank()) endDate = med.endDate
                    scheduleType = med.scheduleType
                    if (med.selectedDays.isNotEmpty()) selectedDays = med.selectedDays
                    reminderTimes = med.reminderTimes
                }
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit Medicine" else "Add New Medicine",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyanPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface.copy(alpha = 0.9f))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Medicine Details
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Medicine Details", color = CyanPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medicine Name") },
                        placeholder = { Text("e.g. Paracetamol, Amoxicillin") },
                        leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null, tint = CyanPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = futuristicTextFieldColors()
                    )

                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage") },
                        placeholder = { Text("e.g. 500 mg, 1 Tablet, 10 ml") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = futuristicTextFieldColors()
                    )

                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Instructions") },
                        placeholder = { Text("e.g. Take after breakfast with water") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = futuristicTextFieldColors()
                    )
                }
            }

            // Section 2: Dates
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Duration", color = CyanPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date") },
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = CyanPrimary) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = futuristicTextFieldColors()
                        )

                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = futuristicTextFieldColors()
                        )
                    }
                }
            }

            // Section 3: Schedule Pattern (Daily vs Selected Days)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Schedule Type", color = CyanPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(
                            selected = scheduleType == ScheduleType.DAILY.name,
                            onClick = { scheduleType = ScheduleType.DAILY.name },
                            label = { Text("Daily") },
                            colors = futuristicFilterChipColors()
                        )
                        FilterChip(
                            selected = scheduleType == ScheduleType.SELECTED_DAYS.name,
                            onClick = { scheduleType = ScheduleType.SELECTED_DAYS.name },
                            label = { Text("Selected Days") },
                            colors = futuristicFilterChipColors()
                        )
                    }

                    if (scheduleType == ScheduleType.SELECTED_DAYS.name) {
                        Text("Select Active Days:", fontSize = 14.sp, color = TextSecondary)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            daysOfWeek.forEach { day ->
                                val selected = selectedDays.contains(day)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedDays = if (selected) {
                                            selectedDays - day
                                        } else {
                                            selectedDays + day
                                        }
                                    },
                                    label = { Text(day) },
                                    colors = futuristicFilterChipColors()
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Multiple Reminder Times
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Reminder Times", color = CyanPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    Text("Quick Presets:", fontSize = 14.sp, color = TextSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AssistChip(
                            onClick = {
                                if (reminderTimes.none { it.type == ReminderType.MORNING.name }) {
                                    reminderTimes = reminderTimes + ReminderTime(
                                        id = UUID.randomUUID().toString(),
                                        label = "Morning",
                                        time = "08:00 AM",
                                        type = ReminderType.MORNING.name
                                    )
                                }
                            },
                            label = { Text("🌅 Morning (08:00 AM)") },
                            colors = futuristicAssistChipColors()
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AssistChip(
                            onClick = {
                                if (reminderTimes.none { it.type == ReminderType.AFTERNOON.name }) {
                                    reminderTimes = reminderTimes + ReminderTime(
                                        id = UUID.randomUUID().toString(),
                                        label = "Afternoon",
                                        time = "01:00 PM",
                                        type = ReminderType.AFTERNOON.name
                                    )
                                }
                            },
                            label = { Text("☀️ Afternoon (01:00 PM)") },
                            colors = futuristicAssistChipColors()
                        )
                        AssistChip(
                            onClick = {
                                if (reminderTimes.none { it.type == ReminderType.EVENING.name }) {
                                    reminderTimes = reminderTimes + ReminderTime(
                                        id = UUID.randomUUID().toString(),
                                        label = "Evening",
                                        time = "08:00 PM",
                                        type = ReminderType.EVENING.name
                                    )
                                }
                            },
                            label = { Text("🌙 Evening (08:00 PM)") },
                            colors = futuristicAssistChipColors()
                        )
                    }

                    Button(
                        onClick = { showCustomTimeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ Add Custom Reminder Time", color = CyanPrimary)
                    }

                    if (reminderTimes.isNotEmpty()) {
                        Text("Active Schedules:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            reminderTimes.forEach { reminder ->
                                Surface(
                                    color = DarkSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, GlassBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = CyanPrimary)
                                            Column {
                                                Text(reminder.label, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                Text(reminder.time, fontSize = 12.sp, color = TextSecondary)
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                reminderTimes = reminderTimes.filter { it.id != reminder.id }
                                            }
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = SOSRed)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text("No reminder times added yet. Select a preset or custom time.", color = SOSRed, fontSize = 12.sp)
                    }
                }
            }

            if (errorMessage != null) {
                Text(errorMessage!!, color = SOSRed, fontWeight = FontWeight.Medium)
            }

            // Save Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter medicine name"
                        return@Button
                    }
                    if (dosage.isBlank()) {
                        errorMessage = "Please enter dosage"
                        return@Button
                    }
                    if (reminderTimes.isEmpty()) {
                        errorMessage = "Please add at least one reminder schedule"
                        return@Button
                    }

                    isSaving = true
                    errorMessage = null

                    val newMedicine = Medicine(
                        id = medicineId ?: "",
                        name = name.trim(),
                        dosage = dosage.trim(),
                        instructions = instructions.trim(),
                        startDate = startDate.trim(),
                        endDate = endDate.trim(),
                        scheduleType = scheduleType,
                        selectedDays = if (scheduleType == ScheduleType.SELECTED_DAYS.name) selectedDays else emptyList(),
                        reminderTimes = reminderTimes
                    )

                    repository.saveMedicine(newMedicine) { success, id ->
                        isSaving = false
                        if (success) {
                            val savedMed = newMedicine.copy(id = id ?: newMedicine.id)
                            scheduler.scheduleMedicineReminders(savedMed)
                            onBack()
                        } else {
                            errorMessage = "Failed to save medicine to database."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(CyanPrimary, BlueGradient, VioletGradient)), shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (isEditing) "Update Medicine" else "Save Medicine", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    if (showCustomTimeDialog) {
        CustomTimeDialog(
            onDismiss = { showCustomTimeDialog = false },
            onTimeSelected = { label, time ->
                reminderTimes = reminderTimes + ReminderTime(
                    id = UUID.randomUUID().toString(),
                    label = label,
                    time = time,
                    type = ReminderType.CUSTOM.name
                )
                showCustomTimeDialog = false
            }
        )
    }
}

@Composable
fun CustomTimeDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (label: String, time: String) -> Unit
) {
    var label by remember { mutableStateOf("Custom Time") }
    var hour by remember { mutableStateOf("09") }
    var minute by remember { mutableStateOf("00") }
    var amPm by remember { mutableStateOf("AM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Add Custom Reminder", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (e.g. Night, Pre-meal)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hour,
                        onValueChange = { if (it.length <= 2) hour = it },
                        label = { Text("Hour") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = futuristicTextFieldColors()
                    )
                    Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = minute,
                        onValueChange = { if (it.length <= 2) minute = it },
                        label = { Text("Minute") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = futuristicTextFieldColors()
                    )
                    Button(
                        onClick = { amPm = if (amPm == "AM") "PM" else "AM" },
                        colors = ButtonDefaults.buttonColors(containerColor = GlassBackground),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(amPm, color = CyanPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val formattedHour = hour.padStart(2, '0')
                    val formattedMinute = minute.padStart(2, '0')
                    val timeString = "$formattedHour:$formattedMinute $amPm"
                    onTimeSelected(label.ifBlank { "Custom" }, timeString)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
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
fun futuristicTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanPrimary,
    unfocusedBorderColor = GlassBorder,
    focusedLabelColor = CyanPrimary,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = CyanPrimary
)

@Composable
fun futuristicFilterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = CyanPrimary,
    selectedLabelColor = Color.Black,
    containerColor = DarkSurface,
    labelColor = TextSecondary
)

@Composable
fun futuristicAssistChipColors() = AssistChipDefaults.assistChipColors(
    containerColor = DarkSurface,
    labelColor = TextPrimary
)
