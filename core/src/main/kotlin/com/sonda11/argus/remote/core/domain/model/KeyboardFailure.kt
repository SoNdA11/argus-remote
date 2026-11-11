package com.sonda11.argus.remote.core.domain.model

/**
 * Reasons why a Bluetooth keyboard operation did not complete.
 */
sealed interface KeyboardFailure {
    data object Unsupported : KeyboardFailure
    data object BluetoothOff : KeyboardFailure
    data object PermissionDenied : KeyboardFailure
    data object NotRegistered : KeyboardFailure
    data object NotConnected : KeyboardFailure
    data object NoBondedHost : KeyboardFailure
    data class RegistrationFailed(val detail: String? = null) : KeyboardFailure
    data class ConnectionFailed(val detail: String? = null) : KeyboardFailure
    data class SendFailed(val detail: String? = null) : KeyboardFailure
    data class Unknown(val detail: String? = null) : KeyboardFailure
}

/**
 * Result of an operation that talks to the Bluetooth adapter.
 */
sealed interface OperationResult {
    data object Success : OperationResult
    data class Failure(val reason: KeyboardFailure) : OperationResult
}
