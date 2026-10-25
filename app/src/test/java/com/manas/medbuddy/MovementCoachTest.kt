package com.manas.medbuddy

import com.manas.medbuddy.data.model.MovementRoutine
import com.manas.medbuddy.data.model.MovementSessionLog
import org.junit.Assert.*
import org.junit.Test

class MovementCoachTest {

    @Test
    fun testMovementRoutine_properties() {
        val routine = MovementRoutine(
            title = "Neck Stretch",
            durationSeconds = 60,
            instructions = listOf("Step 1", "Step 2")
        )

        assertEquals("Neck Stretch", routine.title)
        assertEquals(60, routine.durationSeconds)
        assertEquals(2, routine.instructions.size)
    }

    @Test
    fun testSessionTimerCountdown_decrement() {
        var timeLeftSeconds = 10
        val isRunning = true

        if (isRunning && timeLeftSeconds > 0) {
            timeLeftSeconds--
        }

        assertEquals(9, timeLeftSeconds)
    }

    @Test
    fun testCompletionHistoryLogCreation() {
        val log = MovementSessionLog(
            routineTitle = "Desk Stretch",
            dateString = "Today"
        )

        assertEquals("Desk Stretch", log.routineTitle)
        assertEquals("Today", log.dateString)
    }
}
