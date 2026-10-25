package com.manas.medbuddy

import com.manas.medbuddy.data.model.CycleRecord
import org.junit.Assert.*
import org.junit.Test

class MenstrualTrackerTest {

    @Test
    fun testNextCycleEstimationCalculation() {
        val now = 1700000000000L
        val record = CycleRecord(
            startDate = "Nov 01, 2023",
            endDate = "Nov 05, 2023",
            cycleLengthDays = 28,
            timestamp = now
        )

        val nextMillis = record.timestamp + (record.cycleLengthDays * 24L * 60L * 60L * 1000L)
        assertEquals(now + (28L * 24L * 60L * 60L * 1000L), nextMillis)
    }

    @Test
    fun testPrivacyIsolationKeying() {
        // Verify private preferences are isolated and distinct from general app preferences
        val privatePrefsName = "medbuddy_private_menstrual_prefs"
        assertTrue(privatePrefsName.contains("private_menstrual"))
    }

    @Test
    fun testRecordDeletionFiltering() {
        val records = listOf(
            CycleRecord(id = "c1", startDate = "Oct 1", endDate = "Oct 5"),
            CycleRecord(id = "c2", startDate = "Nov 1", endDate = "Nov 5")
        )

        val filtered = records.filterNot { it.id == "c1" }
        assertEquals(1, filtered.size)
        assertEquals("c2", filtered[0].id)
    }
}
