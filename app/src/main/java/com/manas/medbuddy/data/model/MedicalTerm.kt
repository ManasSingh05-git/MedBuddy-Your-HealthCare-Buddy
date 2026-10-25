package com.manas.medbuddy.data.model

import java.util.UUID

data class MedicalTerm(
    val id: String = UUID.randomUUID().toString(),
    val term: String,
    val category: String = "General",
    val simpleExplanation: String,
    val contextualExample: String,
    val multipleMeaningsWarning: String? = null,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
