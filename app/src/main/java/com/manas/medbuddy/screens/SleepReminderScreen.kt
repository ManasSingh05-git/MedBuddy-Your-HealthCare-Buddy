package com.manas.medbuddy.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Warning
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
import androidx.core.content.ContextCompat
import com.manas.medbuddy.data.model.SleepReminderConfig
import com.manas.medbuddy.data.repository.SleepReminderRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.util.SleepCalculator
import com.manas.medbuddy.util.SleepReminderCalculator
import com.manas.medbuddy.util.SleepReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepReminderScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { SleepReminderRepository.getInstance(context) }
    val currentConfig by repository.configFlow.collectAsState()

    var isEnabled by remember(currentConfig) { mutableStateOf(currentConfig.isEnabled) }
    var bedtimeHour by remember(currentConfig) { mutableStateOf(currentConfig.bedtimeHour) }
    var bedtimeMin by remember(currentConfig) { mutableStateOf(currentConfig.bedtimeMinute) }
    var wakeupHour by remember(currentConfig) { mutableStateOf(currentConfig.wakeupHour) }
    var wakeupMin by remember(currentConfig) { mutableStateOf(currentConfig.wakeupMinute) }
    var leadTimeMins by remember(currentConfig) { mutableStateOf(currentConfig.leadTimeMinutes) }
    var activeDays by remember(currentConfig) { mutableStateOf(currentConfig.activeDays.toSet()) }

    var hasNotifPerm by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotifPerm = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Notifications permission denied. Reminders may not show.", Toast.LENGTH_LONG).show()
        }
    }

    val nextReminderMillis = remember(isEnabled, bedtimeHour, bedtimeMin, leadTimeMins, activeDays) {
        val tempConfig = SleepReminderConfig(
            isEnabled = isEnabled,
            bedtimeHour = bedtimeHour,
            bedtimeMinute = bedtimeMin,
            wakeupHour = wakeupHour,
            wakeupMinute = wakeupMin,
            leadTimeMinutes = leadTimeMins,
            activeDays = activeDays.toList()
        )
        SleepReminderCalculator.calculateNextReminderTimeMillis(tempConfig)
    }

    val plannedDurationMins = remember(bedtimeHour, bedtimeMin, wakeupHour, wakeupMin) {
        SleepCalculator.calculateDurationMinutes(bedtimeHour, bedtimeMin, wakeupHour, wakeupMin)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Sleep & Bedtime Reminders", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Enable Switch & Next Reminder Banner
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(
                                if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = if (isEnabled) CyanPrimary else TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text("Enable Sleep Reminders", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Wind down before bedtime", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = {
                                isEnabled = it
                                if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPerm) {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = CyanPrimary)
                        )
                    }

                    if (isEnabled) {
                        Surface(
                            color = CyanPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                SleepReminderCalculator.formatNextReminderText(nextReminderMillis),
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // 2. Permission Status Warning Banner (if denied)
            if (!hasNotifPerm) {
                Surface(
                    color = SOSRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SOSRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = SOSRed)
                        Text("Notification permission is disabled. Tap to grant permission.", color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                openAppSettings(context)
                            }
                        }) {
                            Text("Grant", color = SOSRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Bedtime & Wake-up Time Section
            Text("Sleep Schedule", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Bedtime Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, tint = CyanPrimary)
                            Text("Bedtime", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        TimeInputSelector(
                            hour = bedtimeHour,
                            minute = bedtimeMin,
                            onTimeChange = { h, m ->
                                bedtimeHour = h
                                bedtimeMin = m
                            }
                        )
                    }

                    HorizontalDivider(color = GlassBorder)

                    // Wakeup Time Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFD700))
                            Text("Wake-up Time", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        TimeInputSelector(
                            hour = wakeupHour,
                            minute = wakeupMin,
                            onTimeChange = { h, m ->
                                wakeupHour = h
                                wakeupMin = m
                            }
                        )
                    }

                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Planned Duration: ${SleepCalculator.formatMinutesToHoursMinutes(plannedDurationMins)}",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            // 4. Lead Time Options
            Text("Reminder Lead Time", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Notify me before bedtime:", color = TextSecondary, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { mins ->
                            FilterChip(
                                selected = leadTimeMins == mins,
                                onClick = { leadTimeMins = mins },
                                label = { Text("$mins mins before") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkSurface,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // 5. Active Days Selection
            Text("Active Reminder Days", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { activeDays = (1..7).toSet() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Everyday", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { activeDays = (1..5).toSet() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Weekdays", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { activeDays = setOf(6, 7) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Weekends", fontSize = 11.sp)
                        }
                    }

                    // Individual Day Toggle Chips
                    val dayNames = listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayNames.forEach { (dayIso, label) ->
                            val isSelected = dayIso in activeDays
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    activeDays = if (isSelected) {
                                        activeDays - dayIso
                                    } else {
                                        activeDays + dayIso
                                    }
                                },
                                label = { Text(label, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkSurface,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // 6. Save Button
            Button(
                onClick = {
                    val updatedConfig = SleepReminderConfig(
                        isEnabled = isEnabled,
                        bedtimeHour = bedtimeHour,
                        bedtimeMinute = bedtimeMin,
                        wakeupHour = wakeupHour,
                        wakeupMinute = wakeupMin,
                        leadTimeMinutes = leadTimeMins,
                        activeDays = activeDays.toList()
                    )
                    repository.saveConfig(updatedConfig)
                    Toast.makeText(context, "Sleep reminder settings saved successfully!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Reminder Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun TimeInputSelector(
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit
) {
    var hStr by remember(hour) { mutableStateOf(String.format(java.util.Locale.US, "%02d", hour)) }
    var mStr by remember(minute) { mutableStateOf(String.format(java.util.Locale.US, "%02d", minute)) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = hStr,
            onValueChange = {
                hStr = it
                val parsed = it.toIntOrNull()
                if (parsed != null && parsed in 0..23) {
                    onTimeChange(parsed, minute)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(60.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        Text(":", color = TextPrimary, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = mStr,
            onValueChange = {
                mStr = it
                val parsed = it.toIntOrNull()
                if (parsed != null && parsed in 0..59) {
                    onTimeChange(hour, parsed)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(60.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = android.net.Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback
    }
}
