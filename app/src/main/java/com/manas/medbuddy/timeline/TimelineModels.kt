package com.manas.medbuddy.timeline

import java.util.UUID

enum class TimelineEventType {
    ALL,
    REPORT,
    VACCINATION,
    MEDICINE,
    USER_EVENT
}

enum class TimelineSourceType {
    CLINICIAN_FACT,  // Clinician-documented or hospital/lab verified fact
    USER_REPORTED    // Patient self-reported or user-entered info
}

enum class TimelineDateFilter {
    ALL_TIME,
    LAST_30_DAYS,
    LAST_6_MONTHS,
    LAST_YEAR
}

data class TimelineEvent(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val eventType: String = TimelineEventType.USER_EVENT.name,
    val sourceType: String = TimelineSourceType.USER_REPORTED.name,
    val eventDate: String = "",
    val providerOrLocation: String = "",
    val description: String = "",
    val targetRoute: String = "",
    val sourceRecordId: String = "",
    val isCustomUserEvent: Boolean = false,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
