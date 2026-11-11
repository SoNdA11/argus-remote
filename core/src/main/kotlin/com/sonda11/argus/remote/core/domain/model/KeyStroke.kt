package com.sonda11.argus.remote.core.domain.model

/**
 * A single HID keyboard usage together with the modifier keys that must be held.
 */
data class KeyStroke(
    val usage: Int,
    val modifiers: Set<KeyModifier> = emptySet(),
) {
    init {
        require(usage in 0..0xFF) { "HID usage must fit in one byte: $usage" }
    }
}

/**
 * Modifier bits from the USB HID boot keyboard report.
 */
enum class KeyModifier(val hidBit: Int) {
    LEFT_CTRL(0x01),
    LEFT_SHIFT(0x02),
    LEFT_ALT(0x04),
    LEFT_GUI(0x08),
}
