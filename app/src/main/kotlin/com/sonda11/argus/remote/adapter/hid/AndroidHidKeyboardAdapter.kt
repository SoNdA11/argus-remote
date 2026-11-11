package com.sonda11.argus.remote.adapter.hid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.sonda11.argus.remote.core.domain.model.Host
import com.sonda11.argus.remote.core.domain.model.KeyStroke
import com.sonda11.argus.remote.core.domain.model.KeyboardFailure
import com.sonda11.argus.remote.core.domain.model.KeyboardStatus
import com.sonda11.argus.remote.core.domain.model.OperationResult
import com.sonda11.argus.remote.core.domain.port.KeyboardPort
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Android adapter for the Bluetooth HID device profile.
 *
 * The phone registers itself as a boot keyboard before the host pairs. All
 * calls are guarded by runtime permission checks; lint suppression is used
 * because the platform does not model those checks.
 */
@SuppressLint("MissingPermission")
class AndroidHidKeyboardAdapter(
    context: Context,
) : KeyboardPort {

    private val appContext = context.applicationContext
    private val bluetoothManager =
        appContext.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val mainExecutor = ContextCompat.getMainExecutor(appContext)

    private val _status = MutableStateFlow(
        KeyboardStatus(
            supported = adapter != null,
            bluetoothEnabled = runCatching { adapter?.isEnabled == true }.getOrDefault(false),
        ),
    )
    override val status: StateFlow<KeyboardStatus> = _status.asStateFlow()

    private var hidDevice: BluetoothHidDevice? = null
    private var proxyDeferred: CompletableDeferred<BluetoothHidDevice?>? = null
    private var registrationDeferred: CompletableDeferred<Boolean>? = null
    private var connectionDeferred: CompletableDeferred<Boolean>? = null
    private val sendMutex = Mutex()

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                val device = proxy as? BluetoothHidDevice
                hidDevice = device
                proxyDeferred?.complete(device)
                proxyDeferred = null
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
                _status.update { current ->
                    current.copy(registered = false, connectedHost = null)
                }
            }
        }
    }

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            val wasRegistering = registrationDeferred != null
            registrationDeferred?.complete(registered)
            _status.update { current ->
                current.copy(
                    registered = registered,
                    connectedHost = if (registered) current.connectedHost else null,
                    lastFailure = if (!registered && wasRegistering) {
                        KeyboardFailure.RegistrationFailed()
                    } else {
                        current.lastFailure
                    },
                )
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectionDeferred?.complete(true)
                    _status.update { current ->
                        current.copy(connectedHost = device.toHost(), lastFailure = null)
                    }
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectionDeferred?.complete(false)
                    _status.update { current ->
                        current.copy(connectedHost = null)
                    }
                }
            }
        }

        override fun onGetReport(
            device: BluetoothDevice,
            type: Byte,
            id: Byte,
            bufferSize: Int,
        ) {
            runCatching {
                hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_UNSUPPORTED_REQ)
            }
        }

        override fun onSetReport(
            device: BluetoothDevice,
            type: Byte,
            id: Byte,
            data: ByteArray,
        ) = Unit

        override fun onSetProtocol(device: BluetoothDevice, protocol: Byte) = Unit

        override fun onInterruptData(
            device: BluetoothDevice,
            reportId: Byte,
            data: ByteArray,
        ) = Unit

        override fun onVirtualCableUnplug(device: BluetoothDevice) {
            connectionDeferred?.complete(false)
            _status.update { current -> current.copy(connectedHost = null) }
        }
    }

    override suspend fun refresh() {
        val adapter = adapter
        if (adapter == null) {
            _status.value = KeyboardStatus(supported = false)
            return
        }

        val enabled = runCatching { adapter.isEnabled }.getOrDefault(false)
        val hasPermission = hasConnectPermission()
        val connectedHost = if (enabled && hasPermission) {
            runCatching { hidDevice?.connectedDevices?.firstOrNull()?.toHost() }.getOrNull()
        } else {
            null
        }

        _status.update { current ->
            current.copy(
                supported = true,
                bluetoothEnabled = enabled,
                registered = current.registered && enabled,
                connectedHost = connectedHost,
                lastFailure = when {
                    !hasPermission -> KeyboardFailure.PermissionDenied
                    current.lastFailure == KeyboardFailure.PermissionDenied -> null
                    else -> current.lastFailure
                },
            )
        }
    }

    override suspend fun register(): OperationResult {
        if (!hasConnectPermission()) return failure(KeyboardFailure.PermissionDenied)
        val adapter = adapter ?: return failure(KeyboardFailure.Unsupported)
        if (!runCatching { adapter.isEnabled }.getOrDefault(false)) {
            return failure(KeyboardFailure.BluetoothOff)
        }
        val device = ensureProxy() ?: return failure(KeyboardFailure.Unsupported)
        if (_status.value.registered) return OperationResult.Success

        val deferred = CompletableDeferred<Boolean>()
        registrationDeferred = deferred
        _status.update { current -> current.copy(lastFailure = null) }

        return try {
            val requested = device.registerApp(
                sdpSettings(),
                null,
                null,
                mainExecutor,
                hidCallback,
            )
            if (!requested) {
                return failure(KeyboardFailure.RegistrationFailed())
            }

            val registered = withTimeoutOrNull(REGISTER_TIMEOUT_MILLIS) {
                deferred.await()
            } ?: false

            if (registered) {
                OperationResult.Success
            } else {
                failure(KeyboardFailure.RegistrationFailed())
            }
        } catch (error: SecurityException) {
            failure(KeyboardFailure.PermissionDenied)
        } catch (error: Exception) {
            failure(KeyboardFailure.RegistrationFailed(error.message))
        } finally {
            if (registrationDeferred === deferred) {
                registrationDeferred = null
            }
        }
    }

    override suspend fun connect(host: Host): OperationResult {
        if (!hasConnectPermission()) return failure(KeyboardFailure.PermissionDenied)
        val adapter = adapter ?: return failure(KeyboardFailure.Unsupported)
        if (!runCatching { adapter.isEnabled }.getOrDefault(false)) {
            return failure(KeyboardFailure.BluetoothOff)
        }
        if (!_status.value.registered) {
            return failure(KeyboardFailure.NotRegistered)
        }
        val device = hidDevice ?: ensureProxy() ?: return failure(KeyboardFailure.Unsupported)

        val remoteDevice = try {
            adapter.getRemoteDevice(host.address)
        } catch (error: IllegalArgumentException) {
            return failure(KeyboardFailure.ConnectionFailed(error.message))
        }

        val bonded = runCatching { remoteDevice.bondState }.getOrDefault(BluetoothDevice.BOND_NONE)
        if (bonded != BluetoothDevice.BOND_BONDED) {
            return failure(KeyboardFailure.ConnectionFailed("The computer is not paired."))
        }

        val alreadyConnected = runCatching {
            device.connectedDevices.any { it.address == host.address }
        }.getOrDefault(false)
        if (alreadyConnected) {
            _status.update { current -> current.copy(connectedHost = host, lastFailure = null) }
            return OperationResult.Success
        }

        val deferred = CompletableDeferred<Boolean>()
        connectionDeferred = deferred

        return try {
            if (!device.connect(remoteDevice)) {
                return failure(KeyboardFailure.ConnectionFailed())
            }

            val connected = withTimeoutOrNull(CONNECT_TIMEOUT_MILLIS) {
                deferred.await()
            } ?: false

            if (connected) {
                _status.update { current ->
                    current.copy(connectedHost = host, lastFailure = null)
                }
                OperationResult.Success
            } else {
                failure(KeyboardFailure.ConnectionFailed())
            }
        } catch (error: SecurityException) {
            failure(KeyboardFailure.PermissionDenied)
        } catch (error: Exception) {
            failure(KeyboardFailure.ConnectionFailed(error.message))
        } finally {
            if (connectionDeferred === deferred) {
                connectionDeferred = null
            }
        }
    }

    override suspend fun disconnect() {
        if (!hasConnectPermission()) return
        val device = hidDevice ?: return
        runCatching {
            device.connectedDevices.forEach { host -> device.disconnect(host) }
        }
        _status.update { current -> current.copy(connectedHost = null) }
    }

    override suspend fun send(keystroke: KeyStroke): OperationResult {
        if (!hasConnectPermission()) return failure(KeyboardFailure.PermissionDenied)
        val device = hidDevice ?: return failure(KeyboardFailure.NotRegistered)
        if (!_status.value.registered) {
            return failure(KeyboardFailure.NotRegistered)
        }

        return sendMutex.withLock {
            val host = runCatching { device.connectedDevices.firstOrNull() }.getOrNull()
                ?: return@withLock failure(KeyboardFailure.NotConnected)

            try {
                val pressed = device.sendReport(
                    host,
                    HidReport.REPORT_ID,
                    HidReport.keyDown(keystroke),
                )
                if (!pressed) {
                    return@withLock failure(KeyboardFailure.SendFailed())
                }

                delay(KEY_HOLD_MILLIS)

                val released = device.sendReport(
                    host,
                    HidReport.REPORT_ID,
                    HidReport.keyUp(),
                )
                if (!released) {
                    return@withLock failure(KeyboardFailure.SendFailed())
                }

                OperationResult.Success
            } catch (error: SecurityException) {
                failure(KeyboardFailure.PermissionDenied)
            } catch (error: Exception) {
                failure(KeyboardFailure.SendFailed(error.message))
            } finally {
                runCatching {
                    device.sendReport(host, HidReport.REPORT_ID, HidReport.keyUp())
                }
            }
        }
    }

    override suspend fun bondedHosts(): List<Host> = withContext(Dispatchers.IO) {
        if (!hasConnectPermission()) {
            return@withContext emptyList()
        }

        runCatching {
            adapter?.bondedDevices
                ?.map { device -> device to device.toHost() }
                ?.sortedWith(
                    compareByDescending<Pair<BluetoothDevice, Host>> { (device, _) ->
                        device.isComputer()
                    }.thenBy(String.CASE_INSENSITIVE_ORDER) { (_, host) -> host.name },
                )
                ?.map { (_, host) -> host }
                ?: emptyList()
        }.getOrDefault(emptyList())
    }

    override fun shutdown() {
        runCatching { hidDevice?.unregisterApp() }
        runCatching {
            hidDevice?.let { device ->
                adapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, device)
            }
        }
        hidDevice = null
        registrationDeferred?.complete(false)
        registrationDeferred = null
        connectionDeferred?.complete(false)
        connectionDeferred = null
        _status.value = KeyboardStatus(
            supported = adapter != null,
            bluetoothEnabled = runCatching { adapter?.isEnabled == true }.getOrDefault(false),
        )
    }

    private suspend fun ensureProxy(): BluetoothHidDevice? {
        hidDevice?.let { return it }
        val adapter = adapter ?: return null

        val deferred = CompletableDeferred<BluetoothHidDevice?>()
        proxyDeferred = deferred

        val requested = runCatching {
            adapter.getProfileProxy(appContext, serviceListener, BluetoothProfile.HID_DEVICE)
        }.getOrDefault(false)

        if (!requested) {
            if (proxyDeferred === deferred) {
                proxyDeferred = null
            }
            return null
        }

        val device = withTimeoutOrNull(PROXY_TIMEOUT_MILLIS) { deferred.await() }
        if (proxyDeferred === deferred) {
            proxyDeferred = null
        }
        return device
    }

    private fun sdpSettings(): BluetoothHidDeviceAppSdpSettings =
        BluetoothHidDeviceAppSdpSettings(
            HidDescriptor.APP_NAME,
            HidDescriptor.APP_DESCRIPTION,
            HidDescriptor.APP_PROVIDER,
            BluetoothHidDevice.SUBCLASS1_KEYBOARD,
            HidDescriptor.BYTES,
        )

    private fun hasConnectPermission(): Boolean =
        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                appContext,
                android.Manifest.permission.BLUETOOTH_CONNECT,
            ) == PackageManager.PERMISSION_GRANTED

    private fun failure(reason: KeyboardFailure): OperationResult {
        _status.update { current -> current.copy(lastFailure = reason) }
        return OperationResult.Failure(reason)
    }

    private fun BluetoothDevice.toHost(): Host = Host(
        address = address,
        name = runCatching { name }.getOrNull() ?: address,
    )

    /**
     * Computers are listed first because a bonded headset or mouse can never
     * host the keyboard. The list is not filtered: a computer that reports an
     * unusual device class must still be selectable.
     */
    private fun BluetoothDevice.isComputer(): Boolean = runCatching {
        bluetoothClass?.majorDeviceClass == BluetoothClass.Device.Major.COMPUTER
    }.getOrDefault(false)

    private companion object {
        const val PROXY_TIMEOUT_MILLIS = 5_000L
        const val REGISTER_TIMEOUT_MILLIS = 10_000L
        const val CONNECT_TIMEOUT_MILLIS = 10_000L
        const val KEY_HOLD_MILLIS = 24L
    }
}
