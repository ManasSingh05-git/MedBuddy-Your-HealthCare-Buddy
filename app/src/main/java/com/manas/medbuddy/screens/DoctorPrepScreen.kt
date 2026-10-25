package com.manas.medbuddy.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.data.model.Appointment
import com.manas.medbuddy.data.model.DoctorPrepRecord
import com.manas.medbuddy.data.repository.DoctorPrepRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPrepScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { DoctorPrepRepository.getInstance(context) }
    val appointments by repository.appointmentsFlow.collectAsState()
    val prepRecords by repository.prepRecordsFlow.collectAsState()

    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    var currentPrepRecord by remember { mutableStateOf<DoctorPrepRecord?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showEditSummaryDialog by remember { mutableStateOf(false) }
    var editableSummaryText by remember { mutableStateOf("") }
    var simulateFailure by remember { mutableStateOf(false) }

    // Check authorization
    val isAuthorized = remember { repository.checkAuthorization() }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("🩺 Doctor Visit Preparation", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
        }
    ) { paddingValues ->
        if (!isAuthorized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Unauthorized access. Please log in.", color = SOSRed, fontWeight = FontWeight.Bold)
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
            // Step 1: Choose Appointment
            SectionHeader(title = "1. Choose Appointment")

            if (appointments.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("No upcoming appointments found.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    appointments.forEach { appt ->
                        val isSelected = selectedAppointment?.id == appt.id
                        Surface(
                            onClick = {
                                selectedAppointment = appt
                                // Find existing prep or generate
                                val existing = prepRecords.find { it.appointmentId == appt.id }
                                currentPrepRecord = existing
                            },
                            color = if (isSelected) CyanPrimary.copy(alpha = 0.25f) else DarkSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) CyanPrimary else Color.Transparent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(appt.doctorName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(appt.specialty, color = CyanPrimary, fontSize = 11.sp)
                                Text(appt.dateTime, color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Generate Button & AI Failure Simulation Toggle
            selectedAppointment?.let { appt ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(
                            checked = simulateFailure,
                            onCheckedChange = { simulateFailure = it },
                            colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                        )
                        Text("Simulate AI Timeout/Failure", color = TextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            isGenerating = true
                            errorMessage = null
                            try {
                                val record = repository.generatePrepForAppointment(appt, simulateFailure)
                                currentPrepRecord = record
                                if (simulateFailure) {
                                    errorMessage = "AI generation failed or timed out. Displaying fallback recorded facts."
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error generating preparation: ${e.localizedMessage}"
                            } finally {
                                isGenerating = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                    ) {
                        Text(if (isGenerating) "Generating..." else "Prepare Visit Summary", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Loading state
            if (isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            }

            // Error state
            errorMessage?.let { err ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .background(SOSRed.copy(alpha = 0.15f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(err, color = SOSRed, fontSize = 13.sp)
                    }
                }
            }

            // Step 2 & 3: Recorded Facts vs AI Suggestions
            currentPrepRecord?.let { prep ->
                SectionHeader(title = "2. Gathered Recorded Facts (Verified)")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(color = EmeraldPrimary.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                            Text("✓ Recorded Facts (From Your History)", color = EmeraldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        Text("Recorded Concerns:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        if (prep.recordedFactsConcerns.isEmpty()) {
                            Text("No concerns logged in symptom journal.", color = TextPrimary, fontSize = 13.sp)
                        } else {
                            prep.recordedFactsConcerns.forEach { concern ->
                                Text("• $concern", color = TextPrimary, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Current Medicines & Reports:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        prep.recordedFactsMedicines.forEach { med -> Text("• Med: $med", color = TextPrimary, fontSize = 12.sp) }
                        prep.recordedFactsReports.forEach { rep -> Text("• Report: $rep", color = TextPrimary, fontSize = 12.sp) }
                    }
                }

                SectionHeader(title = "3. AI Consultation Summary & Questions")
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
                            Surface(color = CyanPrimary.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                Text("🤖 AI Suggestion (Review Before Use)", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            IconButton(
                                onClick = {
                                    editableSummaryText = prep.editableConsultationSummary
                                    showEditSummaryDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Summary", tint = CyanPrimary, modifier = Modifier.size(16.dp))
                            }
                        }

                        Text("Editable Summary:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(prep.editableConsultationSummary, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Suggested Questions for Doctor:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        prep.aiSuggestedQuestions.forEach { q ->
                            Text("❓ $q", color = TextPrimary, fontSize = 12.sp)
                        }

                        // Export & Attach Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Doctor Visit Preparation for ${prep.doctorName}:\n\nSummary:\n${prep.editableConsultationSummary}\n\nQuestions:\n${prep.aiSuggestedQuestions.joinToString("\n")}")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = CyanPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export / Share")
                            }

                            Button(
                                onClick = {
                                    val updated = prep.copy(isAttached = !prep.isAttached)
                                    repository.savePrepRecord(updated)
                                    currentPrepRecord = updated
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (prep.isAttached) EmeraldPrimary else CyanPrimary,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (prep.isAttached) "Attached ✓" else "Attach to Appt")
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Summary Dialog
    if (showEditSummaryDialog) {
        AlertDialog(
            onDismissRequest = { showEditSummaryDialog = false },
            containerColor = DarkSurface,
            title = { Text("Edit Consultation Summary", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editableSummaryText,
                    onValueChange = { editableSummaryText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        currentPrepRecord?.let { prep ->
                            val updated = prep.copy(editableConsultationSummary = editableSummaryText)
                            repository.savePrepRecord(updated)
                            currentPrepRecord = updated
                        }
                        showEditSummaryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditSummaryDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
