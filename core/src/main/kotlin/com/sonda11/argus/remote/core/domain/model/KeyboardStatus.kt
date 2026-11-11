package com.sonda11.argus.remote.core.domain.model

/**
 * Snapshot of the Bluetooth HID keyboard state.
 */
data class KeyboardStatus(
    val supported: Boolean = true,
    val bluetoothEnabled: Boolean = false,
    val registered: Boolean = false,
    val connectedHost: Host? = null,
    val lastFailure: KeyboardFailure? = null,
) {
    val connected: Boolean
        get() = connectedHost != null
}
