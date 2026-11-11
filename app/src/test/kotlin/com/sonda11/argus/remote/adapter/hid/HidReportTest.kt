package com.sonda11.argus.remote.adapter.hid

import com.sonda11.argus.remote.core.domain.model.HidUsage
import com.sonda11.argus.remote.core.domain.model.KeyModifier
import com.sonda11.argus.remote.core.domain.model.KeyStroke
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class HidReportTest {
    @Test
    fun `right arrow report has no modifier and one key`() {
        assertArrayEquals(
            byteArrayOf(0, 0, 0x4F, 0, 0, 0, 0, 0),
            HidReport.keyDown(KeyStroke(HidUsage.RIGHT_ARROW)),
        )
    }

    @Test
    fun `shift is encoded in the modifier byte`() {
        assertArrayEquals(
            byteArrayOf(0x02, 0, 0x50.toByte(), 0, 0, 0, 0, 0),
            HidReport.keyDown(
                KeyStroke(HidUsage.LEFT_ARROW, setOf(KeyModifier.LEFT_SHIFT)),
            ),
        )
    }

    @Test
    fun `alt is encoded in the modifier byte`() {
        assertArrayEquals(
            byteArrayOf(0x04, 0, 0x4F, 0, 0, 0, 0, 0),
            HidReport.keyDown(
                KeyStroke(HidUsage.RIGHT_ARROW, setOf(KeyModifier.LEFT_ALT)),
            ),
        )
    }

    @Test
    fun `release report clears every byte`() {
        assertArrayEquals(ByteArray(HidReport.LENGTH), HidReport.keyUp())
        assertEquals(0, HidReport.REPORT_ID)
    }

    @Test
    fun `descriptor is a standard boot keyboard with six key slots`() {
        assertEquals(45, HidDescriptor.BYTES.size)
        assertEquals(0x05, HidDescriptor.BYTES.first().toInt())
        assertEquals(0x09, HidDescriptor.BYTES[2].toInt())
        assertEquals(0x06, HidDescriptor.BYTES[3].toInt())
        assertEquals(0xC0, HidDescriptor.BYTES.last().toInt() and 0xFF)
        assertEquals("Argus Remote", HidDescriptor.APP_NAME)
    }
}
