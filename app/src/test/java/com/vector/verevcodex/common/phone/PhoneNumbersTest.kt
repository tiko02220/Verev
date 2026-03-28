package com.vector.verevcodex.common.phone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumbersTest {

    @Test
    fun `empty phone input keeps fixed armenia prefix visible`() {
        assertEquals("+374", sanitizePhoneNumberInput(""))
    }

    @Test
    fun `local digits are normalized to fixed armenia prefix`() {
        assertEquals("+37499111222", normalizePhoneNumber("099111222"))
    }

    @Test
    fun `full armenia number remains canonical`() {
        assertEquals("+37499111222", normalizePhoneNumber("+37499111222"))
    }

    @Test
    fun `phone validity requires exactly eight local digits`() {
        assertTrue(isValidPhoneNumber("+37499111222"))
        assertFalse(isValidPhoneNumber("+3749911122"))
    }
}
