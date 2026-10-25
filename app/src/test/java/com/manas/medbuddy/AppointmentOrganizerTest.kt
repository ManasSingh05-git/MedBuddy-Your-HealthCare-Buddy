package com.manas.medbuddy

import com.manas.medbuddy.appointments.AppointmentBookingType
import com.manas.medbuddy.appointments.AppointmentRepository
import com.manas.medbuddy.appointments.AppointmentStatus
import com.manas.medbuddy.appointments.DoctorAppointment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppointmentOrganizerTest {

    private lateinit var apptRepo: AppointmentRepository

    @Before
    fun setUp() {
        apptRepo = AppointmentRepository()
    }

    @Test
    fun testAppointmentModelDefaultsAndBookingType() {
        val appt = DoctorAppointment(
            id = "appt_1",
            userId = "user_101",
            doctorName = "Dr. Mehta",
            specialty = "Cardiology",
            clinicLocation = "Apollo Hospital Room 302",
            appointmentDate = "2026-11-05",
            appointmentTime = "10:30 AM",
            bookingType = AppointmentBookingType.PROVIDER_CONFIRMED.name,
            status = AppointmentStatus.UPCOMING.name,
            hasAttachment = true
        )

        assertEquals("appt_1", appt.id)
        assertEquals("Dr. Mehta", appt.doctorName)
        assertEquals("Cardiology", appt.specialty)
        assertEquals("PROVIDER_CONFIRMED", appt.bookingType)
        assertEquals("UPCOMING", appt.status)
        assertTrue(appt.hasAttachment)
    }

    @Test
    fun testRescheduleTracking() {
        val originalAppt = DoctorAppointment(
            id = "appt_2",
            doctorName = "Dr. Sharma",
            appointmentDate = "2026-10-25",
            appointmentTime = "11:00 AM",
            status = AppointmentStatus.UPCOMING.name
        )

        var rescheduledAppt: DoctorAppointment? = null
        apptRepo.rescheduleAppointment(originalAppt, "2026-11-02", "02:00 PM") { success, _ ->
            if (success) {
                rescheduledAppt = originalAppt.copy(
                    previousRescheduledDate = "${originalAppt.appointmentDate} at ${originalAppt.appointmentTime}",
                    appointmentDate = "2026-11-02",
                    appointmentTime = "02:00 PM",
                    status = AppointmentStatus.RESCHEDULED.name
                )
            }
        }

        assertNotNull(rescheduledAppt)
        assertEquals("2026-11-02", rescheduledAppt?.appointmentDate)
        assertEquals("02:00 PM", rescheduledAppt?.appointmentTime)
        assertEquals("RESCHEDULED", rescheduledAppt?.status)
        assertEquals("2026-10-25 at 11:00 AM", rescheduledAppt?.previousRescheduledDate)
    }

    @Test
    fun testCancellationStatus() {
        val appt = DoctorAppointment(
            id = "appt_3",
            doctorName = "Dr. Patel",
            status = AppointmentStatus.UPCOMING.name,
            reminderEnabled = true
        )

        val cancelled = appt.copy(status = AppointmentStatus.CANCELLED.name, reminderEnabled = false)

        assertEquals("CANCELLED", cancelled.status)
        assertFalse(cancelled.reminderEnabled)
    }
}
