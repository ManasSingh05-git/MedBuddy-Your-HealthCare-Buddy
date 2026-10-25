package com.manas.medbuddy

import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.timeline.HealthTimelineRepository
import com.manas.medbuddy.timeline.TimelineDateFilter
import com.manas.medbuddy.timeline.TimelineEvent
import com.manas.medbuddy.timeline.TimelineSourceType
import com.manas.medbuddy.vault.MedicalRecord
import com.manas.medbuddy.vault.VaultCategory
import com.manas.medbuddy.vaccination.VaccinationRecord
import com.manas.medbuddy.vaccination.VaccinationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HealthTimelineTest {

    private lateinit var timelineRepo: HealthTimelineRepository

    @Before
    fun setUp() {
        timelineRepo = HealthTimelineRepository()
    }

    @Test
    fun testAggregateAndDeduplicateTimelineEvents() {
        val customEvents = listOf(
            TimelineEvent(id = "c_1", title = "Doctor Consultation", eventDate = "2026-10-25", isCustomUserEvent = true)
        )

        val vaultRecords = listOf(
            MedicalRecord(id = "v_1", title = "Discharge Summary", category = VaultCategory.DISCHARGE_SUMMARY.name, doctorName = "Dr. Mehta", documentDate = "2026-10-20")
        )

        val vaccinations = listOf(
            VaccinationRecord(id = "vac_1", vaccineName = "Covid Booster", date = "2026-09-15", status = VaccinationStatus.COMPLETED.name, hasCertificate = true)
        )

        val medicines = listOf(
            Medicine(id = "m_1", name = "Amoxicillin", startDate = "2026-10-01", dosage = "500 mg")
        )

        val aggregated = timelineRepo.aggregateAllTimelineEvents(customEvents, vaultRecords, vaccinations, medicines)

        assertNotNull(aggregated)
        assertEquals(4, aggregated.size)

        // Verify chronological sorting (newest first)
        assertEquals("Doctor Consultation", aggregated[0].title)
        assertEquals("Discharge Summary", aggregated[1].title)
        assertEquals("Prescribed Medicine: Amoxicillin", aggregated[2].title)
        assertEquals("Vaccine: Covid Booster (Dose 1)", aggregated[3].title)
    }

    @Test
    fun testSourceFactDistinction() {
        val customEvents = listOf(
            TimelineEvent(id = "c_1", title = "Self-Reported Symptom", eventDate = "2026-10-25", sourceType = TimelineSourceType.USER_REPORTED.name)
        )

        val vaultRecords = listOf(
            MedicalRecord(id = "v_1", title = "Hospital Lab Report", category = VaultCategory.LAB_REPORT.name, doctorName = "Dr. Sharma", documentDate = "2026-10-20")
        )

        val aggregated = timelineRepo.aggregateAllTimelineEvents(customEvents, vaultRecords, emptyList(), emptyList())

        val userEvent = aggregated.find { it.id == "c_1" }
        assertEquals(TimelineSourceType.USER_REPORTED.name, userEvent?.sourceType)

        val labFact = aggregated.find { it.id == "vault_v_1" }
        assertEquals(TimelineSourceType.CLINICIAN_FACT.name, labFact?.sourceType)
    }

    @Test
    fun testDateFiltering() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -40)
        val fortyDaysAgoStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val events = listOf(
            TimelineEvent(id = "1", title = "Recent Event", eventDate = todayStr),
            TimelineEvent(id = "2", title = "Old Event", eventDate = fortyDaysAgoStr)
        )

        val last30Days = timelineRepo.filterEventsByDate(events, TimelineDateFilter.LAST_30_DAYS)
        assertEquals(1, last30Days.size)
        assertEquals("Recent Event", last30Days.first().title)

        val allTime = timelineRepo.filterEventsByDate(events, TimelineDateFilter.ALL_TIME)
        assertEquals(2, allTime.size)
    }
}
