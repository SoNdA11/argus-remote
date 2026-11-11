package com.sonda11.argus.remote.core.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * User preferences persisted on the device.
 */
data class RemoteSettings(
    val target: Duration = DEFAULT_TARGET,
    val hapticsEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
) {
    init {
        require(target > Duration.ZERO) { "Presentation target must be positive" }
    }

    companion object {
        val DEFAULT_TARGET: Duration = 17.minutes + 55.seconds
        const val MIN_TARGET_SECONDS: Long = 60L
        const val MAX_TARGET_SECONDS: Long = 180L * 60L
    }
}
