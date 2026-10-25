package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.data.model.CaregiverPermission
import com.manas.medbuddy.data.repository.CaregiverRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverDashboardScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { CaregiverRepository.getInstance(context) }
    val permissions by repository.permissionsFlow.collectAsState()

    var showGrantDialog by remember { mutableStateOf(false) }
    var selectedPermissionId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(permissions) {
        if (permissions.isNotEmpty() && selectedPermissionId == null) {
            selectedPermissionId = permissions.first().id
        }
    }

    val activePermission = permissions.find { it.id == selectedPermissionId }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("🤝 Caregiver Dashboard & Sharing", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                onClick = { showGrantDialog = true },
                containerColor = CyanPrimary,
                contentColor = Color.Black,
                modifier = Modifier.semantics { contentDescription = "Grant Caregiver Access" }
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
            // Security Banner
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
                    Text(
                        "Server-side authorization enforced: Caregivers only access health records explicitly covered by active, unrevoked permissions.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            SectionHeader(title = "Authorized Caregivers & Permissions")

            if (permissions.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("No caregiver permissions granted yet.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    permissions.forEach { perm ->
                        val isSelected = selectedPermissionId == perm.id
                        Surface(
                            onClick = { selectedPermissionId = perm.id },
                            color = if (isSelected) CyanPrimary.copy(alpha = 0.25f) else DarkSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(perm.caregiverName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                Text(if (perm.isRevoked) "Revoked" else "Active", color = if (perm.isRevoked) SOSRed else EmeraldPrimary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Caregiver View / Shared Health Summary
            activePermission?.let { perm ->
                SectionHeader(title = "Caregiver View for: ${perm.caregiverName}")

                if (perm.isRevoked) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🔒 Access Revoked", color = SOSRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("This caregiver's permissions have been revoked. No health records are exposed.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Adherence Summary
                            Text("Adherence Summary:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            val adherence = repository.getSharedAdherenceSummary(perm.id)
                            Text(adherence, color = TextPrimary, fontSize = 13.sp)

                            Spacer(modifier = Modifier.height(4.dp))
                            // Upcoming Appointments
                            Text("Upcoming Appointments:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            val appts = repository.getSharedUpcomingAppointments(perm.id)
                            if (appts.isEmpty()) {
                                Text("No appointments visible (permission restricted or empty).", color = TextSecondary, fontSize = 12.sp)
                            } else {
                                appts.forEach { appt ->
                                    Text("• $appt", color = TextPrimary, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            // Revoke Button
                            Button(
                                onClick = { repository.revokePermission(perm.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = SOSRed, contentColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Revoke Caregiver Access", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Grant Permission Dialog
    if (showGrantDialog) {
        GrantPermissionDialog(
            onDismiss = { showGrantDialog = false },
            onGrant = { perm ->
                repository.grantPermission(perm)
                selectedPermissionId = perm.id
                showGrantDialog = false
            }
        )
    }
}

@Composable
private fun GrantPermissionDialog(
    onDismiss: () -> Unit,
    onGrant: (CaregiverPermission) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var canAdherence by remember { mutableStateOf(true) }
    var canAppointments by remember { mutableStateOf(true) }
    var canVitals by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Grant Caregiver Access", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Caregiver Name", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; error = null },
                    label = { Text("Caregiver Email", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Share Adherence Summary", color = TextPrimary, fontSize = 13.sp)
                    Checkbox(checked = canAdherence, onCheckedChange = { canAdherence = it }, colors = CheckboxDefaults.colors(checkedColor = CyanPrimary))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Share Appointments", color = TextPrimary, fontSize = 13.sp)
                    Checkbox(checked = canAppointments, onCheckedChange = { canAppointments = it }, colors = CheckboxDefaults.colors(checkedColor = CyanPrimary))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Share Vitals / Reports", color = TextPrimary, fontSize = 13.sp)
                    Checkbox(checked = canVitals, onCheckedChange = { canVitals = it }, colors = CheckboxDefaults.colors(checkedColor = CyanPrimary))
                }

                if (error != null) {
                    Text(error!!, color = SOSRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank()) {
                        error = "Please fill in all required fields."
                    } else {
                        val perm = CaregiverPermission(
                            caregiverName = name,
                            caregiverEmail = email,
                            canViewAdherence = canAdherence,
                            canViewAppointments = canAppointments,
                            canViewVitals = canVitals,
                            isRevoked = false
                        )
                        onGrant(perm)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
            ) {
                Text("Grant Access", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
