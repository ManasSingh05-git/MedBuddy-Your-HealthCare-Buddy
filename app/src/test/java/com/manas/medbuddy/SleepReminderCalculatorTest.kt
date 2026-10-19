package com.manas.medbuddy

import com.manas.medbuddy.data.model.SleepReminderConfig
import com.manas.medbuddy.util.SleepReminderCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SleepReminderCalculatorTest {

    @Test
    fun calculateNextReminderTimeMillis_returnsFutureTimestamp() {
        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val nowMillis = nowCal.timeInMillis

        // Bedtime 22:30 (10:30 PM), lead time 30 mins -> Target reminder today at 22:00 (10:00 PM)
        val config = SleepReminderConfig(
            isEnabled = true,
            bedtimeHour = 22,
            bedtimeMinute = 30,
            leadTimeMinutes = 30,
            activeDays = listOf(1, 2, 3, 4, 5, 6, 7)
        )

        val nextReminder = SleepReminderCalculator.calculateNextReminderTimeMillis(config, nowMillis)

        assertNotNull(nextReminder)
        assertTrue(nextReminder!! > nowMillis)
    }

    @Test
    fun calculateNextReminderTimeMillis_disabledReturnsNull() {
        val config = SleepReminderConfig(isEnabled = false)
        val nextReminder = SleepReminderCalculator.calculateNextReminderTimeMillis(config)

        assertNull(nextReminder)
    }

    @Test
    fun formatNextReminderText_nullAndValidTimestamps() {
        assertEquals("No upcoming reminder scheduled", SleepReminderCalculator.formatNextReminderText(null))

        val nowMillis = System.currentTimeMillis()
        val futureMillis = nowMillis + 2 * 60 * 60 * 1000L
        val formatted = SleepReminderCalculator.formatNextReminderText(futureMillis)

        assertTrue(formatted.contains("Next reminder:"))
    }
}
