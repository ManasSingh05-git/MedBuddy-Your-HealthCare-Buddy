package com.manas.medbuddy

import com.manas.medbuddy.insurance.ClaimStatus
import com.manas.medbuddy.insurance.InsuranceClaim
import com.manas.medbuddy.insurance.InsurancePolicy
import com.manas.medbuddy.insurance.InsuranceRepository
import com.manas.medbuddy.insurance.PolicyVerificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InsuranceOrganizerTest {

    private lateinit var insuranceRepo: InsuranceRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Before
    fun setUp() {
        insuranceRepo = InsuranceRepository()
    }

    @Test
    fun testInsurancePolicyDefaultsAndVerificationType() {
        val policy = InsurancePolicy(
            id = "pol_1",
            insurerName = "Star Health Insurance",
            policyNumber = "P/111211/01/2026/00123",
            verificationType = PolicyVerificationType.INSURER_VERIFIED_DOC.name,
            hasDocument = true
        )

        assertEquals("Star Health Insurance", policy.insurerName)
        assertEquals("P/111211/01/2026/00123", policy.policyNumber)
        assertEquals("INSURER_VERIFIED_DOC", policy.verificationType)
        assertTrue(policy.hasDocument)
    }

    @Test
    fun testExpiryCountdownCalculation() {
        val fromDate = dateFormat.parse("2026-10-01") ?: Date()
        val expiryDateStr = "2026-10-31"

        val daysRemaining = insuranceRepo.calculateDaysToExpiry(expiryDateStr, fromDate)
        assertEquals(30, daysRemaining)
    }

    @Test
    fun testClaimAdditionToPolicy() {
        val policy = InsurancePolicy(
            id = "pol_2",
            insurerName = "HDFC ERGO",
            policyNumber = "HDFC-9921"
        )

        val claim = InsuranceClaim(
            id = "clm_1",
            claimNumber = "CLM-101",
            hospitalName = "Apollo Hospital",
            claimAmount = 25000.0,
            status = ClaimStatus.SUBMITTED.name
        )

        var updatedPolicy: InsurancePolicy? = null
        insuranceRepo.addClaimToPolicy(policy, claim) { success, _ ->
            if (success) {
                updatedPolicy = policy.copy(claims = policy.claims + claim)
            }
        }

        assertEquals(1, updatedPolicy?.claims?.size)
        assertEquals("CLM-101", updatedPolicy?.claims?.first()?.claimNumber)
        assertEquals(25000.0, updatedPolicy?.claims?.first()?.claimAmount ?: 0.0, 0.01)
    }
}
