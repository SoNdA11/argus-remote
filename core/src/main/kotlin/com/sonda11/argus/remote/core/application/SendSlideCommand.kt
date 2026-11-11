package com.sonda11.argus.remote.core.application

import com.sonda11.argus.remote.core.domain.model.OperationResult
import com.sonda11.argus.remote.core.domain.model.SlideCommand
import com.sonda11.argus.remote.core.domain.port.HapticsPort
import com.sonda11.argus.remote.core.domain.port.KeyboardPort
import com.sonda11.argus.remote.core.domain.service.ArgusKeyMap

/**
 * Translates a presentation command into a HID keystroke and sends it.
 *
 * Haptics are only triggered when the key was actually delivered, so a
 * disconnected host never produces a misleading vibration.
 */
class SendSlideCommand(
    private val keyboard: KeyboardPort,
    private val haptics: HapticsPort,
) {
    suspend operator fun invoke(command: SlideCommand, hapticsEnabled: Boolean = true): OperationResult {
        val result = keyboard.send(ArgusKeyMap.strokeFor(command))
        if (result is OperationResult.Success && hapticsEnabled) {
            haptics.tap()
        }
        return result
    }
}
