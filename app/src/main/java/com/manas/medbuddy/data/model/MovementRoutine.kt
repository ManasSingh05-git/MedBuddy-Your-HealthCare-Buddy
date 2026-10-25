package com.manas.medbuddy.data.model

import java.util.UUID

data class MovementRoutine(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val durationSeconds: Int,
    val instructions: List<String>
)

data class MovementSessionLog(
    val id: String = UUID.randomUUID().toString(),
    val routineTitle: String,
    val completedAt: Long = System.currentTimeMillis(),
    val dateString: String = "Today"
)
