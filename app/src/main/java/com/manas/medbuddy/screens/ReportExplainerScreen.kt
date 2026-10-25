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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
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
import com.manas.medbuddy.reports.LabTestItem
import com.manas.medbuddy.reports.LabValueStatus
import com.manas.medbuddy.reports.MedicalReportResult
import com.manas.medbuddy.reports.ReportExplainerEngine
import com.manas.medbuddy.scanner.PrescriptionStorageManager
import com.manas.medbuddy.ui.components.AnimatedProgressRing
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportExplainerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val storageManager = remember { PrescriptionStorageManager(context) }
    val explainerEngine = remember { ReportExplainerEngine() }

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedFile by remember { mutableStateOf<File?>(null) }

    var isAnalyzing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reportResult by remember { mutableStateOf<MedicalReportResult?>(null) }

    var isHinglishMode by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            reportResult = null
            errorMessage = null
            isAnalyzing = true

            explainerEngine.analyzeReportBitmap(
                bitmap = bitmap,
                onSuccess = { result ->
                    reportResult = result
                    isAnalyzing = false
                },
                onError = { err ->
                    errorMessage = err
                    isAnalyzing = false
                }
            )
        }
    }

    // Document Picker Launcher (Images & PDFs)
    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val savedFile = storageManager.saveUriToSecureStorage(uri)
            if (savedFile != null) {
                selectedFile = savedFile
                val bitmap = storageManager.loadBitmapFromFile(savedFile)
                if (bitmap != null) {
                    selectedBitmap = bitmap
                    reportResult = null
                    errorMessage = null
                    isAnalyzing = true

                    explainerEngine.analyzeReportBitmap(
                        bitmap = bitmap,
                        onSuccess = { result ->
                            reportResult = result
                            isAnalyzing = false
                        },
                        onError = { err ->
                            errorMessage = err
                            isAnalyzing = false
                        }
                    )
                } else {
                    errorMessage = "Failed to render lab report file. Please select another image or PDF."
                }
            } else {
                errorMessage = "Failed to save document securely."
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("AI Report Explainer", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Disclaimer", tint = CyanPrimary)
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
            // Upload / Capture Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Upload Lab Test Report", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Upload Blood Test, Lipid Profile, Thyroid, or Urine report image/PDF. AI extracts values and explains them in simple English & Hinglish.", fontSize = 13.sp, color = TextSecondary)

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
                            Text("Upload Report", color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Loading / Analyzing State
            if (isAnalyzing) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnimatedProgressRing(progress = 0.8f, modifier = Modifier.size(100.dp))
                        Text("AI OCR Analyzing Medical Report...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        Text("Parsing lab parameters, units, and reference ranges...", fontSize = 12.sp, color = TextSecondary)
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

            // Report Results
            if (reportResult != null && !isAnalyzing) {
                val currentResult = reportResult!!

                // Mandatory AI Disclaimer Banner
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyanPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = CyanPrimary)
                            Text("🤖 AI Educational Summary — Not a Medical Diagnosis", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 14.sp)
                        }
                        Text("This explanation is for educational understanding only. Always consult your qualified doctor or clinician before taking medical decisions.", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                // Language Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = CyanPrimary)
                        Text("Explanation Language:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }

                    FilterChip(
                        selected = isHinglishMode,
                        onClick = { isHinglishMode = !isHinglishMode },
                        label = { Text(if (isHinglishMode) "🇮🇳 Hinglish Mode" else "🇬🇧 English Mode") },
                        colors = futuristicFilterChipColors()
                    )
                }

                // Overall AI Summary Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Report Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        Text(
                            text = if (isHinglishMode) currentResult.summaryHinglish else currentResult.summaryEnglish,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Document Preview Card
                if (selectedBitmap != null) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Original Scanned Report", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                                IconButton(
                                    onClick = {
                                        selectedBitmap = null
                                        reportResult = null
                                    }
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TextSecondary)
                                }
                            }

                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Original Report Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                }

                // Lab Test Items Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Extracted Test Values (${currentResult.labItems.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(
                        onClick = {
                            val newItem = LabTestItem(
                                id = UUID.randomUUID().toString(),
                                name = "Custom Test Item",
                                value = "10.0",
                                unit = "mg/dL",
                                referenceRange = "8.0 - 12.0",
                                status = LabValueStatus.NORMAL,
                                explanationEnglish = "Custom test value entered by user.",
                                explanationHinglish = "User dwara add kiya gaya test value."
                            )
                            reportResult = currentResult.copy(labItems = currentResult.labItems + newItem)
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item", tint = CyanPrimary)
                    }
                }

                currentResult.labItems.forEachIndexed { index, item ->
                    LabItemCard(
                        index = index + 1,
                        item = item,
                        isHinglish = isHinglishMode,
                        onUpdate = { updatedItem ->
                            val updatedList = currentResult.labItems.map { if (it.id == updatedItem.id) updatedItem else it }
                            val (newEng, newHing) = explainerEngine.generateSummaries(updatedList)
                            val newQuestions = explainerEngine.generateDoctorQuestions(updatedList)
                            reportResult = currentResult.copy(
                                labItems = updatedList,
                                summaryEnglish = newEng,
                                summaryHinglish = newHing,
                                suggestedQuestionsForDoctor = newQuestions
                            )
                        },
                        onDelete = {
                            val updatedList = currentResult.labItems.filter { it.id != item.id }
                            val (newEng, newHing) = explainerEngine.generateSummaries(updatedList)
                            val newQuestions = explainerEngine.generateDoctorQuestions(updatedList)
                            reportResult = currentResult.copy(
                                labItems = updatedList,
                                summaryEnglish = newEng,
                                summaryHinglish = newHing,
                                suggestedQuestionsForDoctor = newQuestions
                            )
                        }
                    )
                }

                // Doctor Questions Section
                if (currentResult.suggestedQuestionsForDoctor.isNotEmpty()) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = CyanPrimary)
                                Text("Suggested Questions for Your Clinician", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }

                            currentResult.suggestedQuestionsForDoctor.forEachIndexed { qIdx, question ->
                                Surface(
                                    color = DarkSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, GlassBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("${qIdx + 1}.", fontWeight = FontWeight.Bold, color = CyanPrimary)
                                        Text(question, fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = DarkSurface,
            title = { Text("AI Medical Report Explainer Safety", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• The AI OCR extracts test names, values, units, and laboratory reference ranges.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Values outside supplied lab ranges are highlighted as HIGH or LOW.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Terminology is explained in simple English and Hinglish.", color = TextSecondary, fontSize = 13.sp)
                    Text("• This feature provides educational insights only and does not formulate medical diagnoses.", color = TextSecondary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)) {
                    Text("Understand", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun LabItemCard(
    index: Int,
    item: LabTestItem,
    isHinglish: Boolean,
    onUpdate: (LabTestItem) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember(item.id) { mutableStateOf(item.name) }
    var value by remember(item.id) { mutableStateOf(item.value) }
    var unit by remember(item.id) { mutableStateOf(item.unit) }
    var refRange by remember(item.id) { mutableStateOf(item.referenceRange) }

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
                    Text("#$index $name", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status Badge (HIGH / LOW / NORMAL)
                    Surface(
                        color = when (item.status) {
                            LabValueStatus.HIGH -> SOSRed.copy(alpha = 0.2f)
                            LabValueStatus.LOW -> Color(0xFF1E88E5).copy(alpha = 0.2f)
                            LabValueStatus.NORMAL -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                            else -> DarkSurface
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            when (item.status) {
                                LabValueStatus.HIGH -> SOSRed
                                LabValueStatus.LOW -> Color(0xFF42A5F5)
                                LabValueStatus.NORMAL -> Color(0xFF81C784)
                                else -> GlassBorder
                            }
                        )
                    ) {
                        Text(
                            text = when (item.status) {
                                LabValueStatus.HIGH -> "⬆️ HIGH"
                                LabValueStatus.LOW -> "⬇️ LOW"
                                LabValueStatus.NORMAL -> "✓ NORMAL"
                                else -> "UNKNOWN"
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (item.status) {
                                LabValueStatus.HIGH -> SOSRed
                                LabValueStatus.LOW -> Color(0xFF64B5F6)
                                LabValueStatus.NORMAL -> Color(0xFF81C784)
                                else -> TextSecondary
                            }
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SOSRed)
                    }
                }
            }

            // Editable Input Fields
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    onUpdate(item.copy(name = it))
                },
                label = { Text("Test Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = futuristicTextFieldColors()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        val num = it.toDoubleOrNull()
                        val newStatus = ReportExplainerEngine().evaluateStatus(num, item.minRange, item.maxRange)
                        onUpdate(item.copy(value = it, numericValue = num, status = newStatus, isOutofRange = newStatus == LabValueStatus.HIGH || newStatus == LabValueStatus.LOW))
                    },
                    label = { Text("Value") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = {
                        unit = it
                        onUpdate(item.copy(unit = it))
                    },
                    label = { Text("Unit") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = futuristicTextFieldColors()
                )
            }

            OutlinedTextField(
                value = refRange,
                onValueChange = {
                    refRange = it
                    onUpdate(item.copy(referenceRange = it))
                },
                label = { Text("Lab Reference Range") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = futuristicTextFieldColors()
            )

            // Plain English / Hinglish Explanation Section
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("💡 What this means:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    Text(
                        text = if (isHinglish) item.explanationHinglish.ifBlank { item.explanationEnglish } else item.explanationEnglish.ifBlank { "Laboratory measurement parameter." },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
