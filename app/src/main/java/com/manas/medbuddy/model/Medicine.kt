package com.manas.medbuddy.model

data class Medicine(
    val id: String = "",
    val name: String = "",
    val dosage: String = "",
    val time: String = "",
    val frequency: String = "Daily",
    val instructions: String = "Take after food",
    val startDate: String = "",
    val endDate: String = "",
    val isActive: Boolean = true,
    val isTaken: Boolean = false
)
