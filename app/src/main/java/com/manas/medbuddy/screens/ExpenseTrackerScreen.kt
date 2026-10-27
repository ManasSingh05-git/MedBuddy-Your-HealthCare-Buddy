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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import com.manas.medbuddy.expenses.CategoryBreakdownItem
import com.manas.medbuddy.expenses.ExpenseCategory
import com.manas.medbuddy.expenses.ExpenseRepository
import com.manas.medbuddy.expenses.HealthcareExpense
import com.manas.medbuddy.scanner.PrescriptionStorageManager
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseTrackerScreen(
    onBack: () -> Unit,
    repository: ExpenseRepository? = null
) {
    val context = LocalContext.current
    val repo = repository ?: remember { ExpenseRepository(context) }
    val storageManager = remember { PrescriptionStorageManager(context) }

    var expenses by remember { mutableStateOf<List<HealthcareExpense>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.ALL.name) }
    val currentMonthPrefix = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()) }
    var selectedMonthFilter by remember { mutableStateOf(currentMonthPrefix) }

    var showAddModal by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<HealthcareExpense?>(null) }
    var deleteCandidate by remember { mutableStateOf<HealthcareExpense?>(null) }
    var previewExpense by remember { mutableStateOf<HealthcareExpense?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener: ValueEventListener? = try {
            repo.listenToExpenses(
                onUpdate = { list ->
                    expenses = list
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

    val filteredExpenses = remember(expenses, searchQuery, selectedCategory, selectedMonthFilter) {
        val byMonth = repo.filterExpensesByMonth(expenses, selectedMonthFilter)
        byMonth.filter { exp ->
            val matchesCategory = selectedCategory == ExpenseCategory.ALL.name || exp.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    exp.title.contains(searchQuery, ignoreCase = true) ||
                    exp.vendorName.contains(searchQuery, ignoreCase = true) ||
                    exp.notes.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val totalSpend = remember(filteredExpenses) { repo.calculateTotalSpend(filteredExpenses) }
    val breakdownItems = remember(filteredExpenses) { repo.calculateCategoryBreakdown(filteredExpenses) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Healthcare Expenses 💰", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val csvText = repo.generateCsvExportText(filteredExpenses, selectedMonthFilter)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csvText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Healthcare Expense Report"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export Report", tint = CyanPrimary)
                    }
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = CyanPrimary)
                    }
                    IconButton(onClick = {
                        editingExpense = null
                        showAddModal = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface.copy(alpha = 0.9f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingExpense = null
                    showAddModal = true
                },
                containerColor = CyanPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
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
            // Spend Summary Card & Category Breakdown
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
                        Column {
                            Text("Total Healthcare Spend", fontSize = 12.sp, color = TextSecondary)
                            val currencySymbol = filteredExpenses.firstOrNull()?.currency ?: "₹"
                            Text("$currencySymbol ${String.format(Locale.getDefault(), "%.2f", totalSpend)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Text(
                                text = if (selectedMonthFilter == "ALL") "All Time" else selectedMonthFilter,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                        }
                    }

                    if (breakdownItems.isNotEmpty()) {
                        Text("Category Breakdown:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            breakdownItems.forEach { item ->
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(formatCategoryLabel(item.category.name), fontSize = 11.sp, color = TextPrimary)
                                        Text("${item.percentage.toInt()}% (${item.totalAmount.toInt()})", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    LinearProgressIndicator(
                                        progress = { item.percentage / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = getCategoryColor(item.category),
                                        trackColor = DarkSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Month Filter & Search Bar
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search title, vendor...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanPrimary) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                FilterChip(
                    selected = selectedMonthFilter == currentMonthPrefix,
                    onClick = {
                        selectedMonthFilter = if (selectedMonthFilter == currentMonthPrefix) "ALL" else currentMonthPrefix
                    },
                    label = { Text(if (selectedMonthFilter == currentMonthPrefix) "This Month" else "All Time") },
                    colors = futuristicFilterChipColors()
                )
            }

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ExpenseCategory.entries.toTypedArray()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.name,
                        onClick = { selectedCategory = cat.name },
                        label = { Text(formatCategoryLabel(cat.name)) },
                        colors = futuristicFilterChipColors()
                    )
                }
            }

            // Expense List
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            } else if (filteredExpenses.isEmpty()) {
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
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(48.dp))
                        Text("No healthcare expenses recorded", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Record medicines, consultations, lab tests, and attach receipts to track expenses.", fontSize = 13.sp, color = TextSecondary)
                        Button(
                            onClick = {
                                editingExpense = null
                                showAddModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("+ Add Expense Record", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onEdit = {
                                editingExpense = expense
                                showAddModal = true
                            },
                            onPreviewReceipt = { previewExpense = expense },
                            onDelete = { deleteCandidate = expense }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Modal Dialog
    if (showAddModal) {
        AddEditExpenseModal(
            existingExpense = editingExpense,
            onDismiss = { showAddModal = false },
            repository = repo,
            onSuccess = {
                showAddModal = false
                Toast.makeText(context, "Healthcare expense saved!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation
    if (deleteCandidate != null) {
        val target = deleteCandidate!!
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            containerColor = DarkSurface,
            title = { Text("Delete Expense Record", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${target.title}' (${target.currency} ${target.amount})? Attached receipt will be removed.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        repo.deleteExpense(target) { _, _ -> }
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

    // Receipt Preview
    if (previewExpense != null) {
        val target = previewExpense!!
        val bitmap = remember(target.id) {
            if (target.localReceiptPath.isNotBlank()) {
                storageManager.loadBitmapFromFile(File(target.localReceiptPath))
            } else null
        }

        AlertDialog(
            onDismissRequest = { previewExpense = null },
            containerColor = DarkSurface,
            title = { Text("Receipt Preview", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${target.title} • ${target.currency} ${target.amount}", color = TextSecondary, fontSize = 12.sp)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Receipt",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Text("No receipt preview available.", color = SOSRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { previewExpense = null }, colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = DarkSurface,
            title = { Text("Healthcare Expense Guidelines", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Record medicine, consultation, lab test, and insurance expenses.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Attach receipts (stored in private app storage).", color = TextSecondary, fontSize = 13.sp)
                    Text("• Filter by month/year and export CSV summaries.", color = TextSecondary, fontSize = 13.sp)
                    Text("• No payment gateway or auto-billing required.", color = TextSecondary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)) {
                    Text("Got It", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ExpenseCard(
    expense: HealthcareExpense,
    onEdit: () -> Unit,
    onPreviewReceipt: () -> Unit,
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
                Column {
                    Text(expense.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${formatCategoryLabel(expense.category)} • Date: ${expense.date}", fontSize = 12.sp, color = TextSecondary)
                }

                Text("${expense.currency} ${String.format(Locale.getDefault(), "%.2f", expense.amount)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
            }

            if (expense.vendorName.isNotBlank()) {
                Text("Vendor/Clinic: ${expense.vendorName} • Method: ${expense.paymentMethod}", fontSize = 12.sp, color = TextSecondary)
            }

            if (expense.hasReceipt) {
                Button(
                    onClick = onPreviewReceipt,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Receipt", fontSize = 12.sp, color = TextPrimary)
                }
            }

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyanPrimary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SOSRed, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddEditExpenseModal(
    existingExpense: HealthcareExpense?,
    onDismiss: () -> Unit,
    repository: ExpenseRepository,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf(existingExpense?.title ?: "") }
    var amountInput by remember { mutableStateOf(existingExpense?.amount?.toString() ?: "") }
    var currency by remember { mutableStateOf(existingExpense?.currency ?: "₹") }
    var selectedCategory by remember { mutableStateOf(existingExpense?.category ?: ExpenseCategory.MEDICINE.name) }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(existingExpense?.date ?: todayStr) }
    var paymentMethod by remember { mutableStateOf(existingExpense?.paymentMethod ?: "UPI / Online") }
    var vendorName by remember { mutableStateOf(existingExpense?.vendorName ?: "") }
    var notes by remember { mutableStateOf(existingExpense?.notes ?: "") }

    var selectedReceiptUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val currencies = listOf("₹", "$", "€", "£")
    val paymentMethods = listOf("UPI / Online", "Cash", "Credit Card", "Insurance Claim")

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedReceiptUri = uri
            errorMsg = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (existingExpense == null) "Add Healthcare Expense" else "Edit Expense", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                    label = { Text("Expense Title") },
                    placeholder = { Text("e.g. Monthly Medicines, Blood Test") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount") },
                        placeholder = { Text("e.g. 1250.00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = futuristicTextFieldColors()
                    )

                    Column {
                        Text("Currency:", fontSize = 11.sp, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            currencies.forEach { curr ->
                                FilterChip(
                                    selected = currency == curr,
                                    onClick = { currency = curr },
                                    label = { Text(curr) },
                                    colors = futuristicFilterChipColors()
                                )
                            }
                        }
                    }
                }

                Text("Category:", fontSize = 12.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExpenseCategory.entries.filter { it != ExpenseCategory.ALL }.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(formatCategoryLabel(cat.name)) },
                            colors = futuristicFilterChipColors()
                        )
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                Text("Payment Method:", fontSize = 12.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    paymentMethods.forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method) },
                            colors = futuristicFilterChipColors()
                        )
                    }
                }

                OutlinedTextField(
                    value = vendorName,
                    onValueChange = { vendorName = it },
                    label = { Text("Vendor / Pharmacy Name") },
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
                    Text(if (selectedReceiptUri == null && !(existingExpense?.hasReceipt == true)) "Attach Receipt (PDF / Image)" else "Receipt Attached ✓", color = CyanPrimary)
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Remarks") },
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
                    val amount = amountInput.toDoubleOrNull()
                    if (title.isBlank()) {
                        errorMsg = "Please enter expense title."
                        return@Button
                    }
                    if (amount == null || amount <= 0.0) {
                        errorMsg = "Please enter a valid expense amount."
                        return@Button
                    }

                    isSaving = true
                    errorMsg = null

                    val expense = HealthcareExpense(
                        id = existingExpense?.id ?: "",
                        title = title.trim(),
                        amount = amount,
                        currency = currency,
                        category = selectedCategory,
                        date = date.trim(),
                        paymentMethod = paymentMethod,
                        vendorName = vendorName.trim(),
                        notes = notes.trim(),
                        localReceiptPath = existingExpense?.localReceiptPath ?: "",
                        hasReceipt = existingExpense?.hasReceipt ?: false
                    )

                    repository.saveExpense(expense, selectedReceiptUri) { success, _ ->
                        isSaving = false
                        if (success) {
                            onSuccess()
                        } else {
                            errorMsg = "Failed to save expense record."
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text(if (existingExpense == null) "Save Expense" else "Update Expense", color = Color.Black, fontWeight = FontWeight.Bold)
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
    "MEDICINE" -> "Medicine 💊"
    "CONSULTATION" -> "Consultation 🩺"
    "LAB_TEST" -> "Lab Tests 🧪"
    "INSURANCE_PREMIUM" -> "Insurance 🛡️"
    "PROCEDURE" -> "Procedure 🏥"
    "OTHER" -> "Other 📄"
    else -> catName
}

private fun getCategoryColor(category: ExpenseCategory): Color = when (category) {
    ExpenseCategory.MEDICINE -> CyanPrimary
    ExpenseCategory.CONSULTATION -> Color(0xFF81C784)
    ExpenseCategory.LAB_TEST -> Color(0xFFFFB74D)
    ExpenseCategory.INSURANCE_PREMIUM -> Color(0xFF64B5F6)
    ExpenseCategory.PROCEDURE -> Color(0xFFBA68C8)
    else -> TextSecondary
}
