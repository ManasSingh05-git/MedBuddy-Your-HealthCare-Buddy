package com.manas.medbuddy.util

object SymptomSafetyChecker {

    private val seriousKeywords = listOf(
        "chest pain",
        "chest tightness",
        "difficulty breathing",
        "shortness of breath",
        "severe bleeding",
        "unconscious",
        "stroke",
        "face drooping",
        "slurred speech",
        "suicidal",
        "severe allergic reaction",
        "anaerobic",
        "high fever with stiff neck",
        "coughing blood",
        "sudden severe headache",
        "seizure"
    )

    fun checkIsSerious(description: String, associated: String): Boolean {
        val combined = "$description $associated".lowercase()
        return seriousKeywords.any { keyword ->
            combined.contains(keyword)
        }
    }

    fun getUrgentCareGuidance(description: String, associated: String): String? {
        return if (checkIsSerious(description, associated)) {
            "⚠️ Potentially serious symptoms detected (e.g., chest pain, breathing difficulty, or neurological signs). Please seek immediate medical attention or call emergency services (911 / 112 / local emergency). Note: MedBuddy does not diagnose conditions."
        } else {
            null
        }
    }
}
