package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.LabTestItem
import com.manas.medbuddy.data.model.MedicalReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MatchedTestComparison(
    val testName: String,
    val valueA: Double,
    val unitA: String,
    val valueB: Double,
    val unitB: String,
    val referenceRange: String,
    val isUnitConverted: Boolean,
    val trend: String // "Increased", "Decreased", "Stable"
)

data class UnmatchedTestItem(
    val testName: String,
    val sourceReport: String,
    val reason: String // "Missing in other report" or "Incompatible unit"
)

data class ComparisonResult(
    val reportA: MedicalReport,
    val reportB: MedicalReport,
    val matchedItems: List<MatchedTestComparison>,
    val unmatchedItems: List<UnmatchedTestItem>,
    val plainLanguageSummary: String
)

class ReportComparisonRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _reportsState = MutableStateFlow<List<MedicalReport>>(emptyList())
    val reportsFlow: StateFlow<List<MedicalReport>> = _reportsState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val json = prefs.getString(KEY_REPORTS, null)
        val list: List<MedicalReport> = if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<MedicalReport>>() {}.type
                gson.fromJson(json, type) ?: defaultReports()
            } catch (_: Exception) {
                defaultReports()
            }
        } else {
            defaultReports()
        }
        _reportsState.value = list
    }

    private fun defaultReports(): List<MedicalReport> {
        val defaults = listOf(
            MedicalReport(
                id = "report_1",
                reportTitle = "Routine Blood Panel (Q2)",
                reportDate = "Jun 10, 2025",
                reportCategory = "Hematology",
                testItems = listOf(
                    LabTestItem("Hemoglobin", 13.2, "g/dL", "13.5 - 17.5"),
                    LabTestItem("Total Cholesterol", 210.0, "mg/dL", "< 200"),
                    LabTestItem("Blood Glucose (Fasting)", 95.0, "mg/dL", "70 - 99"),
                    LabTestItem("Platelets", 250.0, "10^3/uL", "150 - 450")
                )
            ),
            MedicalReport(
                id = "report_2",
                reportTitle = "Routine Blood Panel (Q3)",
                reportDate = "Oct 15, 2025",
                reportCategory = "Hematology",
                testItems = listOf(
                    LabTestItem("Hemoglobin", 14.1, "g/dL", "13.5 - 17.5"),
                    LabTestItem("Total Cholesterol", 195.0, "mg/dL", "< 200"),
                    LabTestItem("Blood Glucose (Fasting)", 92.0, "mg/dL", "70 - 99"),
                    LabTestItem("Triglycerides", 150.0, "mg/dL", "< 150") // Unmatched in report 1
                )
            )
        )
        saveReportsToPrefs(defaults)
        return defaults
    }

    private fun saveReportsToPrefs(list: List<MedicalReport>) {
        _reportsState.value = list
        prefs.edit().putString(KEY_REPORTS, gson.toJson(list)).apply()
    }

    fun addReport(report: MedicalReport) {
        val current = _reportsState.value.toMutableList()
        current.add(0, report)
        saveReportsToPrefs(current)
    }

    fun compareReports(reportA: MedicalReport, reportB: MedicalReport): ComparisonResult {
        val matched = mutableListOf<MatchedTestComparison>()
        val unmatched = mutableListOf<UnmatchedTestItem>()

        val mapA = reportA.testItems.associateBy { it.testName.lowercase().trim() }
        val mapB = reportB.testItems.associateBy { it.testName.lowercase().trim() }

        // Match tests carefully
        for ((nameA, itemA) in mapA) {
            val itemB = mapB[nameA]
            if (itemB != null) {
                // Check unit compatibility or conversion validation
                var valB = itemB.value
                var unitB = itemB.unit
                var isConverted = false

                if (itemA.unit.equals(itemB.unit, ignoreCase = true)) {
                    // Direct match
                } else {
                    // Example validated unit conversion (e.g. mg/dL to g/L)
                    if (itemA.unit.equals("mg/dL", true) && itemB.unit.equals("g/L", true)) {
                        valB *= 10.0
                        unitB = "mg/dL (converted)"
                        isConverted = true
                    } else if (itemA.unit.equals("g/L", true) && itemB.unit.equals("mg/dL", true)) {
                        valB /= 10.0
                        unitB = "g/L (converted)"
                        isConverted = true
                    } else {
                        // Incompatible units flag
                        unmatched.add(UnmatchedTestItem(itemA.testName, reportB.reportTitle, "Incompatible units (${itemA.unit} vs ${itemB.unit})"))
                        continue
                    }
                }

                val trend = when {
                    itemB.value > itemA.value -> "Increased"
                    itemB.value < itemA.value -> "Decreased"
                    else -> "Stable"
                }

                matched.add(
                    MatchedTestComparison(
                        testName = itemA.testName,
                        valueA = itemA.value,
                        unitA = itemA.unit,
                        valueB = valB,
                        unitB = unitB,
                        referenceRange = itemA.referenceRange,
                        isUnitConverted = isConverted,
                        trend = trend
                    )
                )
            } else {
                unmatched.add(UnmatchedTestItem(itemA.testName, reportB.reportTitle, "Missing in Report B"))
            }
        }

        // Find items in B not in A
        for ((nameB, itemB) in mapB) {
            if (!mapA.containsKey(nameB)) {
                unmatched.add(UnmatchedTestItem(itemB.testName, reportA.reportTitle, "Missing in Report A"))
            }
        }

        val summary = buildPlainLanguageSummary(reportA, reportB, matched, unmatched)

        return ComparisonResult(
            reportA = reportA,
            reportB = reportB,
            matchedItems = matched,
            unmatchedItems = unmatched,
            plainLanguageSummary = summary
        )
    }

    private fun buildPlainLanguageSummary(
        reportA: MedicalReport,
        reportB: MedicalReport,
        matched: List<MatchedTestComparison>,
        unmatched: List<UnmatchedTestItem>
    ): String {
        val increased = matched.filter { it.trend == "Increased" }.map { it.testName }
        val decreased = matched.filter { it.trend == "Decreased" }.map { it.testName }
        val stable = matched.filter { it.trend == "Stable" }.map { it.testName }

        val sb = StringBuilder()
        sb.append("Comparison between '${reportA.reportTitle}' (${reportA.reportDate}) and '${reportB.reportTitle}' (${reportB.reportDate}):\n")
        if (increased.isNotEmpty()) sb.append("• Tests with increased values: ${increased.joinToString(", ")}.\n")
        if (decreased.isNotEmpty()) sb.append("• Tests with decreased values: ${decreased.joinToString(", ")}.\n")
        if (stable.isNotEmpty()) sb.append("• Tests remaining stable: ${stable.joinToString(", ")}.\n")
        if (unmatched.isNotEmpty()) sb.append("• Note: ${unmatched.size} test(s) had missing or incompatible counterparts.\n")
        sb.append("Disclaimer: This comparison is provided for informational purposes only and does not constitute a medical diagnosis.")
        return sb.toString()
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_report_comparison_prefs"
        private const val KEY_REPORTS = "key_stored_reports"

        @Volatile
        private var INSTANCE: ReportComparisonRepository? = null

        fun getInstance(context: Context): ReportComparisonRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ReportComparisonRepository(context).also { INSTANCE = it }
            }
        }
    }
}
