package com.manas.medbuddy.checklist

import java.util.UUID

enum class ChecklistItemCategory {
    REPORTS,
    PRESCRIPTIONS,
    DOCUMENTS,
    QUESTIONS_FOR_DOCTOR,
    PERSONAL_ITEMS,
    OTHER
}

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val category: String = ChecklistItemCategory.OTHER.name,
    val isCompleted: Boolean = false,
    val notes: String = ""
)

data class HospitalVisitChecklist(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "Hospital Visit Checklist",
    val linkedAppointmentId: String = "",
    val doctorOrHospitalName: String = "",
    val visitDate: String = "",
    val items: List<ChecklistItem> = emptyList(),
    val completionPercentage: Float = 0f,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
