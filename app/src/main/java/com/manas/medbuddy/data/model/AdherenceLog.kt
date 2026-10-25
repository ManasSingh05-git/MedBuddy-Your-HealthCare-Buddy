package com.manas.medbuddy.data.model

import java.util.UUID

data class AdherenceLog(
    val id: String = UUID.randomUUID().toString(),
    val medicineName: String,
    val memberName: String = "Manas Singh",
    val status: String, // "Taken", "Skipped", "Unconfirmed"
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = "Today"
)
