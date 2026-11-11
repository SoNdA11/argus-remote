package com.sonda11.argus.remote.core.application

import com.sonda11.argus.remote.core.domain.model.HidUsage
import com.sonda11.argus.remote.core.domain.model.Host
import com.sonda11.argus.remote.core.domain.model.KeyStroke
import com.sonda11.argus.remote.core.domain.model.KeyboardFailure
import com.sonda11.argus.remote.core.domain.model.KeyboardStatus
import com.sonda11.argus.remote.core.domain.model.OperationResult
import com.sonda11.argus.remote.core.domain.model.SlideCommand
import com.sonda11.argus.remote.core.domain.port.HapticsPort
import com.sonda11.argus.remote.core.domain.port.KeyboardPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SendSlideCommandTest {
    private class FakeKeyboardPort(
        private val result: OperationResult,
    ) : KeyboardPort {
        override val status: StateFlow<KeyboardStatus> = MutableStateFlow(KeyboardStatus())
        val sent = mutableListOf<KeyStroke>()

        override suspend fun refresh() = Unit
        override suspend fun register(): OperationResult = result
        override suspend fun connect(host: Host): OperationResult = result
        override suspend fun disconnect() = Unit
        override suspend fun send(keystroke: KeyStroke): OperationResult {
            sent += keystroke
            return result
        }

        override suspend fun bondedHosts(): List<Host> = emptyList()
        override fun shutdown() = Unit
    }

    private class FakeHapticsPort : HapticsPort {
        var taps = 0
        override fun tap() {
            taps += 1
        }
    }

    @Test
    fun `successful command sends the mapped key and taps haptics`() = runTest {
        val keyboard = FakeKeyboardPort(OperationResult.Success)
        val haptics = FakeHapticsPort()
        val useCase = SendSlideCommand(keyboard, haptics)

        val result = useCase(SlideCommand.NEXT)

        assertEquals(OperationResult.Success, result)
        assertEquals(listOf(KeyStroke(HidUsage.RIGHT_ARROW)), keyboard.sent)
        assertEquals(1, haptics.taps)
    }

    @Test
    fun `failed command does not tap haptics`() = runTest {
        val keyboard = FakeKeyboardPort(OperationResult.Failure(KeyboardFailure.NotConnected))
        val haptics = FakeHapticsPort()
        val useCase = SendSlideCommand(keyboard, haptics)

        val result = useCase(SlideCommand.NEXT)

        assertEquals(OperationResult.Failure(KeyboardFailure.NotConnected), result)
        assertEquals(0, haptics.taps)
    }

    @Test
    fun `disabled haptics still send the key`() = runTest {
        val keyboard = FakeKeyboardPort(OperationResult.Success)
        val haptics = FakeHapticsPort()
        val useCase = SendSlideCommand(keyboard, haptics)

        val result = useCase(SlideCommand.NEXT, hapticsEnabled = false)

        assertEquals(OperationResult.Success, result)
        assertEquals(listOf(KeyStroke(HidUsage.RIGHT_ARROW)), keyboard.sent)
        assertEquals(0, haptics.taps)
    }
}
