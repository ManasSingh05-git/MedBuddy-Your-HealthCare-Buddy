package com.manas.medbuddy.vaccination

import java.util.UUID

enum class VaccinationStatus {
    ALL,
    COMPLETED,
    PLANNED,
    UNVERIFIED
}

data class VaccinationRecord(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val vaccineName: String = "",
    val doseNumber: String = "Dose 1",
    val date: String = "",
    val providerName: String = "",
    val status: String = VaccinationStatus.COMPLETED.name,
    val localCertificatePath: String = "",
    val hasCertificate: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderDate: String = "",
    val reminderTime: String = "09:00 AM",
    val notes: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
