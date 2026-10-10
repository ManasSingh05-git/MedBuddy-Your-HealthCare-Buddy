package com.manas.medbuddy.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.manas.medbuddy.model.DoseLog
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.reminder.ReminderScheduler
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListScreen(
    onBack: () -> Unit,
    onAddMedicine: () -> Unit,
    onEditMedicine: (String) -> Unit,
    repository: MedicineRepository = remember { MedicineRepository() }
) {
    val context = LocalContext.current
    val scheduler = remember { ReminderScheduler(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All Medicines, 1: Today's Schedule, 2: Dose History
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var doseLogs by remember { mutableStateOf<List<DoseLog>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var medicineToDelete by remember { mutableStateOf<Medicine?>(null) }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    DisposableEffect(Unit) {
        val medListener: ValueEventListener = repository.listenToMedicines(
            onUpdate = { list ->
                medicines = list
                loading = false
            },
            onError = { loading = false }
        )
        val logListener: ValueEventListener = repository.listenToDoseLogs(
            onUpdate = { logs -> doseLogs = logs }
        )

        onDispose {
            repository.removeMedicinesListener(medListener)
            repository.removeDoseLogsListener(logListener)
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Medicine Management", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onAddMedicine) {
                        Icon(Icons.Default.Add, contentDescription = "Add Medicine", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface.copy(alpha = 0.9f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicine,
                containerColor = CyanPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine")
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
            // Segmented Tab Selector
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
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Medicines (${medicines.size})", fontWeight = FontWeight.Medium) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Today's Schedule", fontWeight = FontWeight.Medium) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("History", fontWeight = FontWeight.Medium) }
                )
            }

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            } else {
                when (selectedTab) {
                    0 -> AllMedicinesTab(
                        medicines = medicines,
                        onEdit = { onEditMedicine(it.id) },
                        onDeleteRequest = { medicineToDelete = it }
                    )
                    1 -> TodayScheduleTab(
                        medicines = medicines,
                        doseLogs = doseLogs,
                        todayStr = todayStr,
                        onLogStatus = { medicine, time, status ->
                            repository.logDoseStatus(medicine, time, status, todayStr)
                        }
                    )
                    2 -> DoseHistoryTab(doseLogs = doseLogs)
                }
            }
        }
    }

    if (medicineToDelete != null) {
        val targetMed = medicineToDelete!!
        AlertDialog(
            onDismissRequest = { medicineToDelete = null },
            containerColor = DarkSurface,
            title = { Text("Delete Medicine", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${targetMed.name}'? This action cannot be undone.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        scheduler.cancelMedicineReminders(targetMed)
                        repository.deleteMedicine(targetMed.id) { _, _ -> }
                        medicineToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SOSRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { medicineToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun AllMedicinesTab(
    medicines: List<Medicine>,
    onEdit: (Medicine) -> Unit,
    onDeleteRequest: (Medicine) -> Unit
) {
    if (medicines.isEmpty()) {
        EmptyStateView("No medicines added yet", "Tap the + button to add your first medicine.")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(medicines, key = { it.id }) { medicine ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = CyanPrimary)
                                Text(medicine.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Row {
                                IconButton(onClick = { onEdit(medicine) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyanPrimary)
                                }
                                IconButton(onClick = { onDeleteRequest(medicine) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SOSRed)
                                }
                            }
                        }

                        Text("Dosage: ${medicine.dosage}", color = TextSecondary, fontSize = 14.sp)

                        if (medicine.instructions.isNotBlank()) {
                            Text("Instructions: ${medicine.instructions}", color = TextSecondary, fontSize = 14.sp)
                        }

                        if (medicine.startDate.isNotBlank()) {
                            Text("Duration: ${medicine.startDate} to ${medicine.endDate.ifBlank { "Ongoing" }}", color = TextSecondary, fontSize = 12.sp)
                        }

                        if (medicine.scheduleType == "SELECTED_DAYS" && medicine.selectedDays.isNotEmpty()) {
                            Text("Days: ${medicine.selectedDays.joinToString(", ")}", color = CyanPrimary, fontSize = 12.sp)
                        }

                        if (medicine.reminderTimes.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                medicine.reminderTimes.forEach { time ->
                                    Surface(
                                        color = DarkSurface,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, GlassBorder)
                                    ) {
                                        Text(
                                            text = "${time.label} (${time.time})",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            color = CyanPrimary
                                        )
                                    }
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
private fun TodayScheduleTab(
    medicines: List<Medicine>,
    doseLogs: List<DoseLog>,
    todayStr: String,
    onLogStatus: (Medicine, String, String) -> Unit
) {
    val todayDoseItems = remember(medicines, doseLogs, todayStr) {
        val list = mutableListOf<Pair<Medicine, String>>()
        medicines.forEach { med ->
            if (med.reminderTimes.isNotEmpty()) {
                med.reminderTimes.forEach { time ->
                    list.add(Pair(med, time.time))
                }
            } else if (med.time.isNotBlank()) {
                list.add(Pair(med, med.time))
            }
        }
        list
    }

    if (todayDoseItems.isEmpty()) {
        EmptyStateView("No scheduled doses for today", "Add medicines and schedules to see daily reminders here.")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(todayDoseItems, key = { "${it.first.id}_${it.second}" }) { (medicine, scheduledTime) ->
                val logId = "${medicine.id}_${todayStr}_${scheduledTime.replace(" ", "_")}"
                val currentLog = doseLogs.find { it.id == logId || (it.medicineId == medicine.id && it.scheduledTime == scheduledTime && it.date == todayStr) }
                val currentStatus = currentLog?.status ?: "PENDING"

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(medicine.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${medicine.dosage} • At $scheduledTime", color = TextSecondary, fontSize = 14.sp)
                            }

                            // Status Badge
                            Surface(
                                color = when (currentStatus) {
                                    "TAKEN" -> Color(0xFF1B5E20)
                                    "SKIPPED" -> Color(0xFFB71C1C)
                                    else -> DarkSurface
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Text(
                                    text = currentStatus,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (currentStatus) {
                                        "TAKEN" -> Color(0xFF81C784)
                                        "SKIPPED" -> Color(0xFFEF9A9A)
                                        else -> CyanPrimary
                                    }
                                )
                            }
                        }

                        if (medicine.instructions.isNotBlank()) {
                            Text(medicine.instructions, color = TextSecondary, fontSize = 12.sp)
                        }

                        // Action Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onLogStatus(medicine, scheduledTime, "TAKEN") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentStatus == "TAKEN") Color(0xFF2E7D32) else DarkSurface
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Taken", color = TextPrimary)
                            }

                            Button(
                                onClick = { onLogStatus(medicine, scheduledTime, "SKIPPED") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentStatus == "SKIPPED") Color(0xFFC62828) else DarkSurface
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = SOSRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Skipped", color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DoseHistoryTab(doseLogs: List<DoseLog>) {
    if (doseLogs.isEmpty()) {
        EmptyStateView("No dose history recorded", "Your taken and skipped doses will appear here.")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(doseLogs, key = { it.id }) { log ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(log.medicineName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text("${log.dosage} • ${log.scheduledTime}", color = TextSecondary, fontSize = 13.sp)
                            Text("Date: ${log.date}", color = TextSecondary, fontSize = 11.sp)
                        }

                        Surface(
                            color = if (log.status == "TAKEN") Color(0xFF1B5E20) else Color(0xFFB71C1C),
                            shape = CircleShape
                        ) {
                            Text(
                                text = log.status,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.AccessTime,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(48.dp)
            )
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, fontSize = 14.sp, color = TextSecondary)
        }
    }
}
