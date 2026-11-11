package com.sonda11.argus.remote.core.domain.port

import com.sonda11.argus.remote.core.domain.model.Host
import com.sonda11.argus.remote.core.domain.model.KeyStroke
import com.sonda11.argus.remote.core.domain.model.KeyboardStatus
import com.sonda11.argus.remote.core.domain.model.OperationResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Driven port for the Bluetooth HID keyboard.
 */
interface KeyboardPort {
    val status: StateFlow<KeyboardStatus>

    suspend fun refresh()

    suspend fun register(): OperationResult

    suspend fun connect(host: Host): OperationResult

    suspend fun disconnect()

    suspend fun send(keystroke: KeyStroke): OperationResult

    suspend fun bondedHosts(): List<Host>

    fun shutdown()
}
