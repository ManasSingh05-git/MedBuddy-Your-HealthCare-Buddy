package com.manas.medbuddy.card

import java.util.UUID

data class HealthCardConfig(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val fullName: String = "Patient Name",
    val dateOfBirth: String = "",
    val age: String = "",
    val bloodGroup: String = "O+",
    val isOrganDonor: Boolean = false,
    val primaryPhysicianName: String = "",
    val primaryPhysicianPhone: String = "",
    val emergencyNotes: String = "",
    val showBloodGroup: Boolean = true,
    val showOrganDonor: Boolean = true,
    val showEmergencyNotes: Boolean = true,
    val showMedicines: Boolean = true,
    val showAllergies: Boolean = true,
    val showEmergencyContacts: Boolean = true,
    val hideSensitiveDetailsOnLockScreen: Boolean = true,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
