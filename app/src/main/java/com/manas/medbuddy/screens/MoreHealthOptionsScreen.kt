package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.DarkBackground
import com.manas.medbuddy.ui.theme.DarkSurface
import com.manas.medbuddy.ui.theme.EmeraldPrimary
import com.manas.medbuddy.ui.theme.SOSRed
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

data class HealthToolItem(
    val title: String,
    val description: String,
    val route: String,
    val icon: ImageVector,
    val iconTint: Color,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreHealthOptionsScreen(
    onBack: () -> Unit = {},
    onNavigateToRoute: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val allTools = remember {
        listOf(
            // AI & Clinical Tools
            HealthToolItem("AI Prescription Scanner", "Scan medicine strips & prescriptions via camera", "prescription_scanner", Icons.Default.MedicalServices, EmeraldPrimary, "AI & Clinical"),
            HealthToolItem("AI Medical Report Explainer", "Plain-English explanations for lab test reports", "report_explainer", Icons.Default.NoteAdd, CyanPrimary, "AI & Clinical"),
            HealthToolItem("AI Symptom Journal", "Log daily symptoms & severity for doctor visits", "symptom_journal", Icons.Default.NoteAdd, EmeraldPrimary, "AI & Clinical"),
            HealthToolItem("Doctor Visit Preparation", "Checklist & questions generator before appointments", "doctor_prep", Icons.Default.MedicalServices, CyanPrimary, "AI & Clinical"),
            HealthToolItem("Medical Terminology Assistant", "Searchable A-Z dictionary of medical terms", "medical_glossary", Icons.Default.MenuBook, CyanPrimary, "AI & Clinical"),
            HealthToolItem("Medical Report Comparison", "Side-by-side blood test & report analyzer", "report_comparison", Icons.Default.Compare, EmeraldPrimary, "AI & Clinical"),

            // Records & Vault
            HealthToolItem("Secure Medical Records Vault", "Upload & store PDF lab reports securely", "medical_vault", Icons.Default.CloudUpload, CyanPrimary, "Vault & Records"),
            HealthToolItem("Personal Health Information Card", "Digital Emergency ID card with blood group & allergies", "health_card", Icons.Default.CreditCard, EmeraldPrimary, "Vault & Records"),
            HealthToolItem("Personal History Timeline", "Chronological medical history & prescription timeline", "health_timeline", Icons.Default.Timeline, CyanPrimary, "Vault & Records"),
            HealthToolItem("Vaccination Record Manager", "Track vaccine history, doses, due dates & certificates", "vaccination_manager", Icons.Default.Vaccines, EmeraldPrimary, "Vault & Records"),
            HealthToolItem("Allergy & Reaction Registry", "Log drug/food allergies with severity ratings", "allergy_registry", Icons.Default.Shield, SOSRed, "Vault & Records"),
            HealthToolItem("Secure Medical Document Sharing", "Share records securely with doctors or caregivers", "document_sharing", Icons.Default.CloudUpload, CyanPrimary, "Vault & Records"),

            // Family & Care
            HealthToolItem("Family Member Profiles", "Manage family members' health records", "family_profiles", Icons.Default.FamilyRestroom, EmeraldPrimary, "Family & Care"),
            HealthToolItem("Caregiver Dashboard", "Remote health monitoring for family & elders", "caregiver_dashboard", Icons.Default.SupervisorAccount, SOSRed, "Family & Care"),
            HealthToolItem("Family Health Calendar", "Shared family medical appointment calendar", "family_calendar", Icons.Default.CalendarMonth, CyanPrimary, "Family & Care"),
            HealthToolItem("Doctor Appointment Organizer", "Manage upcoming doctor visits & reminders", "appointment_organizer", Icons.Default.CalendarMonth, EmeraldPrimary, "Family & Care"),

            // Wellness & Coaching
            HealthToolItem("AI Wellness Habit Coach", "Daily wellness advice & habit coaching", "wellness_coach", Icons.Default.SelfImprovement, EmeraldPrimary, "Wellness & Coaching"),
            HealthToolItem("Posture & Movement Coach", "Guided posture breaks & physical exercise", "movement_coach", Icons.Default.SelfImprovement, CyanPrimary, "Wellness & Coaching"),
            HealthToolItem("Private Menstrual Tracker", "Cycle tracking & wellness insights", "menstrual_tracker", Icons.Default.SelfImprovement, SOSRed, "Wellness & Coaching"),
            HealthToolItem("Medication Adherence Reports", "Medication compliance statistics & graphs", "adherence_reports", Icons.Default.TrendingUp, EmeraldPrimary, "Wellness & Coaching")
        )
    }

    val filteredTools = allTools.filter { tool ->
        val matchesSearch = tool.title.contains(searchQuery, ignoreCase = true) ||
                tool.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || tool.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("More Health Options", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search health tools...", color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = DarkSurface,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("All", "AI & Clinical", "Vault & Records", "Family & Care", "Wellness & Coaching").forEach { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        onClick = { selectedCategory = category },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) EmeraldPrimary else DarkSurface,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Tools Count Header
            Text(
                text = "Available Tools (${filteredTools.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Tools List Items (Each opens on a NEW standalone screen!)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredTools.forEach { tool ->
                    StandaloneToolCard(
                        tool = tool,
                        onClick = { onNavigateToRoute(tool.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StandaloneToolCard(
    tool: HealthToolItem,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(tool.icon, contentDescription = null, tint = tool.iconTint, modifier = Modifier.size(24.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(tool.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text(tool.description, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = CyanPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
