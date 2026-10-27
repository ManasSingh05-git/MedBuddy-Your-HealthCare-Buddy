package com.manas.medbuddy.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.data.MedicineRepository
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.model.ReminderTime
import com.manas.medbuddy.model.ReminderType
import com.manas.medbuddy.refill.MedicineRefillConfig
import com.manas.medbuddy.refill.RefillCalculator
import com.manas.medbuddy.refill.RefillPlannerRepository
import com.manas.medbuddy.refill.RefillStockStatus
import com.manas.medbuddy.reminder.ReminderScheduler
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RefillPlannerScreen(
    onBack: () -> Unit,
    refillRepository: RefillPlannerRepository? = null,
    medicineRepository: MedicineRepository? = null
) {
    val context = LocalContext.current
    val refillRepo = refillRepository ?: remember { RefillPlannerRepository(context) }
    val medRepo = medicineRepository ?: remember { MedicineRepository(context) }
    val scheduler = remember { ReminderScheduler(context) }
    val calculator = remember { RefillCalculator() }

    var refillConfigs by remember { mutableStateOf<List<MedicineRefillConfig>>(emptyList()) }
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var showConfigModal by remember { mutableStateOf(false) }
    var editingConfig by remember { mutableStateOf<MedicineRefillConfig?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val refillListener: ValueEventListener? = try {
            refillRepo.listenToRefillConfigs(
                onUpdate = { list -> refillConfigs = list },
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

        onDispose {
            if (refillListener != null) refillRepo.removeListener(refillListener)
            if (medListener != null) medRepo.removeMedicinesListener(medListener)
        }
    }

    val lowStockCount = remember(refillConfigs) {
        refillConfigs.count { it.stockStatus == RefillStockStatus.LOW_STOCK.name || it.stockStatus == RefillStockStatus.OUT_OF_STOCK.name }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Prescription Refill Planner 💊", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Disclaimer Info", tint = CyanPrimary)
                    }
                    IconButton(onClick = {
                        editingConfig = null
                        showConfigModal = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Configure Refill", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface.copy(alpha = 0.9f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingConfig = null
                    showConfigModal = true
                },
                containerColor = CyanPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Configure Refill")
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
                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanPrimary)
                        Text("📊 Estimated Refill Date — Schedule-Based Planning", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 13.sp)
                    }
                    Text("MedBuddy calculates estimated run-out dates based on your daily schedule. MedBuddy never makes automatic purchases or changes prescribed dosages.", fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                }
            }

            // Summary Metric Row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tracked Medicines", fontSize = 11.sp, color = TextSecondary)
                        Text("${refillConfigs.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Surface(
                    color = if (lowStockCount > 0) SOSRed.copy(alpha = 0.2f) else DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (lowStockCount > 0) SOSRed else GlassBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Low Stock Alerts", fontSize = 11.sp, color = if (lowStockCount > 0) SOSRed else TextSecondary)
                        Text("$lowStockCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (lowStockCount > 0) SOSRed else TextPrimary)
                    }
                }
            }

            // List View
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            } else if (medicines.isEmpty()) {
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
                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(48.dp))
                        Text("No active medicines found", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Add medicines to your schedule first, then configure refill tracking.", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(medicines, key = { it.id }) { med ->
                        val config = refillConfigs.find { it.medicineId == med.id } ?: MedicineRefillConfig(
                            medicineId = med.id,
                            medicineName = med.name,
                            dosageInfo = med.dosage,
                            dailyDoseConsumption = calculator.estimateDefaultDailyConsumption(med)
                        )

                        RefillMedicineCard(
                            medicine = med,
                            config = config,
                            calculator = calculator,
                            onRestock = { additional ->
                                refillRepo.restockPackage(config, additional) { success, _ ->
                                    if (success) Toast.makeText(context, "Restocked +$additional units for ${med.name}!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onConfigure = {
                                editingConfig = config
                                showConfigModal = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Configure Modal
    if (showConfigModal) {
        ConfigureRefillModal(
            existingConfig = editingConfig,
            medicines = medicines,
            onDismiss = { showConfigModal = false },
            repository = refillRepo,
            scheduler = scheduler,
            calculator = calculator,
            onSuccess = {
                showConfigModal = false
                Toast.makeText(context, "Refill settings updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = DarkSurface,
            title = { Text("Prescription Refill Planner Rules", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Links directly to registered medicines to avoid duplicate records.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Calculates remaining stock and estimated run-out date from daily schedule.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Notifies before estimated refill date.", color = TextSecondary, fontSize = 13.sp)
                    Text("• Does NOT make automatic purchases or dosage alterations.", color = TextSecondary, fontSize = 13.sp)
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
private fun RefillMedicineCard(
    medicine: Medicine,
    config: MedicineRefillConfig,
    calculator: RefillCalculator,
    onRestock: (Int) -> Unit,
    onConfigure: () -> Unit
) {
    val remainingDays = calculator.calculateRemainingDays(config.currentStockRemaining, config.dailyDoseConsumption)
    val runOutDate = calculator.calculateRunOutDate(config.currentStockRemaining, config.dailyDoseConsumption)
    val status = calculator.evaluateStockStatus(config.currentStockRemaining, config.dailyDoseConsumption, config.lowStockThresholdDays)

    val progress = if (config.totalPackageQuantity > 0) {
        (config.currentStockRemaining.toFloat() / config.totalPackageQuantity.toFloat()).coerceIn(0f, 1f)
    } else 0f

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
                    Text(medicine.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${medicine.dosage} • Schedule: ${medicine.time}", fontSize = 12.sp, color = TextSecondary)
                }

                IconButton(onClick = onConfigure) {
                    Icon(Icons.Default.Edit, contentDescription = "Configure", tint = CyanPrimary, modifier = Modifier.size(20.dp))
                }
            }

            // Stock Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Stock Remaining: ${config.currentStockRemaining} / ${config.totalPackageQuantity} Units", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when (status) {
                        RefillStockStatus.OUT_OF_STOCK -> SOSRed
                        RefillStockStatus.LOW_STOCK -> Color(0xFFFFB74D)
                        else -> CyanPrimary
                    },
                    trackColor = DarkSurface
                )
            }

            // Status Badge & Calculation
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = when (status) {
                        RefillStockStatus.OUT_OF_STOCK -> SOSRed.copy(alpha = 0.2f)
                        RefillStockStatus.LOW_STOCK -> Color(0xFFF57F17).copy(alpha = 0.2f)
                        else -> Color(0xFF1B5E20).copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        when (status) {
                            RefillStockStatus.OUT_OF_STOCK -> SOSRed
                            RefillStockStatus.LOW_STOCK -> Color(0xFFFFB74D)
                            else -> Color(0xFF81C784)
                        }
                    )
                ) {
                    Text(
                        text = when (status) {
                            RefillStockStatus.OUT_OF_STOCK -> "❌ OUT OF STOCK"
                            RefillStockStatus.LOW_STOCK -> "⚠️ LOW STOCK ALERT"
                            else -> "✓ STOCK SUFFICIENT"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            RefillStockStatus.OUT_OF_STOCK -> SOSRed
                            RefillStockStatus.LOW_STOCK -> Color(0xFFFFB74D)
                            else -> Color(0xFF81C784)
                        }
                    )
                }

                Text("• $remainingDays Days Remaining", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }

            Text("Estimated Run-Out Date: $runOutDate (at ${config.dailyDoseConsumption} units/day)", fontSize = 12.sp, color = TextSecondary)

            if (config.pharmacyNotes.isNotBlank()) {
                Text("Pharmacy Notes: ${config.pharmacyNotes}", fontSize = 11.sp, color = TextSecondary)
            }

            // Restock Action Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Button(
                    onClick = { onRestock(30) },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+30 Restock", fontSize = 11.sp, color = TextPrimary)
                }

                Button(
                    onClick = { onRestock(10) },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+10 Units", fontSize = 11.sp, color = TextPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConfigureRefillModal(
    existingConfig: MedicineRefillConfig?,
    medicines: List<Medicine>,
    onDismiss: () -> Unit,
    repository: RefillPlannerRepository,
    scheduler: ReminderScheduler,
    calculator: RefillCalculator,
    onSuccess: () -> Unit
) {
    var selectedMed by remember { mutableStateOf(medicines.find { it.id == existingConfig?.medicineId } ?: medicines.firstOrNull()) }
    var packageQtyInput by remember { mutableStateOf(existingConfig?.totalPackageQuantity?.toString() ?: "30") }
    var currentStockInput by remember { mutableStateOf(existingConfig?.currentStockRemaining?.toString() ?: "30") }
    var dailyConsumptionInput by remember { mutableStateOf(existingConfig?.dailyDoseConsumption?.toString() ?: "2.0") }
    var thresholdDaysInput by remember { mutableStateOf(existingConfig?.lowStockThresholdDays?.toString() ?: "3") }
    var pharmacyNotes by remember { mutableStateOf(existingConfig?.pharmacyNotes ?: "") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Configure Prescription Refill", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Select Medicine:", fontSize = 12.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    medicines.forEach { med ->
                        FilterChip(
                            selected = selectedMed?.id == med.id,
                            onClick = {
                                selectedMed = med
                                dailyConsumptionInput = calculator.estimateDefaultDailyConsumption(med).toString()
                            },
                            label = { Text(med.name) },
                            colors = futuristicFilterChipColors()
                        )
                    }
                }

                OutlinedTextField(
                    value = packageQtyInput,
                    onValueChange = { packageQtyInput = it },
                    label = { Text("Total Package Size (e.g. 30 units)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = currentStockInput,
                    onValueChange = { currentStockInput = it },
                    label = { Text("Current Stock Remaining in Pillbox") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = dailyConsumptionInput,
                    onValueChange = { dailyConsumptionInput = it },
                    label = { Text("Daily Dose Consumption Rate (units/day)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = thresholdDaysInput,
                    onValueChange = { thresholdDaysInput = it },
                    label = { Text("Alert Threshold (Days before run-out)") },
                    singleLine = true,
                    colors = futuristicTextFieldColors()
                )

                OutlinedTextField(
                    value = pharmacyNotes,
                    onValueChange = { pharmacyNotes = it },
                    label = { Text("Pharmacy Notes / Preferred Chemist") },
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
                    val med = selectedMed ?: run {
                        errorMsg = "Please select a medicine."
                        return@Button
                    }

                    val totalPkg = packageQtyInput.toIntOrNull() ?: 30
                    val currentStock = currentStockInput.toIntOrNull() ?: 30
                    val dailyCons = dailyConsumptionInput.toDoubleOrNull() ?: 1.0
                    val thresholdDays = thresholdDaysInput.toIntOrNull() ?: 3

                    isSaving = true
                    errorMsg = null

                    val config = MedicineRefillConfig(
                        id = existingConfig?.id ?: "",
                        medicineId = med.id,
                        medicineName = med.name,
                        dosageInfo = med.dosage,
                        totalPackageQuantity = totalPkg,
                        currentStockRemaining = currentStock,
                        dailyDoseConsumption = dailyCons,
                        lowStockThresholdDays = thresholdDays,
                        pharmacyNotes = pharmacyNotes.trim()
                    )

                    repository.saveRefillConfig(config) { success, id ->
                        isSaving = false
                        if (success) {
                            val alertDate = calculator.calculateRefillAlertDate(currentStock, dailyCons, thresholdDays)
                            val dummyMed = Medicine(
                                id = "refill_${id ?: config.id}",
                                name = "REFILL ALERT: ${med.name} Low Stock!",
                                dosage = "Estimated run-out soon. ${config.pharmacyNotes}",
                                reminderTimes = listOf(
                                    ReminderTime(id = "1", label = "Refill Reminder", time = "09:00 AM", type = ReminderType.CUSTOM.name)
                                )
                            )
                            scheduler.scheduleMedicineReminders(dummyMed)
                            onSuccess()
                        } else {
                            errorMsg = "Failed to save refill settings."
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Save Refill Planner", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
