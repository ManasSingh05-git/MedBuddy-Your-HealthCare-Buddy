package com.manas.medbuddy.data.model

import java.util.UUID

data class EmergencyContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val relationship: String = "",
    val isPrimary: Boolean = false
)
