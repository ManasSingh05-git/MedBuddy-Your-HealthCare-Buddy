package com.manas.medbuddy.appointments

import java.util.UUID

enum class AppointmentBookingType {
    LOCALLY_SAVED,      // User self-recorded personal appointment
    PROVIDER_CONFIRMED  // Confirmed directly by clinic/hospital provider
}

enum class AppointmentStatus {
    UPCOMING,
    COMPLETED,
    RESCHEDULED,
    CANCELLED
}

data class DoctorAppointment(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val doctorName: String = "",
    val specialty: String = "General Medicine",
    val clinicLocation: String = "",
    val appointmentDate: String = "",
    val appointmentTime: String = "10:00 AM",
    val bookingType: String = AppointmentBookingType.LOCALLY_SAVED.name,
    val status: String = AppointmentStatus.UPCOMING.name,
    val localAttachmentPath: String = "",
    val hasAttachment: Boolean = false,
    val reminderEnabled: Boolean = true,
    val reminderOffset: String = "1 Hour Before",
    val customReminderDateTime: String = "",
    val notes: String = "",
    val previousRescheduledDate: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
