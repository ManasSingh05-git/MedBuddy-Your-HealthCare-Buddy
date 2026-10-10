package com.manas.medbuddy

import com.manas.medbuddy.util.SosTapDetector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SosTapDetectorTest {

    @Test
    fun singleTap_triggersSingleTapCallbackAfterDelay() = runTest {
        var singleTapCount = 0
        var tripleTapCount = 0

        val detector = SosTapDetector(
            scope = this,
            timeWindowMs = 1200L,
            singleTapDelayMs = 350L,
            onSingleTap = { singleTapCount++ },
            onTripleTap = { tripleTapCount++ }
        )

        detector.registerTap()

        // Advance before delay expires
        advanceTimeBy(100L)
        assertEquals(0, singleTapCount)
        assertEquals(0, tripleTapCount)

        // Advance past single tap delay
        advanceTimeBy(300L)
        assertEquals(1, singleTapCount)
        assertEquals(0, tripleTapCount)
    }

    @Test
    fun tripleTap_triggersTripleTapAndCancelsSingleTap() = runTest {
        var singleTapCount = 0
        var tripleTapCount = 0

        val detector = SosTapDetector(
            scope = this,
            timeWindowMs = 1200L,
            singleTapDelayMs = 350L,
            onSingleTap = { singleTapCount++ },
            onTripleTap = { tripleTapCount++ }
        )

        // Register 3 taps in quick succession
        detector.registerTap()
        advanceTimeBy(50L)
        detector.registerTap()
        advanceTimeBy(50L)
        detector.registerTap()

        // Triple tap should fire immediately
        assertEquals(0, singleTapCount)
        assertEquals(1, tripleTapCount)

        // Advance time further to confirm single tap was NOT fired
        advanceTimeBy(500L)
        assertEquals(0, singleTapCount)
        assertEquals(1, tripleTapCount)
    }
}
