package com.manas.medbuddy.util

import com.manas.medbuddy.data.model.SleepReminderConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SleepReminderCalculator {

    fun calculateNextReminderTimeMillis(
        config: SleepReminderConfig,
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        if (!config.isEnabled || config.activeDays.isEmpty()) return null

        val cal = Calendar.getInstance()
        cal.timeInMillis = nowMillis

        // Search through today and up to 7 days ahead
        for (dayOffset in 0..7) {
            val candidateCal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_MONTH, dayOffset)
                set(Calendar.HOUR_OF_DAY, config.bedtimeHour)
                set(Calendar.MINUTE, config.bedtimeMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // Subtract lead time
                add(Calendar.MINUTE, -config.leadTimeMinutes)
            }

            // Convert Calendar day-of-week (Sunday=1, Monday=2..) to 1=Mon .. 7=Sun
            val calDow = candidateCal.get(Calendar.DAY_OF_WEEK)
            val isoDow = if (calDow == Calendar.SUNDAY) 7 else calDow - 1

            if (isoDow in config.activeDays) {
                val candidateTime = candidateCal.timeInMillis
                if (candidateTime > nowMillis) {
                    return candidateTime
                }
            }
        }
        return null
    }

    fun formatNextReminderText(nextReminderMillis: Long?): String {
        if (nextReminderMillis == null) return "No upcoming reminder scheduled"

        val nowCal = Calendar.getInstance()
        val remCal = Calendar.getInstance().apply { timeInMillis = nextReminderMillis }

        val isToday = nowCal.get(Calendar.YEAR) == remCal.get(Calendar.YEAR) &&
                nowCal.get(Calendar.DAY_OF_YEAR) == remCal.get(Calendar.DAY_OF_YEAR)

        val isTomorrow = nowCal.get(Calendar.YEAR) == remCal.get(Calendar.YEAR) &&
                nowCal.get(Calendar.DAY_OF_YEAR) + 1 == remCal.get(Calendar.DAY_OF_YEAR)

        val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date(nextReminderMillis))

        return when {
            isToday -> "Next reminder: Today at $timeStr"
            isTomorrow -> "Next reminder: Tomorrow at $timeStr"
            else -> {
                val dateStr = SimpleDateFormat("EEE, MMM dd", Locale.US).format(Date(nextReminderMillis))
                "Next reminder: $dateStr at $timeStr"
            }
        }
    }
}
