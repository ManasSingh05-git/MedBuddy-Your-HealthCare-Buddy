package com.manas.medbuddy.data.model

import java.util.UUID

data class SharedDocument(
    val id: String = UUID.randomUUID().toString(),
    val documentTitle: String,
    val ownerName: String,
    val recipientName: String,
    val secureHandle: String = "medbuddy-secure-token-${UUID.randomUUID().toString().take(8)}",
    val isRevoked: Boolean = false,
    val sharedAt: Long = System.currentTimeMillis()
)

data class DocumentAuditLog(
    val id: String = UUID.randomUUID().toString(),
    val action: String, // "Shared", "Revoked", "Access Attempted"
    val documentTitle: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
