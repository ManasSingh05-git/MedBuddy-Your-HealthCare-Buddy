package com.manas.medbuddy.allergy

import java.util.UUID

enum class AllergyCategory {
    ALL,
    MEDICATION,
    FOOD,
    ENVIRONMENTAL,
    OTHER
}

enum class AllergySeverity {
    MILD,
    MODERATE,
    SEVERE,
    LIFE_THREATENING
}

enum class AllergyVerificationStatus {
    VERIFIED,   // Verified by physician/lab test
    UNVERIFIED  // Patient self-reported
}

enum class AllergyProfileStatus {
    UNRECORDED,          // Registry has not been filled out yet
    NO_KNOWN_ALLERGIES,  // Explicitly confirmed NKDA by user
    HAS_ALLERGIES        // Has active allergy records
}

data class AllergyRecord(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val substance: String = "",
    val category: String = AllergyCategory.MEDICATION.name,
    val reportedReaction: String = "",
    val severity: String = AllergySeverity.MODERATE.name,
    val onsetDate: String = "",
    val isVerified: Boolean = false,
    val verifiedBy: String = "Self-Reported",
    val notes: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

data class AllergyMatchResult(
    val hasMatch: Boolean = false,
    val matchedSubstance: String = "",
    val allergyRecord: AllergyRecord? = null,
    val isVerified: Boolean = false,
    val warningMessage: String = ""
)
