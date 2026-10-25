package com.manas.medbuddy

import com.manas.medbuddy.allergy.AllergyRecord
import com.manas.medbuddy.card.HealthCardConfig
import com.manas.medbuddy.card.HealthCardRepository
import com.manas.medbuddy.data.model.EmergencyContact
import com.manas.medbuddy.model.Medicine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HealthCardTest {

    private lateinit var cardRepo: HealthCardRepository

    @Before
    fun setUp() {
        cardRepo = HealthCardRepository()
    }

    @Test
    fun testHealthCardConfigDefaultsAndVisibility() {
        val config = HealthCardConfig(
            fullName = "John Doe",
            bloodGroup = "O+",
            isOrganDonor = true,
            primaryPhysicianName = "Dr. Mehta",
            emergencyNotes = "Diabetic Type 2"
        )

        assertEquals("John Doe", config.fullName)
        assertEquals("O+", config.bloodGroup)
        assertTrue(config.isOrganDonor)
        assertTrue(config.showBloodGroup)
        assertTrue(config.showAllergies)
        assertTrue(config.hideSensitiveDetailsOnLockScreen)
    }

    @Test
    fun testGenerateShareableSummaryTextWithRedaction() {
        val config = HealthCardConfig(
            fullName = "John Doe",
            bloodGroup = "A+",
            emergencyNotes = "Pacemaker installed"
        )

        val contacts = listOf(EmergencyContact(id = "1", name = "Jane Doe", phoneNumber = "9876543210", relationship = "Spouse", isPrimary = true))
        val medicines = listOf(Medicine(id = "m1", name = "Aspirin", dosage = "75 mg"))
        val allergies = listOf(AllergyRecord(substance = "Penicillin", isVerified = false, reportedReaction = "Hives"))

        // Test Unmasked Summary
        val unmaskedSummary = cardRepo.generateShareableSummaryText(config, contacts, medicines, allergies, isPrivacyRedacted = false)
        assertTrue(unmaskedSummary.contains("John Doe"))
        assertTrue(unmaskedSummary.contains("Penicillin"))
        assertTrue(unmaskedSummary.contains("Self-Reported"))
        assertTrue(unmaskedSummary.contains("Pacemaker installed"))

        // Test Privacy Redacted Summary
        val redactedSummary = cardRepo.generateShareableSummaryText(config, contacts, medicines, allergies, isPrivacyRedacted = true)
        assertTrue(redactedSummary.contains("[REDACTED ALLERGY DETAILS"))
        assertTrue(redactedSummary.contains("[REDACTED MEDICINE NAME"))
        assertTrue(redactedSummary.contains("[REDACTED SENSITIVE NOTES]"))
    }
}
