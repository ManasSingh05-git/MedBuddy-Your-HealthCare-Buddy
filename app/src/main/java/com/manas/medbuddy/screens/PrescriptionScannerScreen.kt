package com.manas.medbuddy.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.data.MedicineRepository
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.model.ReminderTime
import com.manas.medbuddy.model.ReminderType
import com.manas.medbuddy.reminder.ReminderScheduler
import com.manas.medbuddy.scanner.ExtractedMedicineCandidate
import com.manas.medbuddy.scanner.PrescriptionScannerEngine
import com.manas.medbuddy.scanner.PrescriptionStorageManager
import com.manas.medbuddy.ui.components.AnimatedProgressRing
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionScannerScreen(
    onBack: () -> Unit,
    onNavigateToMedicineList: () -> Unit = {},
    repository: MedicineRepository = remember { MedicineRepository() }
) {
    val context = LocalContext.current
    val storageManager = remember { PrescriptionStorageManager(context) }
    val scannerEngine = remember { PrescriptionScannerEngine() }
    val scheduler = remember { ReminderScheduler(context) }

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedFile by remember { mutableStateOf<File?>(null) }

    var isScanning by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var candidateList by remember { mutableStateOf<List<ExtractedMedicineCandidate>>(emptyList()) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            candidateList = emptyList()
            errorMessage = null
            successMessage = null
            isScanning = true

            scannerEngine.scanPrescriptionBitmap(
                bitmap = bitmap,
                onSuccess = { result ->
                    candidateList = result.candidates
                    isScanning = false
                },
                onError = { err ->
                    errorMessage = err
                    isScanning = false
                }
            )
        }
    }

    // Document Picker Launcher (Images and PDFs)
    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val savedFile = storageManager.saveUriToSecureStorage(uri)
            if (savedFile != null) {
                selectedFile = savedFile
                val bitmap = storageManager.loadBitmapFromFile(savedFile)
                if (bitmap != null) {
                    selectedBitmap = bitmap
                    candidateList = emptyList()
                    errorMessage = null
                    successMessage = null
                    isScanning = true

                    scannerEngine.scanPrescriptionBitmap(
                        bitmap = bitmap,
                        onSuccess = { result ->
                            candidateList = result.candidates
                            isScanning = false
                        },
                        onError = { err ->
                            errorMessage = err
                            isScanning = false
                        }
                    )
                } else {
                    errorMessage = "Failed to render prescription image/PDF. Please try another file."
                }
            } else {
                errorMessage = "Failed to store selected document securely."
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("AI Prescription Scanner", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "How it works", tint = CyanPrimary)
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
            // Upload & Capture Section
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Capture or Upload Prescription", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Select a clear prescription image or PDF. Our AI OCR extracts candidate medicine details for your review.", fontSize = 13.sp, color = TextSecondary)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, GlassBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = CyanPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Take Photo", color = TextPrimary, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { pickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, GlassBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = CyanPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload File", color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Scanning Loading State
            if (isScanning) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnimatedProgressRing(progress = 0.75f, modifier = Modifier.size(100.dp))
                        Text("Scanning Prescription with AI OCR...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        Text("Extracting medicine names, dosages, and instructions...", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            // Error Display
            if (errorMessage != null) {
                Surface(
                    color = SOSRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SOSRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = SOSRed)
                        Text(errorMessage!!, color = TextPrimary, fontSize = 13.sp)
                    }
                }
            }

            // Document Preview & Candidates List
            if (selectedBitmap != null && !isScanning) {
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
                            Text("Original Document Preview", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                            IconButton(
                                onClick = {
                                    selectedBitmap = null
                                    candidateList = emptyList()
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TextSecondary)
                            }
                        }

                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Scanned Prescription",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }

                // Extracted Candidates Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Candidate Medicines (${candidateList.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(
                        onClick = {
                            candidateList = candidateList + ExtractedMedicineCandidate(
                                id = UUID.randomUUID().toString(),
                                name = "New Medicine",
                                strength = "500 mg",
                                dosage = "1 Tablet",
                                frequency = "Twice Daily",
                                instructions = "Take after meal"
                            )
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add candidate", tint = CyanPrimary)
                    }
                }

                if (candidateList.isEmpty()) {
                    Text("No candidate medicines detected. Tap + to add one manually.", color = TextSecondary, fontSize = 14.sp)
                } else {
                    candidateList.forEachIndexed { index, candidate ->
                        CandidateReviewCard(
                            index = index + 1,
                            candidate = candidate,
                            onUpdate = { updated ->
                                candidateList = candidateList.map { if (it.id == updated.id) updated else it }
                            },
                            onDelete = {
                                candidateList = candidateList.filter { it.id != candidate.id }
                            }
                        )
                    }

                    // Explicit Confirmation Button
                    Button(
                        onClick = {
                            if (candidateList.isEmpty()) {
                                errorMessage = "No candidate medicines to confirm."
                                return@Button
                            }

                            isSaving = true
                            errorMessage = null

                            var savedCount = 0
                            candidateList.forEach { candidate ->
                                val timeSummary = "${candidate.dosage} - ${candidate.frequency}"
                                val defaultReminderTimes = listOf(
                                    ReminderTime(id = UUID.randomUUID().toString(), label = "Morning", time = "08:00 AM", type = ReminderType.MORNING.name),
                                    ReminderTime(id = UUID.randomUUID().toString(), label = "Evening", time = "08:00 PM", type = ReminderType.EVENING.name)
                                )

                                val medicine = Medicine(
                                    id = "",
                                    name = candidate.name.trim(),
                                    dosage = "${candidate.strength} ${candidate.dosage}".trim(),
                                    time = timeSummary,
                                    instructions = candidate.instructions.trim(),
                                    reminderTimes = defaultReminderTimes
                                )

                                repository.saveMedicine(medicine) { success, id ->
                                    if (success) {
                                        savedCount++
                                        val savedMed = medicine.copy(id = id ?: "")
                                        scheduler.scheduleMedicineReminders(savedMed)
                                    }
                                    if (savedCount == candidateList.size) {
                                        isSaving = false
                                        successMessage = "Successfully confirmed and added ${candidateList.size} medicine(s) to your schedule!"
                                    }
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                                    Text("Confirm & Add to Medicines System", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            if (successMessage != null) {
                AlertDialog(
                    onDismissRequest = { successMessage = null },
                    containerColor = DarkSurface,
                    title = { Text("Prescription Saved", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = { Text(successMessage!!, color = TextSecondary) },
                    confirmButton = {
                        Button(
                            onClick = {
                                successMessage = null
                                onNavigateToMedicineList()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("View Medicines", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = DarkSurface,
            title = { Text("AI Prescription Scanner Safety", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• The AI OCR extracts candidate medicine names, strengths, and dosage.", color = TextSecondary, fontSize = 13.sp)
                    Text("• All extracted fields are editable. User review and explicit confirmation is required before adding.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Unclear handwriting or ambiguous dosages are flagged for pharmacist/doctor review.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Original files are stored securely on device.", color = TextSecondary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)) {
                    Text("Got It", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun CandidateReviewCard(
    index: Int,
    candidate: ExtractedMedicineCandidate,
    onUpdate: (ExtractedMedicineCandidate) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember(candidate.id) { mutableStateOf(candidate.name) }
    var strength by remember(candidate.id) { mutableStateOf(candidate.strength) }
    var dosage by remember(candidate.id) { mutableStateOf(candidate.dosage) }
    var frequency by remember(candidate.id) { mutableStateOf(candidate.frequency) }
    var instructions by remember(candidate.id) { mutableStateOf(candidate.instructions) }

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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = CyanPrimary)
                    Text("Candidate #$index", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = SOSRed)
                }
            }

            // Flagged for Pharmacist Review Banner
            if (candidate.needsPharmacistReview) {
                Surface(
                    color = SOSRed.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SOSRed)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = SOSRed, modifier = Modifier.size(20.dp))
                        Text(
                            text = candidate.flagReason.ifBlank { "⚠️ Flagged for Pharmacist/Doctor Review: Unclear dosage or handwriting." },
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    onUpdate(candidate.copy(name = it))
                },
                label = { Text("Medicine Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = futuristicTextFieldColors()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = strength,
                    onValueChange = {
                        strength = it
                        onUpdate(candidate.copy(strength = it))
                    },
                    label = { Text("Strength (e.g. 500mg)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = {
                        dosage = it
                        onUpdate(candidate.copy(dosage = it))
                    },
                    label = { Text("Dosage (e.g. 1 Tab)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = futuristicTextFieldColors()
                )
            }

            OutlinedTextField(
                value = frequency,
                onValueChange = {
                    frequency = it
                    onUpdate(candidate.copy(frequency = it))
                },
                label = { Text("Frequency (e.g. Twice Daily)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = futuristicTextFieldColors()
            )

            OutlinedTextField(
                value = instructions,
                onValueChange = {
                    instructions = it
                    onUpdate(candidate.copy(instructions = it))
                },
                label = { Text("Instructions (e.g. Take after meal)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = futuristicTextFieldColors()
            )
        }
    }
}
