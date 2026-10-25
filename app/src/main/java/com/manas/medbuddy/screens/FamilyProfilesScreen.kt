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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
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
import com.manas.medbuddy.data.model.FamilyProfile
import com.manas.medbuddy.data.repository.FamilyRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyProfilesScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { FamilyRepository.getInstance(context) }
    val profiles by repository.profilesFlow.collectAsState()
    val activeProfileId by repository.activeProfileIdFlow.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var profileToEdit by remember { mutableStateOf<FamilyProfile?>(null) }
    var profileToDelete by remember { mutableStateOf<FamilyProfile?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("👨‍👩‍👦 Family Member Profiles", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                modifier = Modifier.semantics { contentDescription = "Add Family Profile" }
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
            // Data Isolation & Authorization Info Banner
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
                    Text(
                        "Strict data isolation enforced: Health records and medicines are bound securely to the selected profile.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            errorMessage?.let { err ->
                Text(err, color = SOSRed, fontSize = 12.sp)
            }

            SectionHeader(title = "Registered Family Profiles")

            if (profiles.isEmpty()) {
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
                        Text("👥", fontSize = 36.sp)
                        Text("No family profiles found", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Tap + to add a family member profile.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(profiles, key = { it.id }) { profile ->
                        val isActive = activeProfileId == profile.id
                        FamilyProfileCard(
                            profile = profile,
                            isActive = isActive,
                            onSelect = { repository.setActiveProfile(profile.id) },
                            onEdit = { profileToEdit = profile },
                            onDelete = {
                                val error = repository.deleteProfile(profile.id)
                                if (error != null) {
                                    errorMessage = error
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || profileToEdit != null) {
        ProfileFormDialog(
            initialProfile = profileToEdit,
            onDismiss = {
                showAddDialog = false
                profileToEdit = null
            },
            onSave = { profile ->
                val error = if (profileToEdit == null) {
                    repository.addProfile(profile)
                } else {
                    repository.updateProfile(profile)
                }
                if (error != null) {
                    errorMessage = error
                } else {
                    errorMessage = null
                    showAddDialog = false
                    profileToEdit = null
                }
            }
        )
    }
}

@Composable
private fun FamilyProfileCard(
    profile: FamilyProfile,
    isActive: Boolean,
    onSelect: () -> Unit,
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) CyanPrimary.copy(alpha = 0.2f) else DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = if (isActive) CyanPrimary else TextSecondary)
                    }
                    Column {
                        Text(
                            profile.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "${profile.relation} • Age: ${profile.age} • Blood: ${profile.bloodGroup}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SOSRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (profile.isMinor) {
                Surface(
                    color = (if (profile.guardianConsentGranted) EmeraldPrimary else SOSRed).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        if (profile.guardianConsentGranted) "✓ Minor Profile: Guardian Consent Verified" else "⚠️ Minor Profile: Guardian Consent Pending",
                        color = if (profile.guardianConsentGranted) EmeraldPrimary else SOSRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (!isActive) {
                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = CyanPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Switch to this Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    color = CyanPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Active Profile ✓",
                        color = CyanPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileFormDialog(
    initialProfile: FamilyProfile?,
    onDismiss: () -> Unit,
    onSave: (FamilyProfile) -> Unit
) {
    var name by remember { mutableStateOf(initialProfile?.name ?: "") }
    var relation by remember { mutableStateOf(initialProfile?.relation ?: "Child") }
    var ageText by remember { mutableStateOf(initialProfile?.age?.toString() ?: "10") }
    var bloodGroup by remember { mutableStateOf(initialProfile?.bloodGroup ?: "O+") }
    var isMinor by remember { mutableStateOf(initialProfile?.isMinor ?: false) }
    var consentGranted by remember { mutableStateOf(initialProfile?.guardianConsentGranted ?: true) }
    var emergencyContact by remember { mutableStateOf(initialProfile?.emergencyContact ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (initialProfile == null) "Add Family Profile" else "Edit Family Profile", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Full Name", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("Relation (Self, Spouse, Child, Parent)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it; error = null },
                        label = { Text("Age", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = { Text("Blood Group", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Is Minor (< 18 years)?", color = TextPrimary, fontSize = 13.sp)
                    Checkbox(
                        checked = isMinor,
                        onCheckedChange = { isMinor = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                    )
                }

                if (isMinor) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Guardian Consent Granted?", color = TextPrimary, fontSize = 13.sp)
                        Checkbox(
                            checked = consentGranted,
                            onCheckedChange = { consentGranted = it },
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                        )
                    }
                }

                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Emergency Contact Number", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                    val age = ageText.toIntOrNull()
                    if (name.isBlank()) {
                        error = "Please enter a name."
                    } else if (age == null || age < 0 || age > 120) {
                        error = "Please enter a valid age."
                    } else if (isMinor && !consentGranted) {
                        error = "Guardian consent is required for minor profiles."
                    } else {
                        val profile = (initialProfile ?: FamilyProfile(name = name, relation = relation, age = age, bloodGroup = bloodGroup)).copy(
                            name = name,
                            relation = relation,
                            age = age,
                            bloodGroup = bloodGroup,
                            isMinor = isMinor,
                            guardianConsentGranted = if (isMinor) consentGranted else true,
                            emergencyContact = emergencyContact
                        )
                        onSave(profile)
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
