package com.sonda11.argus.remote.core.domain.model

import kotlin.time.Duration

/**
 * Immutable view of the presentation timer.
 */
data class TimerSnapshot(
    val elapsed: Duration,
    val target: Duration,
    val running: Boolean,
) {
    val remaining: Duration
        get() = target - elapsed

    val overTarget: Boolean
        get() = elapsed > target
}
