package com.manas.medbuddy.data.model

import java.util.Locale
import java.util.concurrent.TimeUnit

data class ActiveSleepSession(
    val startTimeMillis: Long = System.currentTimeMillis()
) {
    fun getFormattedElapsed(nowMillis: Long): String {
        val elapsedMillis = maxOf(0L, nowMillis - startTimeMillis)
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
        val hrs = totalMinutes / 60
        val mins = totalMinutes % 60
        val secs = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60
        return if (hrs > 0) {
            String.format(Locale.US, "%dh %02dm %02ds", hrs, mins, secs)
        } else {
            String.format(Locale.US, "%dm %02ds", mins, secs)
        }
    }
}
