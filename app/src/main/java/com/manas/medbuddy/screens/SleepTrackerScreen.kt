package com.manas.medbuddy.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.data.model.ActiveSleepSession
import com.manas.medbuddy.data.model.SleepRecord
import com.manas.medbuddy.data.repository.SleepRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.util.SleepCalculator
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTrackerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { SleepRepository.getInstance(context) }

    val records by repository.recordsFlow.collectAsState()
    val activeSession by repository.activeSessionFlow.collectAsState()
    val sleepGoal by repository.goalFlow.collectAsState()

    var showStopDialog by remember { mutableStateOf(false) }
    var showAddManualDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<SleepRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<SleepRecord?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }

    // Real-time ticker for active session elapsed time
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeSession) {
        if (activeSession != null) {
            while (true) {
                delay(1000)
                nowMillis = System.currentTimeMillis()
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Sleep Tracker", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showGoalDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Adjust Goal", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        floatingActionButton = {
            if (activeSession == null) {
                FloatingActionButton(
                    onClick = { showAddManualDialog = true },
                    containerColor = CyanPrimary,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Log Sleep Manually")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Active Sleep Tracking Card
            if (activeSession != null) {
                ActiveSleepTrackingCard(
                    session = activeSession!!,
                    nowMillis = nowMillis,
                    onStopSleep = { showStopDialog = true },
                    onCancelSession = { repository.cancelActiveSession() }
                )
            } else {
                StartSleepCard(
                    onStartSleep = { repository.startSleepSession() },
                    onAddManual = { showAddManualDialog = true }
                )
            }

            // 2. Metrics Summary Cards (Last Night, Average, Goal)
            val latestRecord = records.firstOrNull()
            val avgMins = SleepCalculator.calculateAverageSleepMinutes(records, daysLimit = 7)
            val lastNightMins = latestRecord?.durationMinutes ?: 0L

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Last Night", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            if (latestRecord != null) latestRecord.getFormattedDuration() else "No Record",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            if (latestRecord != null) "Wake: ${latestRecord.getFormattedEndTime()}" else "Log sleep below",
                            color = CyanPrimary,
                            fontSize = 11.sp
                        )
                    }
                }

                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("7-Day Avg", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            SleepCalculator.formatMinutesToHoursMinutes(avgMins),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text("Target: ${sleepGoal.toInt()}h 00m", color = CyanPrimary, fontSize = 11.sp)
                    }
                }
            }

            // 3. Goal Progress Card
            val progress = SleepCalculator.calculateGoalProgressPercentage(lastNightMins, sleepGoal)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sleep Goal Progress", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${(progress * 100).toInt()}% of ${sleepGoal.toInt()}h goal", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = CyanPrimary,
                        trackColor = DarkSurface
                    )
                }
            }

            // 4. 7-Day Sleep Duration Chart
            Text("Weekly Sleep Overview", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            SleepWeeklyBarChart(records = records, goalHours = sleepGoal)

            // 5. History Header & List
            Text("Sleep History (${records.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No sleep records logged yet.\nTap 'Start Sleep Session' or '+' to add one.", color = TextSecondary)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    records.forEach { record ->
                        SleepRecordCard(
                            record = record,
                            onEdit = { recordToEdit = record },
                            onDelete = { recordToDelete = record }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Stop Sleep Quick Dialog (Rating & Notes)
    if (showStopDialog) {
        var quality by remember { mutableStateOf(4) }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            containerColor = DarkSurface,
            title = { Text("Complete Sleep Session", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("How restful was your sleep?", color = TextSecondary, fontSize = 14.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { quality = star }) {
                                if (star <= quality) {
                                    Icon(Icons.Default.Star, contentDescription = "$star Stars", tint = Color(0xFFFFD700))
                                } else {
                                    Icon(Icons.Outlined.StarBorder, contentDescription = "$star Stars", tint = TextSecondary)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Sleep Notes (optional)") },
                        placeholder = { Text("e.g. Woke up refreshed") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = TextSecondary,
                            focusedLabelColor = CyanPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.stopSleepSession(qualityRating = quality, notes = notes)
                        showStopDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                ) {
                    Text("Save Sleep", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Manual Add / Edit Dialog
    if (showAddManualDialog) {
        ManualSleepFormDialog(
            title = "Log Manual Sleep Record",
            initialRecord = null,
            existingRecords = records,
            onDismiss = { showAddManualDialog = false },
            onSave = { newRecord ->
                repository.saveSleepRecord(newRecord)
                showAddManualDialog = false
            }
        )
    }

    recordToEdit?.let { record ->
        ManualSleepFormDialog(
            title = "Edit Sleep Record",
            initialRecord = record,
            existingRecords = records,
            onDismiss = { recordToEdit = null },
            onSave = { updated ->
                repository.saveSleepRecord(updated)
                recordToEdit = null
            }
        )
    }

    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            containerColor = DarkSurface,
            title = { Text("Delete Sleep Record", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete the sleep entry for ${record.getFormattedDate()}?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteSleepRecord(record.id)
                        recordToDelete = null
                    }
                ) {
                    Text("Delete", color = SOSRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Goal Adjust Dialog
    if (showGoalDialog) {
        var goalInput by remember { mutableStateOf(sleepGoal.toString()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            containerColor = DarkSurface,
            title = { Text("Set Daily Sleep Goal", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your target daily sleep hours (4 to 12 hours):", color = TextSecondary, fontSize = 14.sp)
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = TextSecondary,
                            focusedLabelColor = CyanPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = goalInput.toFloatOrNull()
                        if (parsed != null && parsed in 4.0f..12.0f) {
                            repository.updateSleepGoal(parsed)
                            showGoalDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                ) {
                    Text("Save Goal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ActiveSleepTrackingCard(
    session: ActiveSleepSession,
    nowMillis: Long,
    onStopSleep: () -> Unit,
    onCancelSession: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sleepPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moonScale"
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(DarkSurface, VioletGradient.copy(alpha = 0.25f))))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Nightlight,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier
                        .size(48.dp)
                        .scale(pulseScale)
                )

                Text("Sleep Session Active", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    session.getFormattedElapsed(nowMillis),
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp
                )

                val startTimeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(Date(session.startTimeMillis))
                Text("Sleeping since $startTimeStr", color = TextSecondary, fontSize = 13.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onStopSleep,
                        colors = ButtonDefaults.buttonColors(containerColor = SOSRed, contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stop Sleep & Save", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onCancelSession,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun StartSleepCard(
    onStartSleep: () -> Unit,
    onAddManual: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Bedtime, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(28.dp))
                Column {
                    Text("Ready for Bed?", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Tap start to begin real-time sleep tracking.", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onStartSleep,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Sleep Session", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onAddManual,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manual")
                }
            }
        }
    }
}

@Composable
private fun SleepWeeklyBarChart(records: List<SleepRecord>, goalHours: Float) {
    val recent7Days = remember(records) {
        val cal = Calendar.getInstance()
        (6 downTo 0).map { dayOffset ->
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_MONTH, -dayOffset)
            val dayStart = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis
            val dayEnd = dayStart + 24 * 60 * 60 * 1000L

            val dayName = SimpleDateFormat("EEE", Locale.US).format(cal.time)
            val dayRecord = records.firstOrNull { it.endTimeMillis in dayStart..dayEnd }
            val hours = dayRecord?.durationHoursFloat ?: 0f
            Pair(dayName, hours)
        }
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxVal = 12f
                    val barWidth = size.width / (recent7Days.size * 2f)
                    val goalY = size.height - (goalHours / maxVal * size.height)

                    // Draw goal line
                    drawLine(
                        color = SOSRed.copy(alpha = 0.5f),
                        start = Offset(0f, goalY),
                        end = Offset(size.width, goalY),
                        strokeWidth = 2f
                    )

                    // Draw day bars
                    recent7Days.forEachIndexed { idx, pair ->
                        val hrs = pair.second
                        val barHeight = (hrs / maxVal * size.height).coerceAtMost(size.height)
                        val x = (idx * 2 + 0.5f) * barWidth
                        val y = size.height - barHeight

                        val barColor = if (hrs >= goalHours) CyanPrimary else BlueGradient

                        drawRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                recent7Days.forEach { (dayLabel, _) ->
                    Text(dayLabel, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SleepRecordCard(
    record: SleepRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Nightlight, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(28.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(record.getFormattedDate(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Text(record.getFormattedDuration(), color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Text("${record.getFormattedStartTime()} - ${record.getFormattedEndTime()}", color = TextSecondary, fontSize = 12.sp)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (star <= record.qualityRating) Color(0xFFFFD700) else TextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    if (record.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("• ${record.notes}", color = TextSecondary, fontSize = 11.sp)
                    }
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
private fun ManualSleepFormDialog(
    title: String,
    initialRecord: SleepRecord?,
    existingRecords: List<SleepRecord>,
    onDismiss: () -> Unit,
    onSave: (SleepRecord) -> Unit
) {
    var startHourStr by remember { mutableStateOf("23") }
    var startMinStr by remember { mutableStateOf("00") }
    var endHourStr by remember { mutableStateOf("07") }
    var endMinStr by remember { mutableStateOf("30") }
    var quality by remember { mutableStateOf(initialRecord?.qualityRating ?: 4) }
    var notes by remember { mutableStateOf(initialRecord?.notes ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Start Time (24h format)", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHourStr,
                        onValueChange = { startHourStr = it },
                        label = { Text("Hour (0-23)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startMinStr,
                        onValueChange = { startMinStr = it },
                        label = { Text("Min (0-59)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Wake-up Time (24h format)", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = endHourStr,
                        onValueChange = { endHourStr = it },
                        label = { Text("Hour (0-23)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endMinStr,
                        onValueChange = { endMinStr = it },
                        label = { Text("Min (0-59)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Sleep Quality Rating", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { quality = star }) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (star <= quality) Color(0xFFFFD700) else TextSecondary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { msg ->
                    Text(msg, color = SOSRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sH = startHourStr.toIntOrNull() ?: 23
                    val sM = startMinStr.toIntOrNull() ?: 0
                    val eH = endHourStr.toIntOrNull() ?: 7
                    val eM = endMinStr.toIntOrNull() ?: 30

                    val now = System.currentTimeMillis()
                    val startMillis = SleepCalculator.calculateMillisFromTime(now, sH, sM, isEndNextDay = false)
                    val endMillis = SleepCalculator.calculateMillisFromTime(now, eH, eM, isEndNextDay = eH < sH || (eH == sH && eM <= sM))

                    val valErr = SleepCalculator.validateSleepRecord(startMillis, endMillis)
                    if (valErr != null) {
                        errorMessage = valErr
                        return@Button
                    }

                    if (SleepCalculator.hasOverlappingRecord(existingRecords, startMillis, endMillis, excludeId = initialRecord?.id)) {
                        errorMessage = "A sleep record already overlaps with this time range."
                        return@Button
                    }

                    onSave(
                        SleepRecord(
                            id = initialRecord?.id ?: java.util.UUID.randomUUID().toString(),
                            startTimeMillis = startMillis,
                            endTimeMillis = endMillis,
                            qualityRating = quality,
                            notes = notes,
                            isAutoTracked = false
                        )
                    )
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
