package com.vector.verevcodex.common.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StaffValidationTest {

    @Test
    fun `staff password is invalid below backend minimum`() {
        assertFalse(isValidStaffPassword("123456789"))
    }

    @Test
    fun `staff password is valid at backend minimum`() {
        assertTrue(isValidStaffPassword("1234567890"))
    }
}
