package com.manas.medbuddy.data.model

import java.util.UUID

data class CycleRecord(
    val id: String = UUID.randomUUID().toString(),
    val startDate: String,
    val endDate: String,
    val cycleLengthDays: Int = 28,
    val symptoms: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
