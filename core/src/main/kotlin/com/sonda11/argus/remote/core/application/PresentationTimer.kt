package com.sonda11.argus.remote.core.application

import com.sonda11.argus.remote.core.domain.model.RemoteSettings
import com.sonda11.argus.remote.core.domain.model.TimerSnapshot
import com.sonda11.argus.remote.core.domain.port.ClockPort
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Presentation timer driven by a monotonic clock.
 *
 * The state is held in memory. The target is persisted by [SettingsPort];
 * the elapsed time is lost if the process is killed.
 */
class PresentationTimer(
    private val clock: ClockPort,
) {
    private var target: Duration = RemoteSettings.DEFAULT_TARGET
    private var accumulatedMillis: Long = 0L
    private var runningSinceMillis: Long? = null

    fun setTarget(newTarget: Duration) {
        require(newTarget > Duration.ZERO) { "Presentation target must be positive" }
        target = newTarget
    }

    fun start() {
        if (runningSinceMillis == null) {
            runningSinceMillis = clock.elapsedRealtimeMillis()
        }
    }

    fun pause() {
        val startedAt = runningSinceMillis ?: return
        accumulatedMillis += elapsedSince(startedAt)
        runningSinceMillis = null
    }

    fun reset() {
        accumulatedMillis = 0L
        runningSinceMillis = null
    }

    fun snapshot(): TimerSnapshot {
        val runningSince = runningSinceMillis
        val elapsedMillis = accumulatedMillis + (runningSince?.let(::elapsedSince) ?: 0L)
        return TimerSnapshot(
            elapsed = elapsedMillis.milliseconds,
            target = target,
            running = runningSince != null,
        )
    }

    private fun elapsedSince(startedAt: Long): Long =
        (clock.elapsedRealtimeMillis() - startedAt).coerceAtLeast(0L)
}
