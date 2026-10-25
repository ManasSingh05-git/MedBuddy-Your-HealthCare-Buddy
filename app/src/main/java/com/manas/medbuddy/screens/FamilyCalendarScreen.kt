package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
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
import com.manas.medbuddy.data.model.CalendarEvent
import com.manas.medbuddy.data.repository.FamilyCalendarRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyCalendarScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { FamilyCalendarRepository.getInstance(context) }
    val allEvents by repository.eventsFlow.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf("All") } // "All", "Appointment", "Vaccination", "Medicine"
    var selectedMemberFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val members = remember(allEvents) {
        listOf("All") + allEvents.map { it.memberName }.distinct()
    }

    val filteredEvents = remember(allEvents, selectedCategoryFilter, selectedMemberFilter) {
        repository.getFilteredEvents(selectedCategoryFilter, selectedMemberFilter, includePrivate = true)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("📅 Family Health Calendar", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                onClick = { showAddDialog = true },
                containerColor = CyanPrimary,
                contentColor = Color.Black,
                modifier = Modifier.semantics { contentDescription = "Add Calendar Event" }
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
            // Category Filter Tabs
            SectionHeader(title = "Filter by Category")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Appointment", "Vaccination", "Medicine").forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Surface(
                        onClick = { selectedCategoryFilter = cat },
                        color = if (isSelected) CyanPrimary else DarkSurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                cat,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Member Filter Chips
            if (members.size > 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    members.forEach { mem ->
                        val isSelected = selectedMemberFilter == mem
                        Surface(
                            onClick = { selectedMemberFilter = mem },
                            color = if (isSelected) EmeraldPrimary else DarkSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                mem,
                                color = if (isSelected) Color.Black else TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            errorMessage?.let { err ->
                Text(err, color = SOSRed, fontSize = 12.sp)
            }

            SectionHeader(title = "Agenda Events")

            if (filteredEvents.isEmpty()) {
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
                        Text("🗓️", fontSize = 36.sp)
                        Text("No health events match filters", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Tap + to add an appointment, vaccination, or medicine schedule.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        CalendarEventCard(
                            event = event,
                            onDelete = { repository.deleteEvent(event.id) }
                        )
                    }
                }
            }
        }
    }

    // Add Event Dialog
    if (showAddDialog) {
        EventFormDialog(
            onDismiss = { showAddDialog = false },
            onSave = { event ->
                val error = repository.addEvent(event)
                if (error != null) {
                    errorMessage = error
                } else {
                    errorMessage = null
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
private fun CalendarEventCard(
    event: CalendarEvent,
    onDelete: () -> Unit
) {
    val categoryColor = when (event.category) {
        "Appointment" -> CyanPrimary
        "Vaccination" -> EmeraldPrimary
        else -> Color(0xFFFFD700)
    }

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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = categoryColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            event.category,
                            color = categoryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        event.memberName,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Event", tint = SOSRed, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                event.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                event.dateTimeString,
                color = CyanPrimary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun EventFormDialog(
    onDismiss: () -> Unit,
    onSave: (CalendarEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Appointment") } // Appointment, Vaccination, Medicine
    var memberName by remember { mutableStateOf("Manas Singh") }
    var dateTimeStr by remember { mutableStateOf("Tomorrow, 10:00 AM") }
    var isPrivate by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Add Health Event", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("Event Title", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Appointment, Vaccination, Medicine)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = memberName,
                    onValueChange = { memberName = it },
                    label = { Text("Family Member Name", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = dateTimeStr,
                    onValueChange = { dateTimeStr = it },
                    label = { Text("Date & Time String", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Private Event (Privacy Permission)", color = TextPrimary, fontSize = 13.sp)
                    Checkbox(checked = isPrivate, onCheckedChange = { isPrivate = it }, colors = CheckboxDefaults.colors(checkedColor = SOSRed))
                }

                if (error != null) {
                    Text(error!!, color = SOSRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        error = "Please enter an event title."
                    } else {
                        val event = CalendarEvent(
                            title = title,
                            category = if (category.isNotBlank()) category else "Appointment",
                            memberName = if (memberName.isNotBlank()) memberName else "Self",
                            dateTimeString = dateTimeStr,
                            timestamp = System.currentTimeMillis() + 10000L,
                            isPrivate = isPrivate
                        )
                        onSave(event)
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
