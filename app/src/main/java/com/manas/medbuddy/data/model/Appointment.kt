package com.manas.medbuddy.data.model

import java.util.UUID

data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val doctorName: String,
    val specialty: String,
    val dateTime: String,
    val location: String
)
