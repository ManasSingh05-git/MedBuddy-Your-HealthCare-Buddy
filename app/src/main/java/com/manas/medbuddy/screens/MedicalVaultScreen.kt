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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
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
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.scanner.PrescriptionStorageManager
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.vault.MedicalRecord
import com.manas.medbuddy.vault.MedicalVaultRepository
import com.manas.medbuddy.vault.VaultCategory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MedicalVaultScreen(
    onBack: () -> Unit,
    repository: MedicalVaultRepository? = null
) {
    val context = LocalContext.current
    val repo = repository ?: remember { MedicalVaultRepository(context) }
    val storageManager = remember { PrescriptionStorageManager(context) }

    var records by remember { mutableStateOf<List<MedicalRecord>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(VaultCategory.ALL.name) }

    var showUploadModal by remember { mutableStateOf(false) }
    var previewRecord by remember { mutableStateOf<MedicalRecord?>(null) }
    var deleteCandidate by remember { mutableStateOf<MedicalRecord?>(null) }

    DisposableEffect(Unit) {
        val listener: ValueEventListener? = try {
            repo.listenToRecords(
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

    val filteredRecords = remember(records, searchQuery, selectedCategory) {
        records.filter { record ->
            val matchesCategory = selectedCategory == VaultCategory.ALL.name || record.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    record.title.contains(searchQuery, ignoreCase = true) ||
                    record.doctorName.contains(searchQuery, ignoreCase = true) ||
                    record.notes.contains(searchQuery, ignoreCase = true) ||
                    record.tags.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Medical Records Vault 🔒", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showUploadModal = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Upload Document", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface.copy(alpha = 0.9f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showUploadModal = true },
                containerColor = CyanPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Upload Document")
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
            // Security Isolation Banner
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                    Text("Encrypted Private Storage: Your documents are isolated and stored securely in app private storage.", fontSize = 12.sp, color = TextSecondary)
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search title, doctor, tags...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = futuristicTextFieldColors()
            )

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(VaultCategory.entries.toTypedArray()) { category ->
                    FilterChip(
                        selected = selectedCategory == category.name,
                        onClick = { selectedCategory = category.name },
                        label = { Text(formatCategoryLabel(category.name)) },
                        colors = futuristicFilterChipColors()
                    )
                }
            }

            // Records List
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            } else if (filteredRecords.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(48.dp))
                        Text("No medical records found", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Tap + to upload and store your first medical document securely.", fontSize = 13.sp, color = TextSecondary)
                        Button(
                            onClick = { showUploadModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Upload Document", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { record ->
                        VaultRecordCard(
                            record = record,
                            onPreview = { previewRecord = record },
                            onDownload = {
                                repo.exportRecordToDownloads(record) { success, path ->
                                    if (success) {
                                        Toast.makeText(context, "Exported to Downloads: ${File(path ?: "").name}", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onDelete = { deleteCandidate = record }
                        )
                    }
                }
            }
        }
    }

    // Upload Modal Dialog
    if (showUploadModal) {
        UploadDocumentModal(
            onDismiss = { showUploadModal = false },
            repository = repo,
            onSuccess = {
                showUploadModal = false
                Toast.makeText(context, "Document stored securely in Medical Vault!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Preview Dialog
    if (previewRecord != null) {
        val targetRecord = previewRecord!!
        val bitmap = remember(targetRecord.id) {
            storageManager.loadBitmapFromFile(File(targetRecord.localFilePath))
        }

        AlertDialog(
            onDismissRequest = { previewRecord = null },
            containerColor = DarkSurface,
            title = { Text(targetRecord.title, color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Category: ${formatCategoryLabel(targetRecord.category)} • Date: ${targetRecord.documentDate}", color = TextSecondary, fontSize = 12.sp)
                    if (targetRecord.doctorName.isNotBlank()) Text("Doctor: ${targetRecord.doctorName}", color = TextSecondary, fontSize = 12.sp)

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Document Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Text("Preview unavailable for this format (${targetRecord.mimeType}). Use Export to open.", color = SOSRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repo.exportRecordToDownloads(targetRecord) { success, _ ->
                            if (success) Toast.makeText(context, "Exported to Downloads!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export File", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { previewRecord = null }) {
                    Text("Close", color = TextSecondary)
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
            title = { Text("Delete Document", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${target.title}' from your private Medical Vault? The local file will be removed.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        repo.deleteRecord(target) { _, _ -> }
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
}

@Composable
private fun VaultRecordCard(
    record: MedicalRecord,
    onPreview: () -> Unit,
    onDownload: () -> Unit,
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = CyanPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = getCategoryBadgeEmoji(record.category),
                            modifier = Modifier.padding(6.dp),
                            fontSize = 16.sp
                        )
                    }
                    Column {
                        Text(record.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("${record.documentDate} • ${record.fileSizeFormatted}", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SOSRed)
                }
            }

            if (record.doctorName.isNotBlank()) {
                Text("Doctor/Hospital: ${record.doctorName}", color = TextSecondary, fontSize = 13.sp)
            }

            if (record.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    record.tags.forEach { tag ->
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Text("#$tag", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = CyanPrimary)
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Button(
                    onClick = onPreview,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview", fontSize = 12.sp, color = TextPrimary)
                }

                Button(
                    onClick = onDownload,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", fontSize = 12.sp, color = TextPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UploadDocumentModal(
    onDismiss: () -> Unit,
    repository: MedicalVaultRepository,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(VaultCategory.PRESCRIPTION.name) }
    var doctorName by remember { mutableStateOf("") }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var documentDate by remember { mutableStateOf(todayStr) }
    var tagsInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableIntStateOf(0) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            errorMsg = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Upload to Medical Vault", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                    label = { Text("Document Title") },
                    placeholder = { Text("e.g. Blood Test, Discharge Summary") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Text("Category:", fontSize = 12.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    VaultCategory.entries.filter { it != VaultCategory.ALL }.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(formatCategoryLabel(cat.name)) },
                            colors = futuristicFilterChipColors()
                        )
                    }
                }

                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Doctor / Clinic Name") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = documentDate,
                    onValueChange = { documentDate = it },
                    label = { Text("Document Date (YYYY-MM-DD)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags (comma separated)") },
                    placeholder = { Text("e.g. Heart, Blood, Checkup") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Remarks") },
                    colors = futuristicTextFieldColors()
                )

                Button(
                    onClick = { filePicker.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedUri == null) "Select File (PDF / Image)" else "File Selected ✓", color = CyanPrimary)
                }

                if (isUploading) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(progress = { uploadProgress / 100f }, modifier = Modifier.fillMaxWidth(), color = CyanPrimary)
                        Text("Uploading & Encrypting Document ($uploadProgress%)...", fontSize = 11.sp, color = CyanPrimary)
                    }
                }

                if (errorMsg != null) {
                    Text(errorMsg!!, color = SOSRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedUri == null) {
                        errorMsg = "Please select a document file."
                        return@Button
                    }
                    if (title.isBlank()) {
                        errorMsg = "Please enter document title."
                        return@Button
                    }

                    isUploading = true
                    errorMsg = null

                    val parsedTags = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }

                    repository.saveDocument(
                        sourceUri = selectedUri!!,
                        title = title.trim(),
                        category = selectedCategory,
                        doctorName = doctorName.trim(),
                        documentDate = documentDate.trim(),
                        tags = parsedTags,
                        notes = notes.trim(),
                        onProgress = { progress -> uploadProgress = progress },
                        onComplete = { success, err ->
                            isUploading = false
                            if (success) {
                                onSuccess()
                            } else {
                                errorMsg = err ?: "Failed to upload document"
                            }
                        }
                    )
                },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Store in Vault", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

private fun formatCategoryLabel(catName: String): String = when (catName) {
    "ALL" -> "All Documents"
    "PRESCRIPTION" -> "Prescriptions 💊"
    "LAB_REPORT" -> "Lab Reports 🧪"
    "DISCHARGE_SUMMARY" -> "Discharge Summaries 🏥"
    "VACCINATION_RECORD" -> "Vaccinations 💉"
    "GENERAL_REPORT" -> "General Reports 📑"
    else -> catName
}

private fun getCategoryBadgeEmoji(catName: String): String = when (catName) {
    "PRESCRIPTION" -> "💊"
    "LAB_REPORT" -> "🧪"
    "DISCHARGE_SUMMARY" -> "🏥"
    "VACCINATION_RECORD" -> "💉"
    else -> "📑"
}
