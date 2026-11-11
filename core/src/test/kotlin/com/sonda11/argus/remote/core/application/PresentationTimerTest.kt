package com.sonda11.argus.remote.core.application

import com.sonda11.argus.remote.core.domain.port.ClockPort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class PresentationTimerTest {
    private class FakeClock(var nowMillis: Long = 0L) : ClockPort {
        override fun elapsedRealtimeMillis(): Long = nowMillis

        fun advance(millis: Long) {
            nowMillis += millis
        }
    }

    @Test
    fun `elapsed time advances only while running`() {
        val clock = FakeClock()
        val timer = PresentationTimer(clock)

        timer.start()
        clock.advance(65_000L)
        val running = timer.snapshot()

        assertTrue(running.running)
        assertEquals(65.seconds, running.elapsed)

        timer.pause()
        clock.advance(30_000L)

        val paused = timer.snapshot()
        assertFalse(paused.running)
        assertEquals(65.seconds, paused.elapsed)
    }

    @Test
    fun `reset clears elapsed time`() {
        val clock = FakeClock()
        val timer = PresentationTimer(clock)

        timer.start()
        clock.advance(10_000L)
        timer.reset()

        val snapshot = timer.snapshot()
        assertEquals(0.seconds, snapshot.elapsed)
        assertFalse(snapshot.running)
    }

    @Test
    fun `target can be changed and over target is reported`() {
        val clock = FakeClock()
        val timer = PresentationTimer(clock)
        timer.setTarget(2.minutes)

        timer.start()
        clock.advance(121_000L)

        val snapshot = timer.snapshot()
        assertTrue(snapshot.overTarget)
        assertEquals(2.minutes, snapshot.target)
        assertEquals((-1).seconds, snapshot.remaining)
    }

    @Test
    fun `snapshot is monotonic even if the clock moves backwards`() {
        val clock = FakeClock(10_000L)
        val timer = PresentationTimer(clock)
        timer.start()

        clock.nowMillis = 5_000L

        assertEquals(0.seconds, timer.snapshot().elapsed)
    }
}
