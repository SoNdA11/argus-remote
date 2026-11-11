package com.sonda11.argus.remote.adapter.hid

/**
 * USB HID boot keyboard descriptor: one modifier byte, one reserved byte and
 * six key slots. The host receives 8-byte input reports with report ID 0.
 */
object HidDescriptor {
    const val APP_NAME = "Argus Remote"
    const val APP_DESCRIPTION = "Presentation remote"
    const val APP_PROVIDER = "Argus"

    val BYTES: ByteArray = byteArrayOf(
        0x05, 0x01, // Usage page (Generic Desktop)
        0x09, 0x06, // Usage (Keyboard)
        0xA1.toByte(), 0x01, // Collection (Application)
        0x05, 0x07, //   Usage page (Keyboard/Keypad)
        0x19, 0xE0.toByte(), //   Usage minimum (224)
        0x29, 0xE7.toByte(), //   Usage maximum (231)
        0x15, 0x00, //   Logical minimum (0)
        0x25, 0x01, //   Logical maximum (1)
        0x75, 0x01, //   Report size (1)
        0x95.toByte(), 0x08, //   Report count (8)
        0x81.toByte(), 0x02, //   Input (Data, Variable, Absolute)
        0x95.toByte(), 0x01, //   Report count (1)
        0x75, 0x08, //   Report size (8)
        0x81.toByte(), 0x01, //   Input (Constant)
        0x95.toByte(), 0x06, //   Report count (6)
        0x75, 0x08, //   Report size (8)
        0x15, 0x00, //   Logical minimum (0)
        0x25, 0x65, //   Logical maximum (101)
        0x05, 0x07, //   Usage page (Keyboard/Keypad)
        0x19, 0x00, //   Usage minimum (0)
        0x29, 0x65, //   Usage maximum (101)
        0x81.toByte(), 0x00, //   Input (Data, Array)
        0xC0.toByte(), // End collection
    )
}
