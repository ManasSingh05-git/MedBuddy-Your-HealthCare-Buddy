package com.manas.medbuddy.util

object PhoneNumberValidator {
    /**
     * Validates if the phone number is in a standard valid format.
     * Allows leading '+' for country code and requires 7 to 15 numeric digits.
     */
    fun isValid(phoneNumber: String): Boolean {
        val trimmed = phoneNumber.trim()
        if (trimmed.isEmpty()) return false
        val digitsAndPlus = trimmed.replace(Regex("[\\s\\-()]"), "")
        return digitsAndPlus.matches(Regex("^\\+?[0-9]{7,15}$"))
    }

    /**
     * Formats phone number by removing spaces, dashes, and parentheses for calling intents.
     */
    fun formatForCall(phoneNumber: String): String {
        return phoneNumber.trim().replace(Regex("[^0-9+]"), "")
    }
}
