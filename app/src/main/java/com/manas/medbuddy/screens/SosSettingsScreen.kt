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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.data.model.EmergencyContact
import com.manas.medbuddy.data.repository.EmergencyContactRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.util.PhoneNumberValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { EmergencyContactRepository.getInstance(context) }
    val contacts by repository.contactsFlow.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<EmergencyContact?>(null) }
    var contactToDelete by remember { mutableStateOf<EmergencyContact?>(null) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contacts & SOS", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyanPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
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
            // Header Info Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Emergency Calling System", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "• Single Tap SOS: Calls your selected primary emergency contact.\n" +
                                "• Triple Tap SOS: Automatically searches for the nearest hospital and prompts to call.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Primary Contact Summary Card
            val primaryContact = contacts.firstOrNull { it.isPrimary } ?: contacts.firstOrNull()
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Primary", tint = SOSRed, modifier = Modifier.size(28.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Primary Contact", color = TextSecondary, fontSize = 12.sp)
                        if (primaryContact != null) {
                            Text(primaryContact.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                            Text(primaryContact.phoneNumber, color = CyanPrimary, fontSize = 14.sp)
                        } else {
                            Text("No Primary Contact Set", fontWeight = FontWeight.Medium, color = SOSRed, fontSize = 16.sp)
                            Text("Tap '+' below to add an emergency contact.", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            Text("All Emergency Contacts (${contacts.size})", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextPrimary)

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No emergency contacts saved yet.\nTap '+' to add your first contact.", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        ContactCard(
                            contact = contact,
                            onSetPrimary = { repository.setPrimaryContact(contact.id) },
                            onEdit = { contactToEdit = contact },
                            onDelete = { contactToDelete = contact }
                        )
                    }
                }
            }
        }
    }

    // Add Contact Dialog
    if (showAddDialog) {
        ContactFormDialog(
            title = "Add Emergency Contact",
            initialContact = null,
            onDismiss = { showAddDialog = false },
            onSave = { contact ->
                repository.saveContact(contact)
                showAddDialog = false
            }
        )
    }

    // Edit Contact Dialog
    contactToEdit?.let { contact ->
        ContactFormDialog(
            title = "Edit Emergency Contact",
            initialContact = contact,
            onDismiss = { contactToEdit = null },
            onSave = { updated ->
                repository.saveContact(updated)
                contactToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    contactToDelete?.let { contact ->
        AlertDialog(
            onDismissRequest = { contactToDelete = null },
            containerColor = DarkSurface,
            title = { Text("Delete Contact", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${contact.name} from emergency contacts?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteContact(contact.id)
                        contactToDelete = null
                    }
                ) {
                    Text("Delete", color = SOSRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { contactToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ContactCard(
    contact: EmergencyContact,
    onSetPrimary: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onSetPrimary) {
                if (contact.isPrimary) {
                    Icon(Icons.Default.Star, contentDescription = "Primary Contact", tint = SOSRed)
                } else {
                    Icon(Icons.Outlined.StarBorder, contentDescription = "Set Primary", tint = TextSecondary)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    if (contact.isPrimary) {
                        Surface(
                            color = SOSRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "PRIMARY",
                                color = SOSRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                    Text(contact.phoneNumber, color = CyanPrimary, fontSize = 14.sp)
                }
                if (contact.relationship.isNotBlank()) {
                    Text("Relationship: ${contact.relationship}", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SOSRed)
                }
            }
        }
    }
}

@Composable
private fun ContactFormDialog(
    title: String,
    initialContact: EmergencyContact?,
    onDismiss: () -> Unit,
    onSave: (EmergencyContact) -> Unit
) {
    var name by remember { mutableStateOf(initialContact?.name ?: "") }
    var phoneNumber by remember { mutableStateOf(initialContact?.phoneNumber ?: "") }
    var relationship by remember { mutableStateOf(initialContact?.relationship ?: "") }
    var isPrimary by remember { mutableStateOf(initialContact?.isPrimary ?: false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Contact Name") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = SOSRed) } },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextSecondary,
                        focusedLabelColor = CyanPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        phoneError = null
                    },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+1 234 567 8900") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it, color = SOSRed) } },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextSecondary,
                        focusedLabelColor = CyanPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship (e.g. Doctor, Spouse)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextSecondary,
                        focusedLabelColor = CyanPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it },
                        colors = CheckboxDefaults.colors(checkedColor = SOSRed)
                    )
                    Text("Set as Primary Emergency Contact", color = TextPrimary, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var valid = true
                    if (name.trim().isEmpty()) {
                        nameError = "Name cannot be empty"
                        valid = false
                    }
                    if (!PhoneNumberValidator.isValid(phoneNumber)) {
                        phoneError = "Enter a valid phone number (7-15 digits)"
                        valid = false
                    }
                    if (valid) {
                        onSave(
                            EmergencyContact(
                                id = initialContact?.id ?: java.util.UUID.randomUUID().toString(),
                                name = name.trim(),
                                phoneNumber = phoneNumber.trim(),
                                relationship = relationship.trim(),
                                isPrimary = isPrimary
                            )
                        )
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
