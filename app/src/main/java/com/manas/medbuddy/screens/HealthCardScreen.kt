package com.manas.medbuddy.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.allergy.AllergyRecord
import com.manas.medbuddy.allergy.AllergyRepository
import com.manas.medbuddy.card.HealthCardConfig
import com.manas.medbuddy.card.HealthCardRepository
import com.manas.medbuddy.data.MedicineRepository
import com.manas.medbuddy.data.model.EmergencyContact
import com.manas.medbuddy.data.repository.EmergencyContactRepository
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.util.EmergencyCallHelper

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HealthCardScreen(
    onBack: () -> Unit,
    cardRepository: HealthCardRepository? = null,
    contactRepository: EmergencyContactRepository? = null,
    medicineRepository: MedicineRepository? = null,
    allergyRepository: AllergyRepository? = null
) {
    val context = LocalContext.current
    val cardRepo = cardRepository ?: remember { HealthCardRepository(context) }
    val contactRepo = contactRepository ?: remember { EmergencyContactRepository.getInstance(context) }
    val medRepo = medicineRepository ?: remember { MedicineRepository(context) }
    val allergyRepo = allergyRepository ?: remember { AllergyRepository(context) }

    var cardConfig by remember { mutableStateOf(HealthCardConfig()) }
    var contacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var allergies by remember { mutableStateOf<List<AllergyRecord>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }
    var isPrivacyRedacted by remember { mutableStateOf(true) }
    var showEditModal by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val cardListener: ValueEventListener? = try {
            cardRepo.listenToHealthCardConfig(
                onUpdate = { config ->
                    cardConfig = config
                    isPrivacyRedacted = config.hideSensitiveDetailsOnLockScreen
                },
                onError = {}
            )
        } catch (_: Exception) { null }

        val medListener: ValueEventListener? = try {
            medRepo.listenToMedicines(
                onUpdate = { list ->
                    medicines = list
                    loading = false
                },
                onError = { loading = false }
            )
        } catch (_: Exception) {
            loading = false
            null
        }

        val allergyListener: ValueEventListener? = try {
            allergyRepo.listenToAllergies(
                onUpdate = { list, _ -> allergies = list },
                onError = {}
            )
        } catch (_: Exception) { null }

        contacts = try { contactRepo.getContacts() } catch (_: Exception) { emptyList() }

        onDispose {
            if (cardListener != null) cardRepo.removeListener(cardListener)
            if (medListener != null) medRepo.removeMedicinesListener(medListener)
            if (allergyListener != null) allergyRepo.removeListener(allergyListener)
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Personal Health Card 💳", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareText = cardRepo.generateShareableSummaryText(cardConfig, contacts, medicines, allergies, isPrivacyRedacted)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Emergency Health Card"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = CyanPrimary)
                    }
                    IconButton(onClick = { showEditModal = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Card", tint = CyanPrimary)
                    }
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = CyanPrimary)
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
            // Privacy Control Banner
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isPrivacyRedacted) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (isPrivacyRedacted) CyanPrimary else SOSRed
                        )
                        Column {
                            Text(
                                text = if (isPrivacyRedacted) "Privacy Mode: Sensitive Info Redacted" else "Full Emergency View: All Info Unmasked",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isPrivacyRedacted) "Sensitive notes and drug lists are hidden by default." else "All medical notes, drugs, and allergy details visible.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = !isPrivacyRedacted,
                        onCheckedChange = { isPrivacyRedacted = !it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                    )
                }
            }

            // Main Emergency Health Badge
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Row: Name & Blood Group
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(cardConfig.fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            if (cardConfig.dateOfBirth.isNotBlank()) {
                                Text("DOB: ${cardConfig.dateOfBirth}", fontSize = 12.sp, color = TextSecondary)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (cardConfig.showBloodGroup && cardConfig.bloodGroup.isNotBlank()) {
                                Surface(
                                    color = SOSRed.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, SOSRed)
                                ) {
                                    Text(
                                        text = "🩸 ${cardConfig.bloodGroup}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SOSRed
                                    )
                                }
                            }

                            if (cardConfig.showOrganDonor && cardConfig.isOrganDonor) {
                                Surface(
                                    color = Color(0xFF1B5E20).copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, Color(0xFF81C784))
                                ) {
                                    Text(
                                        text = "🫀 Organ Donor",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF81C784)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = GlassBorder)

                    // Primary Physician
                    if (cardConfig.primaryPhysicianName.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Primary Physician", fontSize = 12.sp, color = TextSecondary)
                                Text(cardConfig.primaryPhysicianName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            if (cardConfig.primaryPhysicianPhone.isNotBlank()) {
                                Button(
                                    onClick = { EmergencyCallHelper.initiateCall(context, cardConfig.primaryPhysicianPhone) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call Doctor", fontSize = 12.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    // Emergency Contacts Section
                    if (cardConfig.showEmergencyContacts) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📞 Emergency Contacts", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                            if (contacts.isEmpty()) {
                                Text("No emergency contacts configured.", fontSize = 12.sp, color = TextSecondary)
                            } else {
                                contacts.forEach { contact ->
                                    Surface(
                                        color = DarkSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, GlassBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("${contact.name} (${contact.relationship})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                                Text(contact.phoneNumber, fontSize = 12.sp, color = TextSecondary)
                                            }
                                            IconButton(
                                                onClick = { EmergencyCallHelper.initiateCall(context, contact.phoneNumber) }
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = "Call", tint = CyanPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Recorded Allergies Section
                    if (cardConfig.showAllergies) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("⚠️ Recorded Allergies", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                            if (allergies.isEmpty()) {
                                Text("No known allergies recorded.", fontSize = 12.sp, color = TextSecondary)
                            } else {
                                allergies.forEach { allergy ->
                                    Surface(
                                        color = DarkSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, GlassBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(allergy.substance, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                                Surface(
                                                    color = if (allergy.isVerified) Color(0xFF1B5E20).copy(alpha = 0.2f) else SOSRed.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = if (allergy.isVerified) "✓ Verified" else "⚠️ Self-Reported",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (allergy.isVerified) Color(0xFF81C784) else SOSRed
                                                    )
                                                }
                                            }

                                            if (isPrivacyRedacted) {
                                                Text("[REDACTED ALLERGY REACTION - UNLOCK TO VIEW]", fontSize = 11.sp, color = TextSecondary)
                                            } else {
                                                Text("Reaction: ${allergy.reportedReaction} (${allergy.severity})", fontSize = 12.sp, color = TextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Current Active Medicines
                    if (cardConfig.showMedicines) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💊 Current Active Medicines", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                            if (medicines.isEmpty()) {
                                Text("No active medicines scheduled.", fontSize = 12.sp, color = TextSecondary)
                            } else {
                                medicines.forEach { med ->
                                    Surface(
                                        color = DarkSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, GlassBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = if (isPrivacyRedacted) "[REDACTED DRUG NAME]" else med.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = if (isPrivacyRedacted) "Unlock card to view dosage" else "${med.dosage} • ${med.time}",
                                                    fontSize = 12.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Important Medical Notes
                    if (cardConfig.showEmergencyNotes && cardConfig.emergencyNotes.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📝 Critical Medical Alerts & Notes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                            Surface(
                                color = DarkSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isPrivacyRedacted) "[MASKED FOR PRIVACY - UNLOCK TO VIEW NOTES]" else cardConfig.emergencyNotes,
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Health Card Modal
    if (showEditModal) {
        EditHealthCardModal(
            currentConfig = cardConfig,
            onDismiss = { showEditModal = false },
            repository = cardRepo,
            onSuccess = {
                showEditModal = false
                Toast.makeText(context, "Health card updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = DarkSurface,
            title = { Text("Personal Health Card Privacy", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Field Selection: Toggle which fields are visible on your card.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Lock Screen Privacy: Redacts sensitive notes and drug names until unmasked.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Verification Badges: Labels physician-verified vs self-reported allergies.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Export & Share: Formats a clean text summary for emergency responders.", color = TextSecondary, fontSize = 13.sp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditHealthCardModal(
    currentConfig: HealthCardConfig,
    onDismiss: () -> Unit,
    repository: HealthCardRepository,
    onSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf(currentConfig.fullName) }
    var dob by remember { mutableStateOf(currentConfig.dateOfBirth) }
    var bloodGroup by remember { mutableStateOf(currentConfig.bloodGroup) }
    var isOrganDonor by remember { mutableStateOf(currentConfig.isOrganDonor) }
    var physicianName by remember { mutableStateOf(currentConfig.primaryPhysicianName) }
    var physicianPhone by remember { mutableStateOf(currentConfig.primaryPhysicianPhone) }
    var emergencyNotes by remember { mutableStateOf(currentConfig.emergencyNotes) }

    var showBloodGroup by remember { mutableStateOf(currentConfig.showBloodGroup) }
    var showOrganDonor by remember { mutableStateOf(currentConfig.showOrganDonor) }
    var showMedicines by remember { mutableStateOf(currentConfig.showMedicines) }
    var showAllergies by remember { mutableStateOf(currentConfig.showAllergies) }
    var showContacts by remember { mutableStateOf(currentConfig.showEmergencyContacts) }
    var showNotes by remember { mutableStateOf(currentConfig.showEmergencyNotes) }
    var hideSensitiveOnLockScreen by remember { mutableStateOf(currentConfig.hideSensitiveDetailsOnLockScreen) }

    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Configure Emergency Card", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Date of Birth") },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = futuristicTextFieldColors()
                    )

                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = { Text("Blood Group") },
                        placeholder = { Text("e.g. O+") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = futuristicTextFieldColors()
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Registered Organ Donor", fontSize = 13.sp, color = TextPrimary)
                    Switch(checked = isOrganDonor, onCheckedChange = { isOrganDonor = it }, colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary))
                }

                OutlinedTextField(
                    value = physicianName,
                    onValueChange = { physicianName = it },
                    label = { Text("Primary Doctor Name") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = physicianPhone,
                    onValueChange = { physicianPhone = it },
                    label = { Text("Doctor Phone Number") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = emergencyNotes,
                    onValueChange = { emergencyNotes = it },
                    label = { Text("Critical Medical Notes / Conditions") },
                    colors = futuristicTextFieldColors()
                )

                Text("Field Visibility Controls:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)

                ToggleRow("Show Blood Group", showBloodGroup) { showBloodGroup = it }
                ToggleRow("Show Organ Donor Status", showOrganDonor) { showOrganDonor = it }
                ToggleRow("Show Active Medicines", showMedicines) { showMedicines = it }
                ToggleRow("Show Recorded Allergies", showAllergies) { showAllergies = it }
                ToggleRow("Show Emergency Contacts", showContacts) { showContacts = it }
                ToggleRow("Show Critical Notes", showNotes) { showNotes = it }
                ToggleRow("Mask Sensitive Details by Default", hideSensitiveOnLockScreen) { hideSensitiveOnLockScreen = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSaving = true
                    val updated = currentConfig.copy(
                        fullName = fullName.trim(),
                        dateOfBirth = dob.trim(),
                        bloodGroup = bloodGroup.trim(),
                        isOrganDonor = isOrganDonor,
                        primaryPhysicianName = physicianName.trim(),
                        primaryPhysicianPhone = physicianPhone.trim(),
                        emergencyNotes = emergencyNotes.trim(),
                        showBloodGroup = showBloodGroup,
                        showOrganDonor = showOrganDonor,
                        showMedicines = showMedicines,
                        showAllergies = showAllergies,
                        showEmergencyContacts = showContacts,
                        showEmergencyNotes = showNotes,
                        hideSensitiveDetailsOnLockScreen = hideSensitiveOnLockScreen
                    )

                    repository.saveHealthCardConfig(updated) { success, _ ->
                        isSaving = false
                        if (success) onSuccess()
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Save Settings", color = Color.Black, fontWeight = FontWeight.Bold)
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
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
        )
    }
}
