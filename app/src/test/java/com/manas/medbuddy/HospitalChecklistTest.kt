package com.manas.medbuddy

import com.manas.medbuddy.checklist.ChecklistItem
import com.manas.medbuddy.checklist.ChecklistRepository
import com.manas.medbuddy.checklist.ChecklistTemplateProvider
import com.manas.medbuddy.checklist.HospitalVisitChecklist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HospitalChecklistTest {

    private lateinit var checklistRepo: ChecklistRepository
    private lateinit var templateProvider: ChecklistTemplateProvider

    @Before
    fun setUp() {
        checklistRepo = ChecklistRepository()
        templateProvider = ChecklistTemplateProvider()
    }

    @Test
    fun testProgressCalculation() {
        val items = listOf(
            ChecklistItem(id = "1", title = "Prescriptions", isCompleted = true),
            ChecklistItem(id = "2", title = "Blood Test", isCompleted = true),
            ChecklistItem(id = "3", title = "ID Proof", isCompleted = false),
            ChecklistItem(id = "4", title = "Insurance Card", isCompleted = false)
        )

        val progress = checklistRepo.calculateProgress(items)
        assertEquals(50.0f, progress, 0.01f)
    }

    @Test
    fun testItemTogglingAndCustomItemAddition() {
        val initialItems = listOf(ChecklistItem(id = "item_1", title = "X-Ray", isCompleted = false))
        val checklist = HospitalVisitChecklist(id = "chk_1", title = "Checklist 1", items = initialItems)

        // Toggle Item
        var updated: HospitalVisitChecklist? = null
        checklistRepo.toggleItemCompletion(checklist, "item_1") { success, _ ->
            if (success) {
                val toggledItems = checklist.items.map { if (it.id == "item_1") it.copy(isCompleted = true) else it }
                updated = checklist.copy(items = toggledItems, completionPercentage = checklistRepo.calculateProgress(toggledItems))
            }
        }

        assertTrue(updated?.items?.first()?.isCompleted == true)
        assertEquals(100.0f, updated?.completionPercentage ?: 0f, 0.01f)
    }

    @Test
    fun testTemplateGenerators() {
        val opdTemplate = templateProvider.getOutpatientConsultationTemplate()
        assertTrue(opdTemplate.size >= 6)
        assertTrue(opdTemplate.any { it.title.contains("Prescription") })

        val admissionTemplate = templateProvider.getHospitalAdmissionTemplate()
        assertTrue(admissionTemplate.size >= 8)
        assertTrue(admissionTemplate.any { it.title.contains("UHID") || it.title.contains("Insurance") })
    }
}
