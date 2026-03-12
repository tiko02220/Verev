package com.vector.verevcodex.data.repository.auth

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordResetCodePolicyTest {
    @Test
    fun generate_returnsSixDigitNumericCode() {
        val code = PasswordResetCodePolicy.generate(Random(7))

        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
    }

    @Test
    fun isExpired_falseWithinExpirationWindow() {
        val issuedAt = 1_000L
        val now = issuedAt + PasswordResetCodePolicy.EXPIRATION_MILLIS

        assertFalse(PasswordResetCodePolicy.isExpired(issuedAt, now))
    }

    @Test
    fun isExpired_trueAfterExpirationWindow() {
        val issuedAt = 1_000L
        val now = issuedAt + PasswordResetCodePolicy.EXPIRATION_MILLIS + 1L

        assertTrue(PasswordResetCodePolicy.isExpired(issuedAt, now))
    }
}
