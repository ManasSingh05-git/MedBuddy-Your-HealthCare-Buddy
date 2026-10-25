package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.data.model.EmergencyContact
import com.manas.medbuddy.data.repository.EmergencyContactRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SOSButton
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.DarkBackground
import com.manas.medbuddy.ui.theme.DarkSurface
import com.manas.medbuddy.ui.theme.EmeraldPrimary
import com.manas.medbuddy.ui.theme.SOSRed
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary
import com.manas.medbuddy.util.EmergencyCallHelper
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
    var show112Dialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Emergency SOS", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Large Central Glowing SOS Button
            SOSButton(
                onClick = {
                    val primary = contacts.firstOrNull { it.isPrimary } ?: contacts.firstOrNull()
                    if (primary != null) {
                        EmergencyCallHelper.initiateCall(context, primary.phoneNumber)
                    } else {
                        show112Dialog = true
                    }
                }
            )

            Text(
                text = "Tap to call emergency\nand find nearest hospitals",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            // 3 Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SosActionTile("Call 112\nEmergency", Icons.Default.Call, SOSRed, Modifier.weight(1f)) {
                    show112Dialog = true
                }
                SosActionTile("Nearest\nHospitals", Icons.Default.MedicalServices, CyanPrimary, Modifier.weight(1f)) {
                    // Quick Action
                }
                SosActionTile("Share\nLocation", Icons.Default.LocationOn, EmeraldPrimary, Modifier.weight(1f)) {
                    // Quick Location
                }
            }

            // Emergency Contacts Header & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Emergency Contacts", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                TextButton(onClick = { showAddDialog = true }) {
                    Text("+ Add", color = CyanPrimary, fontWeight = FontWeight.Bold)
                }
            }

            // Contacts List
            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No emergency contacts saved yet.\nTap '+ Add' to add your primary contact.", color = TextSecondary, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        ContactCard(
                            contact = contact,
                            onSetPrimary = { repository.setPrimaryContact(contact.id) },
                            onEdit = { contactToEdit = contact },
                            onDelete = { contactToDelete = contact },
                            onCall = { EmergencyCallHelper.initiateCall(context, contact.phoneNumber) }
                        )
                    }
                }
            }
        }
    }

    // 112 Confirmation Dialog
    if (show112Dialog) {
        AlertDialog(
            onDismissRequest = { show112Dialog = false },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = SOSRed)
                    Text("Call Emergency 112?", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = { Text("Do you want to initiate a direct call to national emergency services (112)?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        show112Dialog = false
                        EmergencyCallHelper.initiateCall(context, "112")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SOSRed, contentColor = Color.White)
                ) {
                    Text("Call 112", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { show112Dialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Add Dialog
    if (showAddDialog) {
        AddEditContactDialog(
            title = "Add Emergency Contact",
            initialContact = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, rel, isPrimary ->
                repository.saveContact(
                    EmergencyContact(
                        name = name,
                        phoneNumber = phone,
                        relationship = rel,
                        isPrimary = isPrimary
                    )
                )
                showAddDialog = false
            }
        )
    }

    // Edit Dialog
    contactToEdit?.let { contact ->
        AddEditContactDialog(
            title = "Edit Emergency Contact",
            initialContact = contact,
            onDismiss = { contactToEdit = null },
            onSave = { name, phone, rel, isPrimary ->
                repository.saveContact(
                    contact.copy(
                        name = name,
                        phoneNumber = phone,
                        relationship = rel,
                        isPrimary = isPrimary
                    )
                )
                contactToEdit = null
            }
        )
    }

    // Delete Dialog
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
private fun SosActionTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(modifier = modifier.clickable { onClick() }) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Text(title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun ContactCard(
    contact: EmergencyContact,
    onSetPrimary: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circle Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(SOSRed.copy(alpha = 0.2f), CyanPrimary.copy(alpha = 0.2f)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = CyanPrimary)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    if (contact.isPrimary) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SOSRed.copy(alpha = 0.2f)
                        ) {
                            Text("PRIMARY", color = SOSRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                Text("${contact.phoneNumber} • ${contact.relationship}", color = TextSecondary, fontSize = 12.sp)
            }

            IconButton(onClick = onCall) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = EmeraldPrimary)
            }

            IconButton(onClick = onSetPrimary) {
                Icon(
                    imageVector = if (contact.isPrimary) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Primary",
                    tint = if (contact.isPrimary) SOSRed else TextSecondary
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextPrimary)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SOSRed)
            }
        }
    }
}

@Composable
private fun AddEditContactDialog(
    title: String,
    initialContact: EmergencyContact?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, relationship: String, isPrimary: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialContact?.name ?: "") }
    var phone by remember { mutableStateOf(initialContact?.phoneNumber ?: "") }
    var relationship by remember { mutableStateOf(initialContact?.relationship ?: "") }
    var isPrimary by remember { mutableStateOf(initialContact?.isPrimary ?: false) }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship (e.g. Mom, Doctor)", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg.isNotBlank()) {
                    Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        errorMsg = "Please fill in name and phone number."
                    } else if (!PhoneNumberValidator.isValid(phone)) {
                        errorMsg = "Please enter a valid phone number."
                    } else {
                        onSave(name.trim(), phone.trim(), relationship.trim(), isPrimary)
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
