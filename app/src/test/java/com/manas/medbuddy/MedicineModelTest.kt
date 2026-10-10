package com.manas.medbuddy

import com.manas.medbuddy.model.DoseLog
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.model.ReminderTime
import com.manas.medbuddy.model.ReminderType
import com.manas.medbuddy.model.ScheduleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicineModelTest {

    @Test
    fun testMedicineCreationAndDefaults() {
        val medicine = Medicine(
            id = "med123",
            name = "Amoxicillin",
            dosage = "500 mg",
            time = "08:00 AM, 08:00 PM",
            instructions = "Take after breakfast and dinner",
            startDate = "2026-10-08",
            endDate = "2026-10-22",
            scheduleType = ScheduleType.SELECTED_DAYS.name,
            selectedDays = listOf("Mon", "Wed", "Fri"),
            reminderTimes = listOf(
                ReminderTime("1", "Morning", "08:00 AM", ReminderType.MORNING.name),
                ReminderTime("2", "Evening", "08:00 PM", ReminderType.EVENING.name)
            )
        )

        assertEquals("med123", medicine.id)
        assertEquals("Amoxicillin", medicine.name)
        assertEquals("500 mg", medicine.dosage)
        assertEquals("Take after breakfast and dinner", medicine.instructions)
        assertEquals(2, medicine.reminderTimes.size)
        assertEquals("SELECTED_DAYS", medicine.scheduleType)
        assertEquals(3, medicine.selectedDays.size)
    }

    @Test
    fun testDoseLogCreation() {
        val doseLog = DoseLog(
            id = "log123",
            medicineId = "med123",
            medicineName = "Amoxicillin",
            dosage = "500 mg",
            scheduledTime = "08:00 AM",
            date = "2026-10-08",
            status = "TAKEN"
        )

        assertEquals("log123", doseLog.id)
        assertEquals("med123", doseLog.medicineId)
        assertEquals("TAKEN", doseLog.status)
        assertTrue(doseLog.timestamp > 0)
    }

    @Test
    fun testReminderTimeTypes() {
        val reminder = ReminderTime(
            id = "r1",
            label = "Custom Night",
            time = "10:30 PM",
            type = ReminderType.CUSTOM.name
        )

        assertEquals("Custom Night", reminder.label)
        assertEquals("10:30 PM", reminder.time)
        assertEquals("CUSTOM", reminder.type)
    }
}
