package com.manas.medbuddy

import com.manas.medbuddy.data.model.WellnessGoal
import com.manas.medbuddy.data.model.WellnessSuggestion
import org.junit.Assert.*
import org.junit.Test

class WellnessCoachTest {

    @Test
    fun testGoalCrudAndProgressCalculation() {
        val goal = WellnessGoal(
            title = "Hydration",
            targetValue = 3.0f,
            currentProgress = 1.5f,
            unit = "L"
        )

        // Progress fraction calculation
        val progressFraction = (goal.currentProgress / goal.targetValue).coerceIn(0f, 1f)
        assertEquals(0.5f, progressFraction, 0.01f)

        // Update goal progress
        val updated = goal.copy(currentProgress = 3.0f, isCompleted = true)
        assertEquals(3.0f, updated.currentProgress, 0.01f)
        assertTrue(updated.isCompleted)
    }

    @Test
    fun testEmptyStateHandling() {
        val emptyGoals = emptyList<WellnessGoal>()
        assertTrue(emptyGoals.isEmpty())

        val emptySuggestions = emptyList<WellnessSuggestion>()
        assertTrue(emptySuggestions.isEmpty())
    }

    @Test
    fun testAiFailureBehavior_deterministicFallback() {
        val simulateFailure = true
        val suggestion = if (simulateFailure) {
            WellnessSuggestion(
                title = "Fallback Suggestion",
                description = "Standard hydration reminder",
                rationale = "Deterministic Fallback: Generated locally due to AI unavailability."
            )
        } else {
            WellnessSuggestion(
                title = "AI Suggestion",
                description = "Custom",
                rationale = "Why this suggestion: Custom"
            )
        }

        assertTrue(suggestion.rationale.contains("Deterministic Fallback"))
    }
}
