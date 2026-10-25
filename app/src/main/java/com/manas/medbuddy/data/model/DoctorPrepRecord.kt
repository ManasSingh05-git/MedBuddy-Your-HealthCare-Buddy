package com.manas.medbuddy.data.model

import java.util.UUID

data class DoctorPrepRecord(
    val id: String = UUID.randomUUID().toString(),
    val appointmentId: String,
    val doctorName: String,
    val appointmentDate: String,
    val recordedFactsConcerns: List<String> = emptyList(),
    val recordedFactsMedicines: List<String> = emptyList(),
    val recordedFactsReports: List<String> = emptyList(),
    val editableConsultationSummary: String = "",
    val aiSuggestedQuestions: List<String> = emptyList(),
    val isAttached: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
