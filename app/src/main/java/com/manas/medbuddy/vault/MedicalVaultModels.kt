package com.manas.medbuddy.vault

import java.util.UUID

enum class VaultCategory {
    ALL,
    PRESCRIPTION,
    LAB_REPORT,
    DISCHARGE_SUMMARY,
    VACCINATION_RECORD,
    GENERAL_REPORT
}

data class MedicalRecord(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val category: String = VaultCategory.GENERAL_REPORT.name,
    val doctorName: String = "",
    val documentDate: String = "",
    val localFilePath: String = "",
    val fileSizeBytes: Long = 0L,
    val fileSizeFormatted: String = "0 KB",
    val mimeType: String = "",
    val isPdf: Boolean = false,
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
