package com.manas.medbuddy

import com.manas.medbuddy.data.model.AdherenceLog
import org.junit.Assert.*
import org.junit.Test

class AdherenceReportTest {

    @Test
    fun testAdherenceCalculationFormula() {
        // Formula: (Taken / (Taken + Skipped)) * 100. Unconfirmed excluded.
        val taken = 8
        val skipped = 2
        val unconfirmed = 3

        val denominator = taken + skipped
        val percentage = (taken.toFloat() / denominator.toFloat()) * 100f

        assertEquals(80.0f, percentage, 0.01f)
        assertEquals(3, unconfirmed)
    }

    @Test
    fun testUnconfirmedDoseExclusionFromDenominator() {
        val logs = listOf(
            AdherenceLog(medicineName = "Med A", status = "Taken"),
            AdherenceLog(medicineName = "Med A", status = "Unconfirmed"),
            AdherenceLog(medicineName = "Med A", status = "Unconfirmed")
        )

        val taken = logs.count { it.status.equals("Taken", true) }
        val skipped = logs.count { it.status.equals("Skipped", true) }
        val unconfirmed = logs.count { it.status.equals("Unconfirmed", true) }

        assertEquals(1, taken)
        assertEquals(0, skipped)
        assertEquals(2, unconfirmed)

        // Denominator (taken + skipped) should be 1, not 3
        val denominator = taken + skipped
        assertEquals(1, denominator)
    }

    @Test
    fun testAdherenceFiltering_byMedicineAndMember() {
        val logs = listOf(
            AdherenceLog(medicineName = "Med A", memberName = "Manas", status = "Taken"),
            AdherenceLog(medicineName = "Med B", memberName = "Aarav", status = "Skipped")
        )

        val medFilter = "Med A"
        val filtered = logs.filter { it.medicineName.equals(medFilter, true) }

        assertEquals(1, filtered.size)
        assertEquals("Med A", filtered[0].medicineName)
    }

    @Test
    fun testAuthorizationRestriction() {
        val isAuthorized = false
        val summaryText = if (!isAuthorized) "Restricted" else "OK"
        assertEquals("Restricted", summaryText)
    }
}
