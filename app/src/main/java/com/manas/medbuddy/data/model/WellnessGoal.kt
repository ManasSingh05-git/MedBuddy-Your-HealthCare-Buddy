package com.manas.medbuddy.data.model

import java.util.UUID

data class WellnessGoal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetValue: Float,
    val currentProgress: Float = 0f,
    val unit: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
