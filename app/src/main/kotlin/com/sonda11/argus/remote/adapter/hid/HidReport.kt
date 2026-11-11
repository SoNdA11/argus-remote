package com.sonda11.argus.remote.adapter.hid

import com.sonda11.argus.remote.core.domain.model.KeyStroke

/**
 * Builds the 8-byte input reports consumed by the host.
 */
object HidReport {
    const val REPORT_ID: Int = 0
    const val LENGTH = 8

    fun keyDown(stroke: KeyStroke): ByteArray {
        val modifier = stroke.modifiers.fold(0) { bits, modifierKey ->
            bits or modifierKey.hidBit
        }.toByte()

        return byteArrayOf(
            modifier,
            0,
            stroke.usage.toByte(),
            0,
            0,
            0,
            0,
            0,
        )
    }

    fun keyUp(): ByteArray = ByteArray(LENGTH)
}
