package com.manas.medbuddy

import com.manas.medbuddy.vaccination.VaccinationRecord
import com.manas.medbuddy.vaccination.VaccinationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VaccinationManagerTest {

    @Test
    fun testVaccinationRecordModelAndDefaults() {
        val record = VaccinationRecord(
            id = "vac_101",
            userId = "user_789",
            vaccineName = "COVID-19 Covishield",
            doseNumber = "Dose 2",
            date = "2026-05-15",
            providerName = "Apollo Hospital",
            status = VaccinationStatus.COMPLETED.name,
            hasCertificate = true,
            reminderEnabled = true,
            reminderDate = "2026-11-15",
            reminderTime = "09:00 AM"
        )

        assertEquals("vac_101", record.id)
        assertEquals("COVID-19 Covishield", record.vaccineName)
        assertEquals("Dose 2", record.doseNumber)
        assertEquals("COMPLETED", record.status)
        assertTrue(record.hasCertificate)
        assertTrue(record.reminderEnabled)
    }

    @Test
    fun testVaccinationStatusEnum() {
        val statuses = VaccinationStatus.entries.map { it.name }
        assertTrue(statuses.contains("COMPLETED"))
        assertTrue(statuses.contains("PLANNED"))
        assertTrue(statuses.contains("UNVERIFIED"))
    }

    @Test
    fun testChronologicalSorting() {
        val records = listOf(
            VaccinationRecord(id = "1", vaccineName = "Hep B Dose 1", date = "2025-01-10"),
            VaccinationRecord(id = "2", vaccineName = "COVID Booster", date = "2026-10-20"),
            VaccinationRecord(id = "3", vaccineName = "Flu Shot 2025", date = "2025-11-05")
        )

        val sorted = records.sortedByDescending { it.date }

        assertEquals("COVID Booster", sorted[0].vaccineName)
        assertEquals("Flu Shot 2025", sorted[1].vaccineName)
        assertEquals("Hep B Dose 1", sorted[2].vaccineName)
    }
}
