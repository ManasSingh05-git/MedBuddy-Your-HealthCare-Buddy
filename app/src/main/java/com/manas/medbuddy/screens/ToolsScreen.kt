package com.manas.medbuddy.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.ui.components.BottomNavigationBar
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.DarkBackground
import com.manas.medbuddy.ui.theme.DarkSurface
import com.manas.medbuddy.ui.theme.EmeraldPrimary
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onNavigateToRoute: (String) -> Unit = {},
    onTabSelected: (String) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Tools & Features", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = "tools",
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("All", "Core Features", "More Tools", "Care & Family").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        onClick = { selectedFilter = filter },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) EmeraldPrimary else DarkSurface,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // SECTION 1: CORE FEATURES
            if (selectedFilter == "All" || selectedFilter == "Core Features") {
                Text(
                    text = "Core Features",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolButtonTile("Health Timeline", Modifier.weight(1f)) { onNavigateToRoute("health_timeline") }
                        ToolButtonTile("Health Card", Modifier.weight(1f)) { onNavigateToRoute("health_card") }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolButtonTile("Vaccines", Modifier.weight(1f)) { onNavigateToRoute("vaccination_manager") }
                        ToolButtonTile("Allergies", Modifier.weight(1f)) { onNavigateToRoute("allergy_registry") }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolButtonTile("Medical Vault", Modifier.weight(1f)) { onNavigateToRoute("medical_vault") }
                        ToolButtonTile("Rx Scanner", Modifier.weight(1f)) { onNavigateToRoute("prescription_scanner") }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolButtonTile("Reports", Modifier.weight(1f)) { onNavigateToRoute("report_explainer") }
                        ToolButtonTile("Appointments", Modifier.weight(1f)) { onNavigateToRoute("appointment_organizer") }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolButtonTile("Sleep Tracker", Modifier.weight(1f)) { onNavigateToRoute("sleep_tracker") }
                        ToolButtonTile("Hospital Finder", Modifier.weight(1f)) { onNavigateToRoute("hospital_finder") }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolButtonTile("Health Map", Modifier.weight(1f)) { onNavigateToRoute("health_map") }
                        ToolButtonTile("SOS Contacts", Modifier.weight(1f)) { onNavigateToRoute("sos_settings") }
                    }
                }
            }

            // SECTION 2: MORE TOOLS
            if (selectedFilter == "All" || selectedFilter == "More Tools" || selectedFilter == "Care & Family") {
                Text(
                    text = "More Tools",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolButtonTile("Symptom Journal", Modifier.weight(1f)) { onNavigateToRoute("symptom_journal") }
                        ToolButtonTile("Doctor Prep", Modifier.weight(1f)) { onNavigateToRoute("doctor_prep") }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolButtonTile("Medical Glossary", Modifier.weight(1f)) { onNavigateToRoute("medical_glossary") }
                        ToolButtonTile("Wellness Coach", Modifier.weight(1f)) { onNavigateToRoute("wellness_coach") }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolButtonTile("Report Compare", Modifier.weight(1f)) { onNavigateToRoute("report_comparison") }
                        ToolButtonTile("Family Profiles", Modifier.weight(1f)) { onNavigateToRoute("family_profiles") }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolButtonTile("Caregiver Dash", Modifier.weight(1f)) { onNavigateToRoute("caregiver_dashboard") }
                        ToolButtonTile("Family Calendar", Modifier.weight(1f)) { onNavigateToRoute("family_calendar") }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolButtonTile("Adherence Reports", Modifier.weight(1f)) { onNavigateToRoute("adherence_reports") }
                        ToolButtonTile("Document Sharing", Modifier.weight(1f)) { onNavigateToRoute("document_sharing") }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolButtonTile("Menstrual Tracker", Modifier.weight(1f)) { onNavigateToRoute("menstrual_tracker") }
                        ToolButtonTile("Movement Coach", Modifier.weight(1f)) { onNavigateToRoute("movement_coach") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ToolButtonTile(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0D2538), // Dark Navy/Teal matching user image
        modifier = modifier.height(60.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
