package com.manas.medbuddy.data.model

import java.util.Locale

data class SleepReminderConfig(
    val isEnabled: Boolean = true,
    val bedtimeHour: Int = 22,
    val bedtimeMinute: Int = 30,
    val wakeupHour: Int = 7,
    val wakeupMinute: Int = 0,
    val leadTimeMinutes: Int = 30,
    val activeDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7) // 1=Mon, 2=Tue, ..., 7=Sun
) {
    fun getFormattedBedtime(): String {
        val hrs = if (bedtimeHour % 12 == 0) 12 else bedtimeHour % 12
        val amPm = if (bedtimeHour >= 12) "PM" else "AM"
        return String.format(Locale.US, "%02d:%02d %s", hrs, bedtimeMinute, amPm)
    }

    fun getFormattedWakeupTime(): String {
        val hrs = if (wakeupHour % 12 == 0) 12 else wakeupHour % 12
        val amPm = if (wakeupHour >= 12) "PM" else "AM"
        return String.format(Locale.US, "%02d:%02d %s", hrs, wakeupMinute, amPm)
    }
}
