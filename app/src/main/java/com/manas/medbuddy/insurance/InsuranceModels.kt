package com.manas.medbuddy.insurance

import java.util.UUID

enum class PolicyVerificationType {
    USER_SELF_REPORTED,      // User notes and unverified upload
    INSURER_VERIFIED_DOC     // Official insurer policy document
}

enum class ClaimStatus {
    DRAFT,
    SUBMITTED,
    IN_PROGRESS,
    SETTLED,
    REJECTED
}

data class InsuranceClaim(
    val id: String = UUID.randomUUID().toString(),
    val claimNumber: String = "",
    val hospitalName: String = "",
    val claimAmount: Double = 0.0,
    val status: String = ClaimStatus.SUBMITTED.name,
    val submissionDate: String = "",
    val localBillPath: String = "",
    val hasBillAttachment: Boolean = false,
    val notes: String = ""
)

data class InsurancePolicy(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val insurerName: String = "Star Health Insurance",
    val policyNumber: String = "",
    val planType: String = "Comprehensive Family Health",
    val sumInsured: Double = 500000.0,
    val startDate: String = "",
    val expiryDate: String = "",
    val insurerContactPhone: String = "",
    val insurerSupportEmail: String = "",
    val verificationType: String = PolicyVerificationType.USER_SELF_REPORTED.name,
    val localDocumentPath: String = "",
    val hasDocument: Boolean = false,
    val coverageNotes: String = "",
    val reminderOffsetDays: Int = 30,
    val expiryReminderEnabled: Boolean = true,
    val claims: List<InsuranceClaim> = emptyList(),
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
