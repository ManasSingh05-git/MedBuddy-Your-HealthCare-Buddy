package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import com.manas.medbuddy.data.model.MovementRoutine
import com.manas.medbuddy.data.model.MovementSessionLog
import com.manas.medbuddy.data.repository.MovementCoachRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementCoachScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { MovementCoachRepository.getInstance(context) }
    val routines by repository.routinesFlow.collectAsState()
    val logs by repository.logsFlow.collectAsState()
    val intervalMinutes by repository.intervalMinutesFlow.collectAsState()
    val remindersEnabled by repository.remindersEnabledFlow.collectAsState()
    val quietHours by repository.quietHoursFlow.collectAsState()
    val reducedMotion by repository.reducedMotionFlow.collectAsState()

    var activeRoutine by remember { mutableStateOf<MovementRoutine?>(null) }
    var timeLeftSeconds by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Session Timer coroutine ticker
    LaunchedEffect(isTimerRunning, timeLeftSeconds) {
        if (isTimerRunning && timeLeftSeconds > 0) {
            delay(1000)
            timeLeftSeconds--
        } else if (isTimerRunning && timeLeftSeconds == 0) {
            isTimerRunning = false
            activeRoutine?.let {
                repository.logCompletedSession(it.title)
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("🧘 Posture & Movement Coach", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics { contentDescription = "Back" }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Movement Coaching Note (No automated posture detection claims)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
                    Text(
                        "General movement & stretching routine guide. (Note: Automated camera/sensor posture detection is not implemented).",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            // Active Session Timer / Routine View
            activeRoutine?.let { routine ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(routine.title, color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        val mins = timeLeftSeconds / 60
                        val secs = timeLeftSeconds % 60
                        Text(
                            String.format(java.util.Locale.US, "%02d:%02d", mins, secs),
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 42.sp
                        )

                        // Instructions
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            routine.instructions.forEach { step ->
                                Text("• $step", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { isTimerRunning = !isTimerRunning },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isTimerRunning) Color(0xFFFFD700) else EmeraldPrimary, contentColor = Color.Black),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isTimerRunning) "Pause" else "Start", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    repository.logCompletedSession(routine.title)
                                    isTimerRunning = false
                                    activeRoutine = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Complete", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    isTimerRunning = false
                                    activeRoutine = null
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SOSRed)
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            }

            // Section 1: Movement Routines
            SectionHeader(title = "Stretching & Movement Routines")

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(routines, key = { it.id }) { routine ->
                    MovementRoutineCard(
                        routine = routine,
                        reducedMotion = reducedMotion,
                        onStart = {
                            activeRoutine = routine
                            timeLeftSeconds = routine.durationSeconds
                            isTimerRunning = true
                        }
                    )
                }

                item {
                    SectionHeader(title = "Completion History")
                }

                if (logs.isEmpty()) {
                    item {
                        Text("No movement sessions logged yet.", color = TextSecondary, fontSize = 12.sp)
                    }
                } else {
                    items(logs, key = { it.id }) { log ->
                        SessionLogCard(log = log)
                    }
                }
            }
        }
    }

    // Settings Dialog (Intervals, Reminders, Quiet Hours, Opt-Out, Reduced Motion)
    if (showSettingsDialog) {
        MovementSettingsDialog(
            initialInterval = intervalMinutes,
            initialReminders = remindersEnabled,
            initialQuiet = quietHours,
            initialReduced = reducedMotion,
            onDismiss = { showSettingsDialog = false },
            onSave = { interval, rem, quiet, red ->
                repository.updatePreferences(interval, rem, quiet, red)
                showSettingsDialog = false
            }
        )
    }
}

@Composable
private fun MovementRoutineCard(
    routine: MovementRoutine,
    reducedMotion: Boolean,
    onStart: () -> Unit
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
                    Text(
                        routine.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "Duration: ${routine.durationSeconds / 60} mins ${if (reducedMotion) "• [Reduced Motion]" else ""}",
                        color = CyanPrimary,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                ) {
                    Text("Start Routine", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SessionLogCard(log: MovementSessionLog) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
            Column {
                Text(log.routineTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(log.dateString, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun MovementSettingsDialog(
    initialInterval: Int,
    initialReminders: Boolean,
    initialQuiet: String,
    initialReduced: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int, Boolean, String, Boolean) -> Unit
) {
    var interval by remember { mutableStateOf(initialInterval.toString()) }
    var reminders by remember { mutableStateOf(initialReminders) }
    var quiet by remember { mutableStateOf(initialQuiet) }
    var reduced by remember { mutableStateOf(initialReduced) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Movement Break Preferences", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = interval,
                    onValueChange = { interval = it; error = null },
                    label = { Text("Break Interval (Minutes)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Movement Reminders", color = TextPrimary, fontSize = 13.sp)
                    Checkbox(checked = reminders, onCheckedChange = { reminders = it }, colors = CheckboxDefaults.colors(checkedColor = CyanPrimary))
                }

                OutlinedTextField(
                    value = quiet,
                    onValueChange = { quiet = it },
                    label = { Text("Quiet Hours (e.g. 22:00 - 07:00)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Reduced Motion Mode", color = TextPrimary, fontSize = 13.sp)
                    Checkbox(checked = reduced, onCheckedChange = { reduced = it }, colors = CheckboxDefaults.colors(checkedColor = CyanPrimary))
                }

                if (error != null) {
                    Text(error!!, color = SOSRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val intVal = interval.toIntOrNull()
                    if (intVal == null || intVal <= 0) {
                        error = "Please enter a valid interval in minutes."
                    } else {
                        onSave(intVal, reminders, quiet, reduced)
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
