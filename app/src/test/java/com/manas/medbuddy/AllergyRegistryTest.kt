package com.manas.medbuddy

import com.manas.medbuddy.allergy.AllergyCategory
import com.manas.medbuddy.allergy.AllergyProfileStatus
import com.manas.medbuddy.allergy.AllergyRecord
import com.manas.medbuddy.allergy.AllergyRepository
import com.manas.medbuddy.allergy.AllergySeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AllergyRegistryTest {

    private lateinit var allergyRepo: AllergyRepository

    @Before
    fun setUp() {
        allergyRepo = AllergyRepository()
    }

    @Test
    fun testAllergyRecordDefaultsAndVerification() {
        val record = AllergyRecord(
            id = "alg_1",
            substance = "Penicillin",
            category = AllergyCategory.MEDICATION.name,
            reportedReaction = "Anaphylaxis / Hives",
            severity = AllergySeverity.LIFE_THREATENING.name,
            isVerified = true,
            verifiedBy = "Dr. Sharma (Skin Test)"
        )

        assertEquals("Penicillin", record.substance)
        assertEquals("MEDICATION", record.category)
        assertEquals("LIFE_THREATENING", record.severity)
        assertTrue(record.isVerified)
        assertEquals("Dr. Sharma (Skin Test)", record.verifiedBy)
    }

    @Test
    fun testDistinguishNkdaFromUnrecorded() {
        val unrecordedStatus = AllergyProfileStatus.UNRECORDED
        val nkdaStatus = AllergyProfileStatus.NO_KNOWN_ALLERGIES
        val activeStatus = AllergyProfileStatus.HAS_ALLERGIES

        assertEquals("UNRECORDED", unrecordedStatus.name)
        assertEquals("NO_KNOWN_ALLERGIES", nkdaStatus.name)
        assertEquals("HAS_ALLERGIES", activeStatus.name)
    }

    @Test
    fun testCheckMedicineAllergyMatch() {
        val allergies = listOf(
            AllergyRecord(
                substance = "Penicillin",
                category = AllergyCategory.MEDICATION.name,
                reportedReaction = "Rash & Swelling",
                severity = AllergySeverity.SEVERE.name,
                isVerified = false
            )
        )

        val matchResult = allergyRepo.checkMedicineAllergyMatch("Amoxicillin 500mg", allergies)

        assertTrue(matchResult.hasMatch)
        assertEquals("Penicillin", matchResult.matchedSubstance)
        assertFalse(matchResult.isVerified)
        assertTrue(matchResult.warningMessage.contains("Potential Allergy Match", ignoreCase = true))
        assertTrue(matchResult.warningMessage.contains("self-reported", ignoreCase = true))

        val noMatchResult = allergyRepo.checkMedicineAllergyMatch("Paracetamol 500mg", allergies)
        assertFalse(noMatchResult.hasMatch)
    }
}
