package com.openchlaw.broke

import org.junit.Test
import org.junit.Assert.*

class HexUtilTest {
    @Test
    fun conversion_isCorrect() {
        val input = byteArrayOf(0x0A, 0xFF.toByte(), 0x12)
        val expected = "0AFF12"
        assertEquals(expected, HexUtil.bytesToHex(input))
    }

    @Test
    fun emptyArray_returnsEmptyString() {
        val input = byteArrayOf()
        assertEquals("", HexUtil.bytesToHex(input))
    }
}
