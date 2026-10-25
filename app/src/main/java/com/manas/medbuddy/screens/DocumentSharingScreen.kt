package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
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
import com.manas.medbuddy.data.model.DocumentAuditLog
import com.manas.medbuddy.data.model.SharedDocument
import com.manas.medbuddy.data.repository.DocumentSharingRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentSharingScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { DocumentSharingRepository.getInstance(context) }
    val documents by repository.documentsFlow.collectAsState()
    val auditLogs by repository.auditFlow.collectAsState()

    var showShareDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Shared Docs") } // "Shared Docs", "Audit Log"
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("🔒 Secure Document Sharing", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                onClick = { showShareDialog = true },
                containerColor = CyanPrimary,
                contentColor = Color.Black,
                modifier = Modifier.semantics { contentDescription = "Share Document" }
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
            // Security Info Banner
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
                    Text(
                        "Tokenized secure handles enforced: Direct public storage URLs are never exposed. All access is authorized and audited.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            // Tab Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Shared Docs", "Audit Log").forEach { tab ->
                    val isSelected = selectedTab == tab
                    Surface(
                        onClick = { selectedTab = tab },
                        color = if (isSelected) CyanPrimary else DarkSurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                tab,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            errorMessage?.let { err ->
                Text(err, color = SOSRed, fontSize = 12.sp)
            }

            if (selectedTab == "Shared Docs") {
                SectionHeader(title = "Explicitly Shared Documents")

                if (documents.isEmpty()) {
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
                            Text("📂", fontSize = 36.sp)
                            Text("No shared documents found", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Tap + to explicitly share a medical document.", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(documents, key = { it.id }) { doc ->
                            SharedDocumentCard(
                                document = doc,
                                onRevoke = { repository.revokeDocument(doc.id) },
                                onTestAccess = {
                                    val handle = repository.accessDocument(doc.id)
                                    if (handle == null) {
                                        errorMessage = "Access Blocked: Document is revoked or unauthorized."
                                    } else {
                                        errorMessage = "Access Granted (Secure Handle: $handle)"
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                SectionHeader(title = "Sharing Audit Trail")

                if (auditLogs.isEmpty()) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No audit logs recorded yet.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(auditLogs, key = { it.id }) { log ->
                            AuditLogCard(log = log)
                        }
                    }
                }
            }
        }
    }

    // Share Document Dialog
    if (showShareDialog) {
        ShareFormDialog(
            onDismiss = { showShareDialog = false },
            onShare = { title, owner, recipient ->
                val error = repository.shareDocument(title, owner, recipient)
                if (error != null) {
                    errorMessage = error
                } else {
                    errorMessage = null
                    showShareDialog = false
                }
            }
        )
    }
}

@Composable
private fun SharedDocumentCard(
    document: SharedDocument,
    onRevoke: () -> Unit,
    onTestAccess: () -> Unit
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
                Text(
                    document.documentTitle,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Surface(
                    color = (if (document.isRevoked) SOSRed else EmeraldPrimary).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        if (document.isRevoked) "Revoked" else "Active",
                        color = if (document.isRevoked) SOSRed else EmeraldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text("Owner: ${document.ownerName} • Recipient: ${document.recipientName}", color = TextSecondary, fontSize = 12.sp)
            Text("Secure Handle: ${document.secureHandle}", color = CyanPrimary, fontSize = 11.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTestAccess,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = CyanPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Test Access", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (!document.isRevoked) {
                    Button(
                        onClick = onRevoke,
                        colors = ButtonDefaults.buttonColors(containerColor = SOSRed, contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Revoke Access", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(log: DocumentAuditLog) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(log.action, color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(log.documentTitle, color = TextSecondary, fontSize = 11.sp)
            }
            Text(log.details, color = TextPrimary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ShareFormDialog(
    onDismiss: () -> Unit,
    onShare: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("Manas Singh") }
    var recipient by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Explicitly Share Document", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("Document Title (e.g. MRI Report)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = owner,
                    onValueChange = { owner = it },
                    label = { Text("Owner Name", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it; error = null },
                    label = { Text("Recipient (Family Member / Doctor)", color = TextSecondary) },
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
                    if (title.isBlank() || recipient.isBlank()) {
                        error = "Please fill in all required fields."
                    } else {
                        onShare(title, owner, recipient)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
            ) {
                Text("Share Securely", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
