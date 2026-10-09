package com.manas.medbuddy

import com.manas.medbuddy.util.PhoneNumberValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberValidatorTest {

    @Test
    fun validPhoneNumbers_returnTrue() {
        assertTrue(PhoneNumberValidator.isValid("+12345678901"))
        assertTrue(PhoneNumberValidator.isValid("9876543210"))
        assertTrue(PhoneNumberValidator.isValid("+91 98765-43210"))
        assertTrue(PhoneNumberValidator.isValid("(555) 123-4567"))
    }

    @Test
    fun invalidPhoneNumbers_returnFalse() {
        assertFalse(PhoneNumberValidator.isValid(""))
        assertFalse(PhoneNumberValidator.isValid("   "))
        assertFalse(PhoneNumberValidator.isValid("12345")) // Too short (< 7 digits)
        assertFalse(PhoneNumberValidator.isValid("abcdefghijk")) // Non-digits
        assertFalse(PhoneNumberValidator.isValid("12345678901234567")) // Too long (> 15 digits)
    }

    @Test
    fun formatForCall_removesFormattingCharacters() {
        assertEquals("+12345678901", PhoneNumberValidator.formatForCall("+1 (234) 567-8901"))
        assertEquals("9876543210", PhoneNumberValidator.formatForCall("987-654-3210"))
    }
}
