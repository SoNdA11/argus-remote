package com.sonda11.argus.remote.adapter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonda11.argus.remote.core.application.PresentationTimer
import com.sonda11.argus.remote.core.application.SendSlideCommand
import com.sonda11.argus.remote.core.domain.model.Host
import com.sonda11.argus.remote.core.domain.model.KeyboardFailure
import com.sonda11.argus.remote.core.domain.model.KeyboardStatus
import com.sonda11.argus.remote.core.domain.model.OperationResult
import com.sonda11.argus.remote.core.domain.model.RemoteSettings
import com.sonda11.argus.remote.core.domain.model.SlideCommand
import com.sonda11.argus.remote.core.domain.model.TimerSnapshot
import com.sonda11.argus.remote.core.domain.port.ClockPort
import com.sonda11.argus.remote.core.domain.port.KeyboardPort
import com.sonda11.argus.remote.core.domain.port.SettingsPort
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class RemoteUiState(
    val keyboard: KeyboardStatus = KeyboardStatus(),
    val settings: RemoteSettings = RemoteSettings(),
    val timer: TimerSnapshot = TimerSnapshot(
        elapsed = Duration.ZERO,
        target = RemoteSettings.DEFAULT_TARGET,
        running = false,
    ),
    val hosts: List<Host> = emptyList(),
    val busy: Boolean = false,
)

class RemoteViewModel(
    private val keyboard: KeyboardPort,
    private val settingsPort: SettingsPort,
    clock: ClockPort,
    private val sendSlideCommand: SendSlideCommand,
) : ViewModel() {

    private val timer = PresentationTimer(clock)
    private val _settings = MutableStateFlow(RemoteSettings())
    private val _timer = MutableStateFlow(timer.snapshot())
    private val _hosts = MutableStateFlow<List<Host>>(emptyList())
    private val _busy = MutableStateFlow(false)
    private val _error = MutableStateFlow<KeyboardFailure?>(null)

    val uiState: StateFlow<RemoteUiState> = combine(
        keyboard.status,
        _settings,
        _timer,
        _hosts,
        _busy,
    ) { status, settings, timerSnapshot, hosts, busy ->
        RemoteUiState(
            keyboard = status,
            settings = settings,
            timer = timerSnapshot,
            hosts = hosts,
            busy = busy,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = RemoteUiState(
            keyboard = keyboard.status.value,
            timer = timer.snapshot(),
        ),
    )

    val error: StateFlow<KeyboardFailure?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            settingsPort.settings.collect { settings ->
                _settings.value = settings
                timer.setTarget(settings.target)
                _timer.value = timer.snapshot()
            }
        }
        viewModelScope.launch {
            while (isActive) {
                _timer.value = timer.snapshot()
                delay(TIMER_TICK_MILLIS)
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            keyboard.refresh()
            _hosts.value = keyboard.bondedHosts()
        }
    }

    fun register() {
        viewModelScope.launch {
            _busy.value = true
            try {
                handle(keyboard.register())
                keyboard.refresh()
                _hosts.value = keyboard.bondedHosts()
            } finally {
                _busy.value = false
            }
        }
    }

    fun connect(host: Host) {
        viewModelScope.launch {
            _busy.value = true
            try {
                handle(keyboard.connect(host))
                keyboard.refresh()
            } finally {
                _busy.value = false
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            keyboard.disconnect()
            keyboard.refresh()
        }
    }

    fun send(command: SlideCommand) {
        viewModelScope.launch {
            handle(sendSlideCommand(command, hapticsEnabled = _settings.value.hapticsEnabled))
        }
    }

    fun startTimer() {
        timer.start()
        _timer.value = timer.snapshot()
    }

    fun pauseTimer() {
        timer.pause()
        _timer.value = timer.snapshot()
    }

    fun resetTimer() {
        timer.reset()
        _timer.value = timer.snapshot()
    }

    fun setTargetSeconds(seconds: Long) {
        val target = seconds
            .coerceIn(RemoteSettings.MIN_TARGET_SECONDS, RemoteSettings.MAX_TARGET_SECONDS)
            .seconds
        viewModelScope.launch {
            settingsPort.save(_settings.value.copy(target = target))
            timer.setTarget(target)
            _timer.value = timer.snapshot()
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPort.save(_settings.value.copy(hapticsEnabled = enabled))
        }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            settingsPort.save(_settings.value.copy(keepScreenOn = enabled))
        }
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        keyboard.shutdown()
        super.onCleared()
    }

    private fun handle(result: OperationResult) {
        if (result is OperationResult.Failure) {
            _error.value = result.reason
        }
    }

    private companion object {
        const val TIMER_TICK_MILLIS = 250L
    }
}
