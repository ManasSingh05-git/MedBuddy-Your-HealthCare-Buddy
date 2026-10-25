package com.manas.medbuddy.data.model

import java.util.UUID

data class CaregiverPermission(
    val id: String = UUID.randomUUID().toString(),
    val caregiverName: String,
    val caregiverEmail: String,
    val canViewAdherence: Boolean = true,
    val canViewAppointments: Boolean = true,
    val canViewVitals: Boolean = false,
    val isRevoked: Boolean = false,
    val grantedAt: Long = System.currentTimeMillis()
)
