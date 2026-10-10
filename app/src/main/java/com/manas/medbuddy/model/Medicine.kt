package com.manas.medbuddy.model

enum class ScheduleType {
    DAILY,
    SELECTED_DAYS
}

enum class ReminderType {
    MORNING,
    AFTERNOON,
    EVENING,
    CUSTOM
}

data class ReminderTime(
    val id: String = "",
    val label: String = "Morning",
    val time: String = "08:00 AM",
    val type: String = "MORNING"
)

data class Medicine(
    val id: String = "",
    val name: String = "",
    val dosage: String = "",
    val time: String = "",
    val instructions: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val scheduleType: String = "DAILY",
    val selectedDays: List<String> = emptyList(),
    val reminderTimes: List<ReminderTime> = emptyList()
)

data class DoseLog(
    val id: String = "",
    val medicineId: String = "",
    val medicineName: String = "",
    val dosage: String = "",
    val scheduledTime: String = "",
    val date: String = "",
    val status: String = "PENDING", // "TAKEN", "SKIPPED", "PENDING"
    val timestamp: Long = System.currentTimeMillis()
)
