package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.data.model.SymptomRecord
import com.manas.medbuddy.data.repository.SymptomRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary
import com.manas.medbuddy.util.SymptomSafetyChecker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomJournalScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { SymptomRepository.getInstance(context) }
    val records by repository.recordsFlow.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<SymptomRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<SymptomRecord?>(null) }

    val neutralSummary = remember(records) {
        repository.generateNeutralSummary()
    }

    val hasSeriousSymptoms = remember(records) {
        records.any { SymptomSafetyChecker.checkIsSerious(it.symptomDescription, it.associatedSymptoms) }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("🩺 Symptom Journal", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyanPrimary,
                contentColor = Color.Black,
                modifier = Modifier.semantics { contentDescription = "Add Symptom" }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Urgent Care Warning Banner if any record contains serious symptoms
            if (hasSeriousSymptoms) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .background(SOSRed.copy(alpha = 0.15f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = SOSRed, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Urgent Care Guidance", color = SOSRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "Some logged symptoms may require immediate medical attention. Please consult a qualified doctor or emergency services if symptoms persist or worsen.",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // AI-Assisted Neutral Summary Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Healing, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                        Text("Neutral Health Summary", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Text(
                        neutralSummary,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            SectionHeader(title = "Symptom History")

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
                        Text("📋", fontSize = 40.sp)
                        Text("No symptoms logged yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Tap + to record your symptoms in natural language.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        SymptomCard(
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
        SymptomFormDialog(
            initialRecord = recordToEdit,
            onDismiss = {
                showAddDialog = false
                recordToEdit = null
            },
            onSave = { record ->
                if (recordToEdit == null) {
                    repository.addSymptom(record)
                } else {
                    repository.updateSymptom(record)
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
            title = { Text("Delete Symptom Record", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this symptom entry?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteSymptom(record.id)
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
private fun SymptomCard(
    record: SymptomRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isSerious = SymptomSafetyChecker.checkIsSerious(record.symptomDescription, record.associatedSymptoms)
    val severityColor = when {
        record.severity <= 3 -> EmeraldPrimary
        record.severity <= 7 -> Color(0xFFFFD700)
        else -> SOSRed
    }

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
                    record.getFormattedDate(),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = severityColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "Severity ${record.severity}/10 (${record.getSeverityLabel()})",
                            color = severityColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

            Text(
                record.symptomDescription,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Onset: ${record.onset}", color = TextSecondary, fontSize = 12.sp)
                Text("Duration: ${record.duration}", color = TextSecondary, fontSize = 12.sp)
            }

            if (record.associatedSymptoms.isNotBlank()) {
                Text(
                    "Associated: ${record.associatedSymptoms}",
                    color = CyanPrimary,
                    fontSize = 12.sp
                )
            }

            if (isSerious) {
                Text(
                    "⚠️ Potential Serious Symptom - Monitor closely or seek medical advice.",
                    color = SOSRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SymptomFormDialog(
    initialRecord: SymptomRecord?,
    onDismiss: () -> Unit,
    onSave: (SymptomRecord) -> Unit
) {
    var description by remember { mutableStateOf(initialRecord?.symptomDescription ?: "") }
    var onset by remember { mutableStateOf(initialRecord?.onset ?: "Today") }
    var duration by remember { mutableStateOf(initialRecord?.duration ?: "Constant") }
    var severity by remember { mutableStateOf(initialRecord?.severity?.toFloat() ?: 3f) }
    var associated by remember { mutableStateOf(initialRecord?.associatedSymptoms ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                if (initialRecord == null) "Log Symptom" else "Edit Symptom",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Describe your symptoms in natural language (English / Hinglish):", color = TextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; error = null },
                    placeholder = { Text("e.g. Sir dard ho raha hai aur halka bukhar hai", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextSecondary,
                        focusedLabelColor = CyanPrimary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = onset,
                        onValueChange = { onset = it },
                        label = { Text("Onset", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Severity: ${severity.toInt()}/10", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            when {
                                severity <= 3 -> "Mild"
                                severity <= 7 -> "Moderate"
                                else -> "Severe"
                            },
                            color = when {
                                severity <= 3 -> EmeraldPrimary
                                severity <= 7 -> Color(0xFFFFD700)
                                else -> SOSRed
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = severity,
                        onValueChange = { severity = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanPrimary,
                            activeTrackColor = CyanPrimary
                        )
                    )
                }

                OutlinedTextField(
                    value = associated,
                    onValueChange = { associated = it },
                    label = { Text("Associated Symptoms (Optional)", color = TextSecondary) },
                    placeholder = { Text("e.g. Nausea, fatigue", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextSecondary,
                        focusedLabelColor = CyanPrimary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (error != null) {
                    Text(error!!, color = SOSRed, fontSize = 12.sp)
                }

                Text(
                    "Disclaimer: MedBuddy does not diagnose conditions. Please consult a healthcare professional.",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isBlank()) {
                        error = "Please enter a symptom description."
                    } else {
                        val record = (initialRecord ?: SymptomRecord(symptomDescription = description)).copy(
                            symptomDescription = description,
                            onset = if (onset.isNotBlank()) onset else "Today",
                            duration = if (duration.isNotBlank()) duration else "Constant",
                            severity = severity.toInt(),
                            associatedSymptoms = associated
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
