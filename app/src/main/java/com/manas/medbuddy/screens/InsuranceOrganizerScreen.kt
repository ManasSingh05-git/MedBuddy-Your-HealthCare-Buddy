package com.manas.medbuddy.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
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
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.insurance.ClaimStatus
import com.manas.medbuddy.insurance.InsuranceClaim
import com.manas.medbuddy.insurance.InsurancePolicy
import com.manas.medbuddy.insurance.InsuranceRepository
import com.manas.medbuddy.insurance.PolicyVerificationType
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.model.ReminderTime
import com.manas.medbuddy.model.ReminderType
import com.manas.medbuddy.reminder.ReminderScheduler
import com.manas.medbuddy.scanner.PrescriptionStorageManager
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.util.EmergencyCallHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InsuranceOrganizerScreen(
    onBack: () -> Unit,
    repository: InsuranceRepository? = null
) {
    val context = LocalContext.current
    val repo = repository ?: remember { InsuranceRepository(context) }
    val scheduler = remember { ReminderScheduler(context) }
    val storageManager = remember { PrescriptionStorageManager(context) }

    var policies by remember { mutableStateOf<List<InsurancePolicy>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var showAddModal by remember { mutableStateOf(false) }
    var editingPolicy by remember { mutableStateOf<InsurancePolicy?>(null) }
    var addClaimTarget by remember { mutableStateOf<InsurancePolicy?>(null) }
    var deleteCandidate by remember { mutableStateOf<InsurancePolicy?>(null) }
    var previewDocPath by remember { mutableStateOf<String?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener: ValueEventListener? = try {
            repo.listenToPolicies(
                onUpdate = { list ->
                    policies = list
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

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Health Insurance Organizer 🛡️", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                        editingPolicy = null
                        showAddModal = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Policy", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface.copy(alpha = 0.9f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingPolicy = null
                    showAddModal = true
                },
                containerColor = CyanPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Policy")
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
            // Mandatory Disclaimer Banner
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = CyanPrimary)
                        Text("🛡️ Personal Health Insurance Vault", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 13.sp)
                    }
                    Text("MedBuddy helps you securely store policy documents and claim records. MedBuddy does not guarantee claim approval or coverage processing without direct insurer verification.", fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                }
            }

            // List View
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            } else if (policies.isEmpty()) {
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
                        Text("No insurance policies stored", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Store policy numbers, coverage details, claim receipts, and set expiration alerts.", fontSize = 13.sp, color = TextSecondary)
                        Button(
                            onClick = {
                                editingPolicy = null
                                showAddModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("+ Add Health Insurance Policy", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(policies, key = { it.id }) { policy ->
                        PolicyCard(
                            policy = policy,
                            repository = repo,
                            onEdit = {
                                editingPolicy = policy
                                showAddModal = true
                            },
                            onAddClaim = { addClaimTarget = policy },
                            onPreviewDocument = { path -> previewDocPath = path },
                            onDelete = { deleteCandidate = policy }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Policy Modal
    if (showAddModal) {
        AddEditPolicyModal(
            existingPolicy = editingPolicy,
            onDismiss = { showAddModal = false },
            repository = repo,
            scheduler = scheduler,
            onSuccess = {
                showAddModal = false
                Toast.makeText(context, "Insurance policy saved!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Add Claim Modal
    if (addClaimTarget != null) {
        val target = addClaimTarget!!
        AddClaimModal(
            policy = target,
            onDismiss = { addClaimTarget = null },
            repository = repo,
            onSuccess = {
                addClaimTarget = null
                Toast.makeText(context, "Claim record added!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Modal
    if (deleteCandidate != null) {
        val target = deleteCandidate!!
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            containerColor = DarkSurface,
            title = { Text("Delete Insurance Policy", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete policy '${target.insurerName}' (${target.policyNumber})? Attached documents will be deleted.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        repo.deletePolicy(target) { _, _ -> }
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

    // Document Preview
    if (previewDocPath != null) {
        val path = previewDocPath!!
        val bitmap = remember(path) {
            storageManager.loadBitmapFromFile(File(path))
        }

        AlertDialog(
            onDismissRequest = { previewDocPath = null },
            containerColor = DarkSurface,
            title = { Text("Policy Document / Receipt Preview", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Document",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Text("Preview unavailable for this attachment format.", color = SOSRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { previewDocPath = null }, colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = DarkSurface,
            title = { Text("Insurance Organizer Guidelines", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Explicitly distinguishes insurer-verified documents from personal notes.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Organizes claim bills and tracks claim status.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Offers configurable policy expiry reminders.", color = TextSecondary, fontSize = 13.sp)
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

@Composable
private fun PolicyCard(
    policy: InsurancePolicy,
    repository: InsuranceRepository,
    onEdit: () -> Unit,
    onAddClaim: () -> Unit,
    onPreviewDocument: (String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val daysToExpiry = repository.calculateDaysToExpiry(policy.expiryDate)
    val isVerifiedDoc = policy.verificationType == PolicyVerificationType.INSURER_VERIFIED_DOC.name

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
                    Text(policy.insurerName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Policy #: ${policy.policyNumber}", fontSize = 12.sp, color = TextSecondary)
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

            // Fact Verification Badge (Mandatory Requirement)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (isVerifiedDoc) Color(0xFF1B5E20).copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (isVerifiedDoc) Color(0xFF81C784) else CyanPrimary)
                ) {
                    Text(
                        text = if (isVerifiedDoc) "🛡️ Insurer-Verified Document" else "📝 User Self-Reported Notes",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isVerifiedDoc) Color(0xFF81C784) else CyanPrimary
                    )
                }

                // Expiry Pill
                Surface(
                    color = if (daysToExpiry <= 30) SOSRed.copy(alpha = 0.2f) else DarkSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (daysToExpiry <= 30) SOSRed else GlassBorder)
                ) {
                    Text(
                        text = if (daysToExpiry <= 0) "❌ EXPIRED" else "⏰ Expires in $daysToExpiry Days",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (daysToExpiry <= 30) SOSRed else TextPrimary
                    )
                }
            }

            Text("Plan: ${policy.planType} • Sum Insured: ₹ ${policy.sumInsured.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text("Valid: ${policy.startDate} to ${policy.expiryDate}", fontSize = 12.sp, color = TextSecondary)

            if (policy.insurerContactPhone.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Helpline: ${policy.insurerContactPhone}", fontSize = 12.sp, color = TextSecondary)
                    Button(
                        onClick = { EmergencyCallHelper.initiateCall(context, policy.insurerContactPhone) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call Insurer", tint = CyanPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call Insurer", fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }

            if (policy.hasDocument) {
                Button(
                    onClick = { onPreviewDocument(policy.localDocumentPath) },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Policy Document", fontSize = 12.sp, color = TextPrimary)
                }
            }

            // Claims Section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Claim Records (${policy.claims.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    TextButton(onClick = onAddClaim) {
                        Text("+ Add Claim Bill", fontSize = 11.sp, color = CyanPrimary)
                    }
                }

                if (policy.claims.isEmpty()) {
                    Text("No claim bills recorded under this policy.", fontSize = 11.sp, color = TextSecondary)
                } else {
                    policy.claims.forEach { claim ->
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Claim #: ${claim.claimNumber} (${claim.hospitalName})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                    Text("Amount: ₹ ${claim.claimAmount.toInt()} • Status: ${claim.status}", fontSize = 11.sp, color = TextSecondary)
                                }

                                if (claim.hasBillAttachment) {
                                    IconButton(onClick = { onPreviewDocument(claim.localBillPath) }) {
                                        Icon(Icons.Default.ReceiptLong, contentDescription = "Bill", tint = CyanPrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddEditPolicyModal(
    existingPolicy: InsurancePolicy?,
    onDismiss: () -> Unit,
    repository: InsuranceRepository,
    scheduler: ReminderScheduler,
    onSuccess: () -> Unit
) {
    var insurerName by remember { mutableStateOf(existingPolicy?.insurerName ?: "") }
    var policyNumber by remember { mutableStateOf(existingPolicy?.policyNumber ?: "") }
    var planType by remember { mutableStateOf(existingPolicy?.planType ?: "Health Shield Plan") }
    var sumInsuredInput by remember { mutableStateOf(existingPolicy?.sumInsured?.toString() ?: "500000") }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var startDate by remember { mutableStateOf(existingPolicy?.startDate ?: todayStr) }
    var expiryDate by remember { mutableStateOf(existingPolicy?.expiryDate ?: "") }
    var phone by remember { mutableStateOf(existingPolicy?.insurerContactPhone ?: "") }
    var email by remember { mutableStateOf(existingPolicy?.insurerSupportEmail ?: "") }
    var verificationType by remember { mutableStateOf(existingPolicy?.verificationType ?: PolicyVerificationType.USER_SELF_REPORTED.name) }
    var notes by remember { mutableStateOf(existingPolicy?.coverageNotes ?: "") }

    var selectedDocUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedDocUri = uri
            errorMsg = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (existingPolicy == null) "Add Health Insurance Policy" else "Edit Policy", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = insurerName,
                    onValueChange = { insurerName = it },
                    label = { Text("Insurer Name") },
                    placeholder = { Text("e.g. Star Health, HDFC ERGO") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = policyNumber,
                    onValueChange = { policyNumber = it },
                    label = { Text("Policy Identifier / Number") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = planType,
                    onValueChange = { planType = it },
                    label = { Text("Plan Type") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = sumInsuredInput,
                    onValueChange = { sumInsuredInput = it },
                    label = { Text("Sum Insured Amount (₹)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = futuristicTextFieldColors()
                    )

                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Expiry Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = futuristicTextFieldColors()
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Helpline Phone") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = futuristicTextFieldColors()
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Support Email") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = futuristicTextFieldColors()
                    )
                }

                Text("Verification Source:", fontSize = 12.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = verificationType == PolicyVerificationType.USER_SELF_REPORTED.name,
                        onClick = { verificationType = PolicyVerificationType.USER_SELF_REPORTED.name },
                        label = { Text("User Notes") },
                        colors = futuristicFilterChipColors()
                    )
                    FilterChip(
                        selected = verificationType == PolicyVerificationType.INSURER_VERIFIED_DOC.name,
                        onClick = { verificationType = PolicyVerificationType.INSURER_VERIFIED_DOC.name },
                        label = { Text("Insurer Verified Doc") },
                        colors = futuristicFilterChipColors()
                    )
                }

                Button(
                    onClick = { filePicker.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedDocUri == null && !(existingPolicy?.hasDocument == true)) "Attach Policy Document (PDF/Image)" else "Document Attached ✓", color = CyanPrimary)
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Coverage Notes & Terms") },
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
                    val sumInsured = sumInsuredInput.toDoubleOrNull() ?: 500000.0
                    if (insurerName.isBlank() || policyNumber.isBlank()) {
                        errorMsg = "Please enter insurer name and policy number."
                        return@Button
                    }

                    isSaving = true
                    errorMsg = null

                    val policy = InsurancePolicy(
                        id = existingPolicy?.id ?: "",
                        insurerName = insurerName.trim(),
                        policyNumber = policyNumber.trim(),
                        planType = planType.trim(),
                        sumInsured = sumInsured,
                        startDate = startDate.trim(),
                        expiryDate = expiryDate.trim(),
                        insurerContactPhone = phone.trim(),
                        insurerSupportEmail = email.trim(),
                        verificationType = verificationType,
                        coverageNotes = notes.trim(),
                        localDocumentPath = existingPolicy?.localDocumentPath ?: "",
                        hasDocument = existingPolicy?.hasDocument ?: false,
                        claims = existingPolicy?.claims ?: emptyList()
                    )

                    repository.savePolicy(policy, selectedDocUri) { success, id ->
                        isSaving = false
                        if (success) {
                            val dummyMed = Medicine(
                                id = "policy_${id ?: policy.id}",
                                name = "POLICY EXPIRY ALERT: ${policy.insurerName}",
                                dosage = "Policy #${policy.policyNumber} expires on ${policy.expiryDate}",
                                reminderTimes = listOf(
                                    ReminderTime(id = "1", label = "Insurance Expiry", time = "09:00 AM", type = ReminderType.CUSTOM.name)
                                )
                            )
                            scheduler.scheduleMedicineReminders(dummyMed)
                            onSuccess()
                        } else {
                            errorMsg = "Failed to save insurance policy."
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text(if (existingPolicy == null) "Save Policy" else "Update Policy", color = Color.Black, fontWeight = FontWeight.Bold)
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
private fun AddClaimModal(
    policy: InsurancePolicy,
    onDismiss: () -> Unit,
    repository: InsuranceRepository,
    onSuccess: () -> Unit
) {
    var claimNumber by remember { mutableStateOf("") }
    var hospitalName by remember { mutableStateOf("") }
    var claimAmountInput by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(ClaimStatus.SUBMITTED.name) }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var submissionDate by remember { mutableStateOf(todayStr) }

    var selectedBillUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) selectedBillUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Add Claim Record", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Policy: ${policy.insurerName} (${policy.policyNumber})", color = TextSecondary, fontSize = 12.sp)

                OutlinedTextField(
                    value = claimNumber,
                    onValueChange = { claimNumber = it },
                    label = { Text("Claim Number") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    label = { Text("Hospital Name") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = claimAmountInput,
                    onValueChange = { claimAmountInput = it },
                    label = { Text("Claim Amount (₹)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Button(
                    onClick = { filePicker.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedBillUri == null) "Attach Bill / Hospital Receipt" else "Bill Attached ✓", color = CyanPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val claimAmount = claimAmountInput.toDoubleOrNull() ?: 0.0
                    isSaving = true
                    val claim = InsuranceClaim(
                        claimNumber = claimNumber.trim(),
                        hospitalName = hospitalName.trim(),
                        claimAmount = claimAmount,
                        status = status,
                        submissionDate = submissionDate.trim()
                    )

                    repository.addClaimToPolicy(policy, claim, selectedBillUri) { success, _ ->
                        isSaving = false
                        if (success) onSuccess()
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Save Claim", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
