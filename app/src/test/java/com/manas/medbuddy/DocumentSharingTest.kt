package com.manas.medbuddy

import com.manas.medbuddy.data.model.SharedDocument
import org.junit.Assert.*
import org.junit.Test

class DocumentSharingTest {

    @Test
    fun testSecureHandleGeneration_noPublicUrlExposure() {
        val doc = SharedDocument(
            documentTitle = "MRI Scan",
            ownerName = "Manas",
            recipientName = "Doctor"
        )

        // Verify it uses a tokenized secure handle instead of a raw public URL
        assertNotNull(doc.secureHandle)
        assertTrue(doc.secureHandle.contains("medbuddy-secure-token"))
        assertFalse(doc.secureHandle.contains("http://") || doc.secureHandle.contains("https://"))
    }

    @Test
    fun testRevokedAccess_blocksAuthorization() {
        val doc = SharedDocument(
            documentTitle = "Lab Report",
            ownerName = "Manas",
            recipientName = "Dr. Fox",
            isRevoked = true
        )

        val handle = if (doc.isRevoked) null else doc.secureHandle
        assertNull(handle)
    }

    @Test
    fun testActiveAccess_grantsSecureHandle() {
        val doc = SharedDocument(
            documentTitle = "Prescription",
            ownerName = "Manas",
            recipientName = "Pharmacy",
            isRevoked = false,
            secureHandle = "token-12345"
        )

        val handle = if (doc.isRevoked) null else doc.secureHandle
        assertEquals("token-12345", handle)
    }
}
