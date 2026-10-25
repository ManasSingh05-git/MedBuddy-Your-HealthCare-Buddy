package com.manas.medbuddy.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.allergy.AllergyCategory
import com.manas.medbuddy.allergy.AllergyProfileStatus
import com.manas.medbuddy.allergy.AllergyRecord
import com.manas.medbuddy.allergy.AllergyRepository
import com.manas.medbuddy.allergy.AllergySeverity
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AllergyRegistryScreen(
    onBack: () -> Unit,
    repository: AllergyRepository? = null
) {
    val context = LocalContext.current
    val repo = repository ?: remember { AllergyRepository(context) }

    var allergies by remember { mutableStateOf<List<AllergyRecord>>(emptyList()) }
    var profileStatus by remember { mutableStateOf(AllergyProfileStatus.UNRECORDED) }
    var loading by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AllergyCategory.ALL.name) }

    var showAddModal by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<AllergyRecord?>(null) }
    var deleteCandidate by remember { mutableStateOf<AllergyRecord?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener: ValueEventListener? = try {
            repo.listenToAllergies(
                onUpdate = { list, status ->
                    allergies = list
                    profileStatus = status
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

    val filteredAllergies = remember(allergies, searchQuery, selectedCategory) {
        allergies.filter { record ->
            val matchesCategory = selectedCategory == AllergyCategory.ALL.name || record.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    record.substance.contains(searchQuery, ignoreCase = true) ||
                    record.reportedReaction.contains(searchQuery, ignoreCase = true) ||
                    record.notes.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Allergy & Reaction Registry", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                        Icon(Icons.Default.Add, contentDescription = "Add Allergy", tint = CyanPrimary)
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
                Icon(Icons.Default.Add, contentDescription = "Add Allergy")
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
            // Profile Status Banner (Explicitly distinguishes NKDA vs Unrecorded)
            ProfileStatusBanner(
                status = profileStatus,
                allergyCount = allergies.size,
                onMarkNkda = {
                    repo.setNoKnownAllergiesStatus(true) { success, _ ->
                        if (success) Toast.makeText(context, "Confirmed: No Known Allergies (NKDA)", Toast.LENGTH_SHORT).show()
                    }
                },
                onAddClick = {
                    editingRecord = null
                    showAddModal = true
                }
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search substance, reaction, notes...") },
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
                items(AllergyCategory.entries.toTypedArray()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.name,
                        onClick = { selectedCategory = cat.name },
                        label = { Text(formatCategoryLabel(cat.name)) },
                        colors = futuristicFilterChipColors()
                    )
                }
            }

            // List View
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            } else if (filteredAllergies.isEmpty()) {
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
                        Icon(Icons.Default.Shield, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(48.dp))
                        Text(
                            text = if (profileStatus == AllergyProfileStatus.NO_KNOWN_ALLERGIES) "No Known Allergies Recorded (NKDA)" else "No Allergy Records Found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (profileStatus == AllergyProfileStatus.NO_KNOWN_ALLERGIES) "Patient explicitly confirmed no known food, drug, or environmental allergies." else "Tap + to add a medication, food, or environmental allergy.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Button(
                            onClick = {
                                editingRecord = null
                                showAddModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("+ Add Allergy Record", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredAllergies, key = { it.id }) { record ->
                        AllergyRecordCard(
                            record = record,
                            onEdit = {
                                editingRecord = record
                                showAddModal = true
                            },
                            onDelete = { deleteCandidate = record }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Modal Dialog
    if (showAddModal) {
        AddEditAllergyModal(
            existingRecord = editingRecord,
            onDismiss = { showAddModal = false },
            repository = repo,
            onSuccess = {
                showAddModal = false
                Toast.makeText(context, "Allergy record saved successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    if (deleteCandidate != null) {
        val target = deleteCandidate!!
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            containerColor = DarkSurface,
            title = { Text("Delete Allergy Record", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete allergy record '${target.substance}'? Warning: Safety alerts when adding medicines will no longer check this substance.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        repo.deleteAllergyRecord(target.id) { _, _ -> }
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
            title = { Text("Allergy Registry Safety Guidelines", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Explicitly distinguishes 'Unrecorded Profile' from 'No Known Allergies' (NKDA).", color = TextSecondary, fontSize = 13.sp)
                    Text("• Verified status marks physician/lab-confirmed entries vs self-reported items.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Informational allergy warnings are displayed alongside medicines, but do not replace clinical verification by a doctor or pharmacist.", color = TextSecondary, fontSize = 13.sp)
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
private fun ProfileStatusBanner(
    status: AllergyProfileStatus,
    allergyCount: Int,
    onMarkNkda: () -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        color = when (status) {
            AllergyProfileStatus.NO_KNOWN_ALLERGIES -> Color(0xFF1B5E20).copy(alpha = 0.2f)
            AllergyProfileStatus.HAS_ALLERGIES -> CyanPrimary.copy(alpha = 0.15f)
            else -> Color(0xFFF57F17).copy(alpha = 0.2f)
        },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            when (status) {
                AllergyProfileStatus.NO_KNOWN_ALLERGIES -> Color(0xFF81C784)
                AllergyProfileStatus.HAS_ALLERGIES -> CyanPrimary
                else -> Color(0xFFFFB74D)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = when (status) {
                        AllergyProfileStatus.NO_KNOWN_ALLERGIES -> "✓ Confirmed: No Known Allergies (NKDA)"
                        AllergyProfileStatus.HAS_ALLERGIES -> "⚠️ Active Allergies Recorded ($allergyCount)"
                        else -> "⚠️ Allergy Status: UNRECORDED"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = when (status) {
                        AllergyProfileStatus.NO_KNOWN_ALLERGIES -> "Explicitly confirmed no known medication, food, or environmental allergies."
                        AllergyProfileStatus.HAS_ALLERGIES -> "Allergies are cross-checked when viewing or scheduling medicines."
                        else -> "Profile not completed yet. Please record your allergies or confirm NKDA."
                    },
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            if (status == AllergyProfileStatus.UNRECORDED) {
                TextButton(onClick = onMarkNkda) {
                    Text("Mark NKDA", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AllergyRecordCard(
    record: AllergyRecord,
    onEdit: () -> Unit,
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
                Column {
                    Text(record.substance, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Category: ${formatCategoryLabel(record.category)}", fontSize = 12.sp, color = TextSecondary)
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Severity Badge
                Surface(
                    color = when (record.severity) {
                        AllergySeverity.LIFE_THREATENING.name, AllergySeverity.SEVERE.name -> SOSRed.copy(alpha = 0.2f)
                        AllergySeverity.MODERATE.name -> Color(0xFFF57F17).copy(alpha = 0.2f)
                        else -> CyanPrimary.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        when (record.severity) {
                            AllergySeverity.LIFE_THREATENING.name, AllergySeverity.SEVERE.name -> SOSRed
                            AllergySeverity.MODERATE.name -> Color(0xFFFFB74D)
                            else -> CyanPrimary
                        }
                    )
                ) {
                    Text(
                        text = "Severity: ${record.severity}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (record.severity) {
                            AllergySeverity.LIFE_THREATENING.name, AllergySeverity.SEVERE.name -> SOSRed
                            AllergySeverity.MODERATE.name -> Color(0xFFFFB74D)
                            else -> CyanPrimary
                        }
                    )
                }

                // Verification Badge
                Surface(
                    color = if (record.isVerified) Color(0xFF1B5E20).copy(alpha = 0.2f) else DarkSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (record.isVerified) Color(0xFF81C784) else GlassBorder)
                ) {
                    Text(
                        text = if (record.isVerified) "✓ Verified (${record.verifiedBy})" else "⚠️ Unverified / Self-Reported",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (record.isVerified) Color(0xFF81C784) else TextSecondary
                    )
                }
            }

            if (record.reportedReaction.isNotBlank()) {
                Text("Reported Reaction: ${record.reportedReaction}", fontSize = 13.sp, color = TextPrimary)
            }

            if (record.onsetDate.isNotBlank()) {
                Text("Onset Date: ${record.onsetDate}", fontSize = 11.sp, color = TextSecondary)
            }

            if (record.notes.isNotBlank()) {
                Text("Notes: ${record.notes}", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddEditAllergyModal(
    existingRecord: AllergyRecord?,
    onDismiss: () -> Unit,
    repository: AllergyRepository,
    onSuccess: () -> Unit
) {
    var substance by remember { mutableStateOf(existingRecord?.substance ?: "") }
    var selectedCategory by remember { mutableStateOf(existingRecord?.category ?: AllergyCategory.MEDICATION.name) }
    var reaction by remember { mutableStateOf(existingRecord?.reportedReaction ?: "") }
    var selectedSeverity by remember { mutableStateOf(existingRecord?.severity ?: AllergySeverity.MODERATE.name) }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var onsetDate by remember { mutableStateOf(existingRecord?.onsetDate ?: todayStr) }
    var isVerified by remember { mutableStateOf(existingRecord?.isVerified ?: false) }
    var verifiedBy by remember { mutableStateOf(existingRecord?.verifiedBy ?: "Doctor / Lab Test") }
    var notes by remember { mutableStateOf(existingRecord?.notes ?: "") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (existingRecord == null) "Add Allergy Record" else "Edit Allergy Record", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = substance,
                    onValueChange = { substance = it },
                    label = { Text("Substance Name (e.g. Penicillin, Peanuts)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Text("Category:", fontSize = 12.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AllergyCategory.entries.filter { it != AllergyCategory.ALL }.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(formatCategoryLabel(cat.name)) },
                            colors = futuristicFilterChipColors()
                        )
                    }
                }

                OutlinedTextField(
                    value = reaction,
                    onValueChange = { reaction = it },
                    label = { Text("Reported Reaction (e.g. Rash, Anaphylaxis)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Text("Severity Level:", fontSize = 12.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AllergySeverity.entries.forEach { sev ->
                        FilterChip(
                            selected = selectedSeverity == sev.name,
                            onClick = { selectedSeverity = sev.name },
                            label = { Text(sev.name) },
                            colors = futuristicFilterChipColors()
                        )
                    }
                }

                OutlinedTextField(
                    value = onsetDate,
                    onValueChange = { onsetDate = it },
                    label = { Text("Onset Date (YYYY-MM-DD)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(
                        checked = isVerified,
                        onCheckedChange = { isVerified = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                    )
                    Text("Verified by Physician / Laboratory Test", fontSize = 13.sp, color = TextPrimary)
                }

                if (isVerified) {
                    OutlinedTextField(
                        value = verifiedBy,
                        onValueChange = { verifiedBy = it },
                        label = { Text("Verified By (e.g. Dr. Sharma / Allergy Test)") },
                        singleLine = true,
                        colors = futuristicTextFieldColors()
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Medical remarks") },
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
                    if (substance.isBlank()) {
                        errorMsg = "Please enter substance name."
                        return@Button
                    }

                    isSaving = true
                    errorMsg = null

                    val record = AllergyRecord(
                        id = existingRecord?.id ?: "",
                        substance = substance.trim(),
                        category = selectedCategory,
                        reportedReaction = reaction.trim(),
                        severity = selectedSeverity,
                        onsetDate = onsetDate.trim(),
                        isVerified = isVerified,
                        verifiedBy = if (isVerified) verifiedBy.trim() else "Self-Reported",
                        notes = notes.trim()
                    )

                    repository.saveAllergyRecord(record) { success, err ->
                        isSaving = false
                        if (success) {
                            onSuccess()
                        } else {
                            errorMsg = err ?: "Failed to save allergy record"
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

private fun formatCategoryLabel(catName: String): String = when (catName) {
    "ALL" -> "All Categories"
    "MEDICATION" -> "Medication 💊"
    "FOOD" -> "Food 🥜"
    "ENVIRONMENTAL" -> "Environmental 🌿"
    "OTHER" -> "Other ⚠️"
    else -> catName
}
