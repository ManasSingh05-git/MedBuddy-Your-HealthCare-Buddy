package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.data.model.CycleRecord
import com.manas.medbuddy.data.repository.MenstrualRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenstrualTrackerScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { MenstrualRepository.getInstance(context) }
    val consentGranted by repository.consentFlow.collectAsState()
    val records by repository.recordsFlow.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<CycleRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<CycleRecord?>(null) }

    val estimatedNext = remember(records) {
        repository.calculateEstimatedNextPeriod()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("🔒 Private Menstrual Health Tracker", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics { contentDescription = "Back" }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        floatingActionButton = {
            if (consentGranted) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = CyanPrimary,
                    contentColor = Color.Black,
                    modifier = Modifier.semantics { contentDescription = "Log Cycle" }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { paddingValues ->
        if (!consentGranted) {
            // Opt-in Consent Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(40.dp))
                        Text("Strict Privacy & Opt-in Consent", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "This feature is strictly optional and private. Reproductive data is stored in isolated, secure local preferences and is NEVER shared with family profiles or caregivers by default.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = { repository.setConsent(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("I Consent & Enable Private Tracker", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estimate Banner (Clearly marked as Estimate / Subject to variation)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Estimate (Subject to variation)",
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text("Predicted Next Period:", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        estimatedNext,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        "Disclaimer: This estimate is for informational purposes only and does not constitute a medical diagnosis.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            SectionHeader(title = "Cycle History")

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🩸", fontSize = 36.sp)
                        Text("No cycle records logged yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Tap + to record your cycle dates.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        CycleRecordCard(
                            record = record,
                            onEdit = { recordToEdit = record },
                            onDelete = { recordToDelete = record }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || recordToEdit != null) {
        CycleFormDialog(
            initialRecord = recordToEdit,
            onDismiss = {
                showAddDialog = false
                recordToEdit = null
            },
            onSave = { record ->
                if (recordToEdit == null) {
                    repository.addRecord(record)
                } else {
                    repository.updateRecord(record)
                }
                showAddDialog = false
                recordToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            containerColor = DarkSurface,
            title = { Text("Delete Cycle Record", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this cycle entry?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteRecord(record.id)
                        recordToDelete = null
                    }
                ) {
                    Text("Delete", color = SOSRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun CycleRecordCard(
    record: CycleRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                Text(
                    "${record.startDate} — ${record.endDate}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CyanPrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "${record.cycleLengthDays} Days",
                            color = CyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SOSRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (record.symptoms.isNotBlank()) {
                Text("Symptoms: ${record.symptoms}", color = TextSecondary, fontSize = 12.sp)
            }
            if (record.notes.isNotBlank()) {
                Text("Notes: ${record.notes}", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CycleFormDialog(
    initialRecord: CycleRecord?,
    onDismiss: () -> Unit,
    onSave: (CycleRecord) -> Unit
) {
    var startDate by remember { mutableStateOf(initialRecord?.startDate ?: "Oct 01, 2025") }
    var endDate by remember { mutableStateOf(initialRecord?.endDate ?: "Oct 06, 2025") }
    var lengthText by remember { mutableStateOf(initialRecord?.cycleLengthDays?.toString() ?: "28") }
    var symptoms by remember { mutableStateOf(initialRecord?.symptoms ?: "") }
    var notes by remember { mutableStateOf(initialRecord?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (initialRecord == null) "Log Cycle Dates" else "Edit Cycle Dates", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it; error = null },
                    label = { Text("Start Date (e.g. Oct 01, 2025)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date (e.g. Oct 06, 2025)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = lengthText,
                    onValueChange = { lengthText = it },
                    label = { Text("Cycle Length in Days", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Symptoms (Optional)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                if (error != null) {
                    Text(error!!, color = SOSRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val len = lengthText.toIntOrNull()
                    if (startDate.isBlank() || endDate.isBlank()) {
                        error = "Please enter start and end dates."
                    } else if (len == null || len <= 0) {
                        error = "Please enter a valid cycle length."
                    } else {
                        val record = (initialRecord ?: CycleRecord(startDate = startDate, endDate = endDate)).copy(
                            startDate = startDate,
                            endDate = endDate,
                            cycleLengthDays = len,
                            symptoms = symptoms,
                            notes = notes
                        )
                        onSave(record)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
