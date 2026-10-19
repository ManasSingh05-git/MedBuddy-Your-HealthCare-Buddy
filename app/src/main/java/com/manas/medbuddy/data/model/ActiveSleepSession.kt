package com.manas.medbuddy.data.model

import java.util.Locale

data class ActiveSleepSession(
    val startTimeMillis: Long,
    val isTracking: Boolean = true
) {
    fun getElapsedMinutes(nowMillis: Long = System.currentTimeMillis()): Long {
        return maxOf(0L, (nowMillis - startTimeMillis) / (1000 * 60))
    }

    fun getFormattedElapsed(nowMillis: Long = System.currentTimeMillis()): String {
        val mins = getElapsedMinutes(nowMillis)
        val hrs = mins / 60
        val remainingMins = mins % 60
        return String.format(Locale.US, "%02dh %02dm", hrs, remainingMins)
    }
}
