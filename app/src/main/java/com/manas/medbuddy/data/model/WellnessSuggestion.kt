package com.manas.medbuddy.data.model

import java.util.UUID

data class WellnessSuggestion(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val rationale: String,
    val isCompleted: Boolean = false
)
