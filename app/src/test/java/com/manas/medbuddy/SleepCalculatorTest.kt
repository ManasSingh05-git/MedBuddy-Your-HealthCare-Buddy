package com.manas.medbuddy

import com.manas.medbuddy.data.model.SleepRecord
import com.manas.medbuddy.util.SleepCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepCalculatorTest {

    @Test
    fun calculateDurationMinutes_sameDayAndMidnightCrossing() {
        // 14:00 to 18:00 = 4 hours (240 mins)
        val sameDay = SleepCalculator.calculateDurationMinutes(14, 0, 18, 0)
        assertEquals(240L, sameDay)

        // 23:30 to 07:00 = 7.5 hours (450 mins)
        val midnightCrossing = SleepCalculator.calculateDurationMinutes(23, 30, 7, 0)
        assertEquals(450L, midnightCrossing)
    }

    @Test
    fun validateSleepRecord_checksDurations() {
        val now = System.currentTimeMillis()

        // End before start -> Error
        assertNotNull(SleepCalculator.validateSleepRecord(now, now - 1000))

        // Duration < 15 mins -> Error
        assertNotNull(SleepCalculator.validateSleepRecord(now, now + 10 * 60 * 1000))

        // Duration > 24 hours -> Error
        assertNotNull(SleepCalculator.validateSleepRecord(now, now + 25 * 60 * 60 * 1000))

        // Valid 8-hour sleep -> Null (no error)
        assertNull(SleepCalculator.validateSleepRecord(now, now + 8 * 60 * 60 * 1000))
    }

    @Test
    fun hasOverlappingRecord_detectsOverlaps() {
        val record1 = SleepRecord(
            id = "rec1",
            startTimeMillis = 1000L,
            endTimeMillis = 5000L
        )
        val list = listOf(record1)

        // Overlapping (2000 to 4000) -> True
        assertTrue(SleepCalculator.hasOverlappingRecord(list, 2000L, 4000L))

        // Non-overlapping (6000 to 9000) -> False
        assertFalse(SleepCalculator.hasOverlappingRecord(list, 6000L, 9000L))

        // Exclude record1 -> False
        assertFalse(SleepCalculator.hasOverlappingRecord(list, 2000L, 4000L, excludeId = "rec1"))
    }

    @Test
    fun calculateGoalProgressPercentage_computesAccuratePercentage() {
        // 6 hours actual (360 mins) vs 8.0 hours goal -> 0.75 (75%)
        val progress = SleepCalculator.calculateGoalProgressPercentage(360L, 8.0f)
        assertEquals(0.75f, progress, 0.01f)
    }
}
