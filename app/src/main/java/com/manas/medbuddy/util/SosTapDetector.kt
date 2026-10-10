package com.manas.medbuddy.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SosTapDetector(
    private val scope: CoroutineScope,
    private val timeWindowMs: Long = 1200L,
    private val singleTapDelayMs: Long = 350L,
    private val onSingleTap: () -> Unit,
    private val onTripleTap: () -> Unit
) {
    private var tapCount = 0
    private var lastTapTime = 0L
    private var timerJob: Job? = null

    fun registerTap() {
        val now = System.currentTimeMillis()

        if (now - lastTapTime > timeWindowMs) {
            tapCount = 0
            timerJob?.cancel()
            timerJob = null
        }

        lastTapTime = now
        tapCount++

        if (tapCount == 1) {
            timerJob = scope.launch {
                delay(singleTapDelayMs)
                if (tapCount < 3) {
                    val count = tapCount
                    tapCount = 0
                    if (count >= 1) {
                        onSingleTap()
                    }
                }
            }
        } else if (tapCount >= 3) {
            timerJob?.cancel()
            timerJob = null
            tapCount = 0
            onTripleTap()
        }
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null
        tapCount = 0
        lastTapTime = 0L
    }
}
