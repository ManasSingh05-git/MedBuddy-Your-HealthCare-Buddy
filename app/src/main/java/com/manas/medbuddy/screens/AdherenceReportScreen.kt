package com.manas.medbuddy.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Share
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
import com.manas.medbuddy.data.model.AdherenceLog
import com.manas.medbuddy.data.repository.AdherenceReportRepository
import com.manas.medbuddy.data.repository.AdherenceSummary
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdherenceReportScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { AdherenceReportRepository.getInstance(context) }
    val logs by repository.logsFlow.collectAsState()

    var selectedMedicineFilter by remember { mutableStateOf("All") }
    var selectedMemberFilter by remember { mutableStateOf("All") }
    var selectedDateRange by remember { mutableStateOf("Last 7 Days") } // "Last 7 Days", "Last 30 Days"
    var isAuthorized by remember { mutableStateOf(true) }

    val medicines = remember(logs) {
        listOf("All") + logs.map { it.medicineName }.distinct()
    }

    val members = remember(logs) {
        listOf("All") + logs.map { it.memberName }.distinct()
    }

    val summary = remember(logs, selectedMedicineFilter, selectedMemberFilter, isAuthorized) {
        repository.calculateSummary(selectedMedicineFilter, selectedMemberFilter, isAuthorized)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("📊 Medication Adherence Reports", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
        }
    ) { paddingValues ->
        if (!isAuthorized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Unauthorized access: Adherence report data is restricted.", color = SOSRed, fontWeight = FontWeight.Bold)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Range & Authorization Simulation Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Last 7 Days", "Last 30 Days").forEach { range ->
                        val isSelected = selectedDateRange == range
                        Surface(
                            onClick = { selectedDateRange = range },
                            color = if (isSelected) CyanPrimary else DarkSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                range,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Checkbox(checked = isAuthorized, onCheckedChange = { isAuthorized = it }, colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary))
                    Text("Authorized", color = TextSecondary, fontSize = 11.sp)
                }
            }

            // Filters (Medicine & Member)
            SectionHeader(title = "Filters")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Medicine Filter
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Medicine", color = TextSecondary, fontSize = 11.sp)
                    medicines.take(3).forEach { med ->
                        val isSelected = selectedMedicineFilter == med
                        Surface(
                            onClick = { selectedMedicineFilter = med },
                            color = if (isSelected) CyanPrimary.copy(alpha = 0.25f) else DarkSurface,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(med, color = TextPrimary, fontSize = 11.sp, modifier = Modifier.padding(6.dp), maxLines = 1)
                        }
                    }
                }

                // Member Filter
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Family Member", color = TextSecondary, fontSize = 11.sp)
                    members.take(3).forEach { mem ->
                        val isSelected = selectedMemberFilter == mem
                        Surface(
                            onClick = { selectedMemberFilter = mem },
                            color = if (isSelected) EmeraldPrimary.copy(alpha = 0.25f) else DarkSurface,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(mem, color = TextPrimary, fontSize = 11.sp, modifier = Modifier.padding(6.dp), maxLines = 1)
                        }
                    }
                }
            }

            // Summary Statistics Cards
            SectionHeader(title = "Adherence Summary")

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
                            Text("Adherence Rate", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                "${String.format(java.util.Locale.US, "%.1f", summary.adherencePercentage)}%",
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyanPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = CyanPrimary)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AdherenceMetricPill("Scheduled", "${summary.totalScheduled}", TextPrimary)
                        AdherenceMetricPill("Taken", "${summary.takenCount}", EmeraldPrimary)
                        AdherenceMetricPill("Skipped", "${summary.skippedCount}", SOSRed)
                        AdherenceMetricPill("Unconfirmed", "${summary.unconfirmedCount}", Color(0xFFFFD700))
                    }
                }
            }

            // Formula Documentation Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Calculation Formula & Rules", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        summary.formulaDocumentation,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Export / Share Button
            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Medication Adherence Report:\nAdherence Rate: ${summary.adherencePercentage}%\nTaken: ${summary.takenCount}\nSkipped: ${summary.skippedCount}\nUnconfirmed: ${summary.unconfirmedCount}\nFormula: ${summary.formulaDocumentation}")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export / Share Authorized Adherence Report", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AdherenceMetricPill(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}
