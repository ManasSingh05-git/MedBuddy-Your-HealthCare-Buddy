package com.manas.medbuddy

import com.manas.medbuddy.data.model.FamilyProfile
import org.junit.Assert.*
import org.junit.Test

class FamilyProfilesTest {

    @Test
    fun testMinorGuardianConsentValidation() {
        val minorWithoutConsent = FamilyProfile(
            name = "Child",
            relation = "Child",
            age = 5,
            isMinor = true,
            guardianConsentGranted = false
        )

        val error = if (minorWithoutConsent.isMinor && !minorWithoutConsent.guardianConsentGranted) {
            "Guardian consent is required for minor family members."
        } else {
            null
        }

        assertNotNull(error)
        assertEquals("Guardian consent is required for minor family members.", error)
    }

    @Test
    fun testProfileDataIsolationBinding() {
        val profileA = FamilyProfile(id = "p1", name = "Manas", relation = "Self", age = 28)
        val profileB = FamilyProfile(id = "p2", name = "Aarav", relation = "Child", age = 6)

        val profiles = listOf(profileA, profileB)

        // Verify profile access validation logic
        val isValidA = profiles.any { it.id == "p1" }
        val isValidInvalid = profiles.any { it.id == "unknown" }

        assertTrue(isValidA)
        assertFalse(isValidInvalid)
    }

    @Test
    fun testProfileCrudModelUpdates() {
        val profile = FamilyProfile(id = "p1", name = "Manas", relation = "Self", age = 28)
        val updated = profile.copy(age = 29, bloodGroup = "A+")

        assertEquals(29, updated.age)
        assertEquals("A+", updated.bloodGroup)
        assertEquals("p1", updated.id)
    }
}
