package com.manas.medbuddy.data.model

import java.util.UUID

data class FamilyProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val relation: String, // "Self", "Spouse", "Child", "Parent"
    val age: Int,
    val bloodGroup: String = "O+",
    val isMinor: Boolean = false,
    val guardianConsentGranted: Boolean = true,
    val emergencyContact: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
