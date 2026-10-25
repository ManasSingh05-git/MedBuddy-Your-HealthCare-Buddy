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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
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
import com.manas.medbuddy.data.model.WellnessGoal
import com.manas.medbuddy.data.model.WellnessSuggestion
import com.manas.medbuddy.data.repository.WellnessCoachRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessCoachScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { WellnessCoachRepository.getInstance(context) }
    val goals by repository.goalsFlow.collectAsState()
    val suggestions by repository.suggestionsFlow.collectAsState()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<WellnessGoal?>(null) }
    var goalToDelete by remember { mutableStateOf<WellnessGoal?>(null) }
    var simulateAiFailure by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("🌿 AI Wellness Habit Coach", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics { contentDescription = "Back" }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isGenerating = true
                        repository.generateWeeklySuggestions(simulateAiFailure)
                        isGenerating = false
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Suggestions", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = CyanPrimary,
                contentColor = Color.Black,
                modifier = Modifier.semantics { contentDescription = "Add Wellness Goal" }
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
            // Disclaimer Banner
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
                    Text(
                        "Wellness coaching suggestions are for lifestyle enhancement and do not replace professional medical advice.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            // Section 1: Wellness Goals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "Your Wellness Goals")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Checkbox(
                        checked = simulateAiFailure,
                        onCheckedChange = { simulateAiFailure = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                    )
                    Text("Simulate AI Fallback", color = TextSecondary, fontSize = 11.sp)
                }
            }

            if (goals.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("No wellness goals set yet. Tap + to create one.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    goals.forEach { goal ->
                        WellnessGoalCard(
                            goal = goal,
                            onEdit = { goalToEdit = goal },
                            onDelete = { goalToDelete = goal }
                        )
                    }
                }
            }

            // Section 2: Weekly AI Suggestions & Rationale
            SectionHeader(title = "Weekly AI Suggestions & Rationale")

            if (suggestions.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("No suggestions available at the moment.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(suggestions, key = { it.id }) { sugg ->
                        WellnessSuggestionCard(
                            suggestion = sugg,
                            onToggleCompletion = { repository.toggleSuggestionCompletion(sugg.id) }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Goal Dialog
    if (showAddGoalDialog || goalToEdit != null) {
        GoalFormDialog(
            initialGoal = goalToEdit,
            onDismiss = {
                showAddGoalDialog = false
                goalToEdit = null
            },
            onSave = { goal ->
                if (goalToEdit == null) {
                    repository.addGoal(goal)
                } else {
                    repository.updateGoal(goal)
                }
                showAddGoalDialog = false
                goalToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    goalToDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            containerColor = DarkSurface,
            title = { Text("Delete Wellness Goal", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${goal.title}'?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteGoal(goal.id)
                        goalToDelete = null
                    }
                ) {
                    Text("Delete", color = SOSRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun WellnessGoalCard(
    goal: WellnessGoal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progressFraction = if (goal.targetValue > 0f) (goal.currentProgress / goal.targetValue).coerceIn(0f, 1f) else 0f

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
                    goal.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${String.format(java.util.Locale.US, "%.1f", goal.currentProgress)} / ${goal.targetValue} ${goal.unit}",
                        color = CyanPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Goal", tint = CyanPrimary, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = SOSRed, modifier = Modifier.size(14.dp))
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = CyanPrimary,
                trackColor = DarkSurface
            )
        }
    }
}

@Composable
private fun WellnessSuggestionCard(
    suggestion: WellnessSuggestion,
    onToggleCompletion: () -> Unit
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
                    suggestion.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Checkbox(
                    checked = suggestion.isCompleted,
                    onCheckedChange = { onToggleCompletion() },
                    colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                )
            }

            Text(
                suggestion.description,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    suggestion.rationale,
                    color = CyanPrimary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(10.dp),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun GoalFormDialog(
    initialGoal: WellnessGoal?,
    onDismiss: () -> Unit,
    onSave: (WellnessGoal) -> Unit
) {
    var title by remember { mutableStateOf(initialGoal?.title ?: "") }
    var targetText by remember { mutableStateOf(initialGoal?.targetValue?.toString() ?: "8.0") }
    var unit by remember { mutableStateOf(initialGoal?.unit ?: "hours") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (initialGoal == null) "Set Wellness Goal" else "Edit Wellness Goal", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("Goal Title (e.g. Daily Hydration)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it; error = null },
                    label = { Text("Target Value", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (e.g. L, hours, steps)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (error != null) {
                    Text(error!!, color = SOSRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedTarget = targetText.toFloatOrNull()
                    if (title.isBlank()) {
                        error = "Please enter a goal title."
                    } else if (parsedTarget == null || parsedTarget <= 0f) {
                        error = "Please enter a valid target value."
                    } else {
                        val goal = (initialGoal ?: WellnessGoal(title = title, targetValue = parsedTarget, unit = unit)).copy(
                            title = title,
                            targetValue = parsedTarget,
                            unit = unit
                        )
                        onSave(goal)
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
