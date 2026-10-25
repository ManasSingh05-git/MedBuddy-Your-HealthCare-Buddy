package com.manas.medbuddy.scanner

import java.util.UUID

data class ExtractedMedicineCandidate(
    val id: String = UUID.randomUUID().toString(),
    val rawText: String = "",
    val name: String = "",
    val strength: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val instructions: String = "",
    val confidenceScore: Float = 1.0f,
    val isLegible: Boolean = true,
    val needsPharmacistReview: Boolean = false,
    val flagReason: String = ""
)

data class PrescriptionScanResult(
    val originalFileUri: String = "",
    val localFilePath: String = "",
    val isPdf: Boolean = false,
    val rawOcrText: String = "",
    val candidates: List<ExtractedMedicineCandidate> = emptyList(),
    val scanTimestamp: Long = System.currentTimeMillis()
)
