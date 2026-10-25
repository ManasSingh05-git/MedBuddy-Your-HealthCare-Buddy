package com.manas.medbuddy.data.model

import java.util.UUID

data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // "Appointment", "Vaccination", "Medicine"
    val memberName: String,
    val dateTimeString: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPrivate: Boolean = false
)
