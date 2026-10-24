package com.manas.medbuddy.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SleepRecord(
    val id: String = UUID.randomUUID().toString(),
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val qualityRating: Int = 4, // 1 (Poor) to 5 (Excellent)
    val notes: String = "",
    val isAutoTracked: Boolean = false
) {
    val durationMinutes: Long
        get() = maxOf(0L, (endTimeMillis - startTimeMillis) / (1000 * 60))

    val durationHoursFloat: Float
        get() = durationMinutes / 60f

    fun getFormattedDuration(): String {
        val hrs = durationMinutes / 60
        val mins = durationMinutes % 60
        return if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
    }

    fun getFormattedStartTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        return sdf.format(Date(startTimeMillis))
    }

    fun getFormattedEndTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        return sdf.format(Date(endTimeMillis))
    }

    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        return sdf.format(Date(endTimeMillis))
    }
}
