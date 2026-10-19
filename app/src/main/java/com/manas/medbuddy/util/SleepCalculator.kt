package com.manas.medbuddy.util

import com.manas.medbuddy.data.model.SleepRecord
import java.util.Calendar

object SleepCalculator {

    fun calculateDurationMinutes(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Long {
        val startMins = startHour * 60 + startMinute
        val endMins = endHour * 60 + endMinute

        val diff = if (endMins >= startMins) {
            endMins - startMins
        } else {
            // Midnight crossing (e.g. 23:00 to 07:00)
            (24 * 60 - startMins) + endMins
        }
        return diff.toLong()
    }

    fun calculateMillisFromTime(baseDateMillis: Long, hour: Int, minute: Int, isEndNextDay: Boolean = false): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = baseDateMillis
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        if (isEndNextDay) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.timeInMillis
    }

    fun formatMinutesToHoursMinutes(totalMinutes: Long): String {
        val hrs = totalMinutes / 60
        val mins = totalMinutes % 60
        return if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
    }

    fun calculateAverageSleepMinutes(records: List<SleepRecord>, daysLimit: Int = 7): Long {
        if (records.isEmpty()) return 0L
        val recentRecords = records.take(daysLimit)
        val sumMinutes = recentRecords.sumOf { it.durationMinutes }
        return sumMinutes / recentRecords.size
    }

    fun calculateGoalProgressPercentage(actualMinutes: Long, goalHours: Float): Float {
        if (goalHours <= 0f) return 0f
        val goalMinutes = goalHours * 60f
        return (actualMinutes / goalMinutes).coerceIn(0f, 1.5f)
    }

    fun validateSleepRecord(startTimeMillis: Long, endTimeMillis: Long): String? {
        if (endTimeMillis <= startTimeMillis) {
            return "Wake-up time must be after sleep start time."
        }
        val durationMins = (endTimeMillis - startTimeMillis) / (1000 * 60)
        if (durationMins < 15) {
            return "Sleep duration must be at least 15 minutes."
        }
        if (durationMins > 24 * 60) {
            return "Sleep duration cannot exceed 24 hours."
        }
        return null
    }

    fun hasOverlappingRecord(
        records: List<SleepRecord>,
        newStart: Long,
        newEnd: Long,
        excludeId: String? = null
    ): Boolean {
        return records.any { record ->
            if (excludeId != null && record.id == excludeId) return@any false
            val maxStart = maxOf(record.startTimeMillis, newStart)
            val minEnd = minOf(record.endTimeMillis, newEnd)
            maxStart < minEnd
        }
    }
}
