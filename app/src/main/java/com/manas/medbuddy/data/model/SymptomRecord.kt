package com.manas.medbuddy.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SymptomRecord(
    val id: String = UUID.randomUUID().toString(),
    val symptomDescription: String,
    val onset: String = "Today",
    val duration: String = "Constant",
    val severity: Int = 3, // 1 (Mild) to 10 (Severe)
    val associatedSymptoms: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun getSeverityLabel(): String {
        return when {
            severity <= 3 -> "Mild"
            severity <= 7 -> "Moderate"
            else -> "Severe"
        }
    }
}
