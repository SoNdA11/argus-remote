package com.sonda11.argus.remote.adapter.time

import android.os.SystemClock
import com.sonda11.argus.remote.core.domain.port.ClockPort

/**
 * Monotonic clock that keeps counting while the app is in the background.
 */
class ElapsedRealtimeClock : ClockPort {
    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
}
