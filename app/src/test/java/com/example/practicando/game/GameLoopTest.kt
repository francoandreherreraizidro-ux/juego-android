package com.example.practicando.game

import org.junit.Assert.assertEquals
import org.junit.Test

class GameLoopTest {

    @Test
    fun calculateDeltaTimeNormalFrameAt60Fps() {
        val previous = 1_000_000_000L
        val current = 1_016_666_667L // ~16.66ms después
        val delta = GameLoop.calculateDeltaTime(current, previous)

        assertEquals(0.016666668f, delta, 0.0001f)
    }

    @Test
    fun calculateDeltaTimeClampedWhenFrameTakesTooLong() {
        val previous = 1_000_000_000L
        val current = 1_200_000_000L // 200ms después (un gran lag spike)
        val delta = GameLoop.calculateDeltaTime(current, previous)

        // Debe estar limitado a MAX_DELTA_TIME (0.05s)
        assertEquals(GameLoop.MAX_DELTA_TIME, delta, 0.0001f)
    }

    @Test
    fun calculateDeltaTimeZeroOnFirstFrame() {
        val previous = 0L
        val current = 1_000_000_000L
        val delta = GameLoop.calculateDeltaTime(current, previous)

        assertEquals(0f, delta, 0.0001f)
    }

    @Test
    fun calculateDeltaTimeZeroWhenCurrentIsLessThanPrevious() {
        val previous = 2_000_000_000L
        val current = 1_000_000_000L
        val delta = GameLoop.calculateDeltaTime(current, previous)

        assertEquals(0f, delta, 0.0001f)
    }
}
