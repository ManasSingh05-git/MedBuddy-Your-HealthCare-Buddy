package com.manas.medbuddy.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Warning
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
import com.manas.medbuddy.data.model.MedicalReport
import com.manas.medbuddy.data.repository.ComparisonResult
import com.manas.medbuddy.data.repository.ReportComparisonRepository
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.SectionHeader
import com.manas.medbuddy.ui.theme.*
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportComparisonScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { ReportComparisonRepository.getInstance(context) }
    val reports by repository.reportsFlow.collectAsState()

    var reportA by remember { mutableStateOf<MedicalReport?>(null) }
    var reportB by remember { mutableStateOf<MedicalReport?>(null) }
    var comparisonResult by remember { mutableStateOf<ComparisonResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(reports) {
        if (reports.size >= 2 && reportA == null && reportB == null) {
            reportA = reports[0]
            reportB = reports[1]
            comparisonResult = repository.compareReports(reports[0], reports[1])
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("📊 Medical Report Comparison", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step 1: Select Reports A & B
            SectionHeader(title = "1. Select Reports to Compare")

            if (reports.size < 2) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("At least 2 reports are required for comparison.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Report A Selector
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Report A (Baseline)", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        reports.forEach { r ->
                            val isSelected = reportA?.id == r.id
                            Surface(
                                onClick = {
                                    reportA = r
                                    reportB?.let { b ->
                                        if (r.id != b.id) {
                                            comparisonResult = repository.compareReports(r, b)
                                            errorMessage = null
                                        } else {
                                            errorMessage = "Please select two different reports."
                                        }
                                    }
                                },
                                color = if (isSelected) CyanPrimary.copy(alpha = 0.25f) else DarkSurface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(r.reportTitle, color = TextPrimary, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }

                    // Report B Selector
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Report B (Comparison)", color = EmeraldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        reports.forEach { r ->
                            val isSelected = reportB?.id == r.id
                            Surface(
                                onClick = {
                                    reportB = r
                                    reportA?.let { a ->
                                        if (r.id != a.id) {
                                            comparisonResult = repository.compareReports(a, r)
                                            errorMessage = null
                                        } else {
                                            errorMessage = "Please select two different reports."
                                        }
                                    }
                                },
                                color = if (isSelected) EmeraldPrimary.copy(alpha = 0.25f) else DarkSurface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(r.reportTitle, color = TextPrimary, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            }

            errorMessage?.let { err ->
                Text(err, color = SOSRed, fontSize = 12.sp)
            }

            // Step 2: Side-by-Side Comparison Table & Summary
            comparisonResult?.let { result ->
                SectionHeader(title = "2. Side-by-Side Test Comparison")

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Test Name", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            Text("Report A (${result.reportA.reportDate})", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Report B (${result.reportB.reportDate})", color = EmeraldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        }

                        HorizontalDivider(color = DarkBackground)

                        result.matchedItems.forEach { matched ->
                            val trendColor = when (matched.trend) {
                                "Increased" -> Color(0xFFFFD700)
                                "Decreased" -> CyanPrimary
                                else -> TextPrimary
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(matched.testName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Ref: ${matched.referenceRange}", color = TextSecondary, fontSize = 10.sp)
                                }
                                Text("${matched.valueA} ${matched.unitA}", color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${matched.valueB} ${matched.unitB}", color = trendColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(matched.trend, color = trendColor, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // Unmatched or Incompatible Flags
                if (result.unmatchedItems.isNotEmpty()) {
                    SectionHeader(title = "3. Flagged Missing / Incompatible Tests")
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            result.unmatchedItems.forEach { un ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = SOSRed, modifier = Modifier.size(16.dp))
                                    Text("${un.testName}: ${un.reason}", color = TextPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Plain Language Summary
                SectionHeader(title = "4. Plain-Language Summary")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(result.plainLanguageSummary, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}
