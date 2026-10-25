package com.manas.medbuddy.data.model

import java.util.UUID

data class LabTestItem(
    val testName: String,
    val value: Double,
    val unit: String,
    val referenceRange: String
)

data class MedicalReport(
    val id: String = UUID.randomUUID().toString(),
    val reportTitle: String,
    val reportDate: String,
    val reportCategory: String = "Blood Test",
    val testItems: List<LabTestItem> = emptyList(),
    val originalFileName: String = "report.pdf",
    val timestamp: Long = System.currentTimeMillis()
)
