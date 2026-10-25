package com.manas.medbuddy.reports

import java.util.UUID

enum class LabValueStatus {
    NORMAL,
    HIGH,
    LOW,
    UNKNOWN
}

data class LabTestItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val value: String = "",
    val numericValue: Double? = null,
    val unit: String = "",
    val referenceRange: String = "",
    val minRange: Double? = null,
    val maxRange: Double? = null,
    val status: LabValueStatus = LabValueStatus.UNKNOWN,
    val explanationEnglish: String = "",
    val explanationHinglish: String = "",
    val isOutofRange: Boolean = false
)

data class MedicalReportResult(
    val originalFileUri: String = "",
    val localFilePath: String = "",
    val reportTitle: String = "Lab Report Analysis",
    val rawOcrText: String = "",
    val labItems: List<LabTestItem> = emptyList(),
    val summaryEnglish: String = "",
    val summaryHinglish: String = "",
    val suggestedQuestionsForDoctor: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
