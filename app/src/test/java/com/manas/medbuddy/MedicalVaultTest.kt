package com.manas.medbuddy

import com.manas.medbuddy.vault.MedicalRecord
import com.manas.medbuddy.vault.VaultCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicalVaultTest {

    @Test
    fun testMedicalRecordCreationAndMetadata() {
        val record = MedicalRecord(
            id = "rec_101",
            userId = "user_456",
            title = "Cardiology Discharge Summary",
            category = VaultCategory.DISCHARGE_SUMMARY.name,
            doctorName = "Dr. Mehta",
            documentDate = "2026-10-25",
            localFilePath = "/data/user/0/com.manas.medbuddy/files/medical_vault/user_456/doc_123.pdf",
            fileSizeBytes = 1887436L,
            fileSizeFormatted = "1.8 MB",
            mimeType = "application/pdf",
            isPdf = true,
            tags = listOf("Heart", "ECG", "Cardiology"),
            notes = "Follow up after 6 months"
        )

        assertEquals("rec_101", record.id)
        assertEquals("user_456", record.userId)
        assertEquals("Cardiology Discharge Summary", record.title)
        assertEquals("DISCHARGE_SUMMARY", record.category)
        assertEquals("Dr. Mehta", record.doctorName)
        assertEquals("1.8 MB", record.fileSizeFormatted)
        assertTrue(record.isPdf)
        assertEquals(3, record.tags.size)
    }

    @Test
    fun testVaultCategories() {
        val categories = VaultCategory.entries.map { it.name }
        assertTrue(categories.contains("PRESCRIPTION"))
        assertTrue(categories.contains("LAB_REPORT"))
        assertTrue(categories.contains("DISCHARGE_SUMMARY"))
        assertTrue(categories.contains("VACCINATION_RECORD"))
        assertTrue(categories.contains("GENERAL_REPORT"))
    }

    @Test
    fun testSearchAndCategoryFilterLogic() {
        val records = listOf(
            MedicalRecord(id = "1", title = "Blood Test CBC", category = VaultCategory.LAB_REPORT.name, doctorName = "Dr. Sharma"),
            MedicalRecord(id = "2", title = "Heart Surgery Discharge", category = VaultCategory.DISCHARGE_SUMMARY.name, doctorName = "Dr. Mehta"),
            MedicalRecord(id = "3", title = "Covid Vaccine Certificate", category = VaultCategory.VACCINATION_RECORD.name, doctorName = "Govt Hospital")
        )

        val filteredLab = records.filter { it.category == VaultCategory.LAB_REPORT.name }
        assertEquals(1, filteredLab.size)
        assertEquals("Blood Test CBC", filteredLab.first().title)

        val filteredSearch = records.filter { it.doctorName.contains("Mehta", ignoreCase = true) }
        assertEquals(1, filteredSearch.size)
        assertEquals("Heart Surgery Discharge", filteredSearch.first().title)
    }
}
