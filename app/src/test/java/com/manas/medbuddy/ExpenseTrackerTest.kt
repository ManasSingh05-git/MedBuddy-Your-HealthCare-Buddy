package com.manas.medbuddy

import com.manas.medbuddy.expenses.ExpenseCategory
import com.manas.medbuddy.expenses.ExpenseRepository
import com.manas.medbuddy.expenses.HealthcareExpense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExpenseTrackerTest {

    private lateinit var expenseRepo: ExpenseRepository

    @Before
    fun setUp() {
        expenseRepo = ExpenseRepository()
    }

    @Test
    fun testTotalSpendCalculation() {
        val list = listOf(
            HealthcareExpense(id = "1", title = "BP Pills", amount = 500.0, category = ExpenseCategory.MEDICINE.name),
            HealthcareExpense(id = "2", title = "Doctor Visit", amount = 1000.0, category = ExpenseCategory.CONSULTATION.name),
            HealthcareExpense(id = "3", title = "Blood Test", amount = 1500.0, category = ExpenseCategory.LAB_TEST.name)
        )

        val total = expenseRepo.calculateTotalSpend(list)
        assertEquals(3000.0, total, 0.01)
    }

    @Test
    fun testCategoryBreakdownPercentages() {
        val list = listOf(
            HealthcareExpense(id = "1", title = "Meds", amount = 500.0, category = ExpenseCategory.MEDICINE.name),
            HealthcareExpense(id = "2", title = "Doctor", amount = 500.0, category = ExpenseCategory.CONSULTATION.name)
        )

        val breakdown = expenseRepo.calculateCategoryBreakdown(list)
        assertEquals(2, breakdown.size)

        val medItem = breakdown.find { it.category == ExpenseCategory.MEDICINE }
        assertEquals(500.0, medItem?.totalAmount ?: 0.0, 0.01)
        assertEquals(50.0f, medItem?.percentage ?: 0.0f, 0.01f)
    }

    @Test
    fun testMonthFilteringAndCsvExport() {
        val list = listOf(
            HealthcareExpense(id = "1", title = "Oct Meds", amount = 400.0, date = "2026-10-10"),
            HealthcareExpense(id = "2", title = "Sep Meds", amount = 600.0, date = "2026-09-15")
        )

        val octList = expenseRepo.filterExpensesByMonth(list, "2026-10")
        assertEquals(1, octList.size)
        assertEquals("Oct Meds", octList.first().title)

        val csv = expenseRepo.generateCsvExportText(octList, "2026-10")
        assertTrue(csv.contains("Oct Meds"))
        assertTrue(csv.contains("400.0"))
        assertTrue(csv.contains("TOTAL SPEND:"))
    }
}
