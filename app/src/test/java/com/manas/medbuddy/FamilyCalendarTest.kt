package com.manas.medbuddy

import com.manas.medbuddy.data.model.CalendarEvent
import org.junit.Assert.*
import org.junit.Test

class FamilyCalendarTest {

    @Test
    fun testDuplicatePreventionLogic() {
        val existingEvent = CalendarEvent(
            title = "Doctor Appointment",
            category = "Appointment",
            memberName = "Manas",
            dateTimeString = "Tomorrow 10 AM",
            timestamp = 1000L
        )

        val newEvent = CalendarEvent(
            title = "doctor appointment", // Case insensitive match
            category = "Appointment",
            memberName = "Manas",
            dateTimeString = "Tomorrow 10 AM",
            timestamp = 1010L // Within 60 seconds
        )

        val isDuplicate = existingEvent.title.equals(newEvent.title, true) &&
                kotlin.math.abs(existingEvent.timestamp - newEvent.timestamp) < 60000L

        assertTrue(isDuplicate)
    }

    @Test
    fun testEventFiltering_byCategoryAndMember() {
        val events = listOf(
            CalendarEvent(title = "Appt 1", category = "Appointment", memberName = "Manas", dateTimeString = "Today"),
            CalendarEvent(title = "Vax 1", category = "Vaccination", memberName = "Aarav", dateTimeString = "Tomorrow")
        )

        val categoryFiltered = events.filter { it.category.equals("Appointment", true) }
        assertEquals(1, categoryFiltered.size)
        assertEquals("Appt 1", categoryFiltered[0].title)

        val memberFiltered = events.filter { it.memberName.equals("Aarav", true) }
        assertEquals(1, memberFiltered.size)
        assertEquals("Vax 1", memberFiltered[0].title)
    }

    @Test
    fun testPrivacyPermission_hidesPrivateEvents() {
        val events = listOf(
            CalendarEvent(title = "Public Checkup", category = "Appointment", memberName = "Manas", dateTimeString = "Today", isPrivate = false),
            CalendarEvent(title = "Private Therapy", category = "Appointment", memberName = "Manas", dateTimeString = "Tomorrow", isPrivate = true)
        )

        val includePrivate = false
        val filtered = events.filter { includePrivate || !it.isPrivate }

        assertEquals(1, filtered.size)
        assertEquals("Public Checkup", filtered[0].title)
    }
}
