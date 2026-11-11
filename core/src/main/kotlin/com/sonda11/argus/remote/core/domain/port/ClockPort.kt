package com.sonda11.argus.remote.core.domain.port

/**
 * Monotonic clock used by the presentation timer.
 *
 * The Android adapter uses SystemClock.elapsedRealtime(), so the elapsed time
 * keeps advancing while the app is in the background.
 */
interface ClockPort {
    fun elapsedRealtimeMillis(): Long
}
