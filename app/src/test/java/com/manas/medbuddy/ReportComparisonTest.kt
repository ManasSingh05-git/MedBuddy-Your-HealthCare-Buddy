package com.manas.medbuddy

import com.manas.medbuddy.data.model.LabTestItem
import com.manas.medbuddy.data.model.MedicalReport
import org.junit.Assert.*
import org.junit.Test

class ReportComparisonTest {

    @Test
    fun testMedicalReport_modelCreation() {
        val report = MedicalReport(
            reportTitle = "CBC Report",
            reportDate = "Oct 15, 2025",
            testItems = listOf(
                LabTestItem("Hemoglobin", 14.1, "g/dL", "13.5 - 17.5")
            )
        )
        assertEquals("CBC Report", report.reportTitle)
        assertEquals(1, report.testItems.size)
        assertEquals("Hemoglobin", report.testItems[0].testName)
    }

    @Test
    fun testTrendCalculationLogic() {
        val valueA = 12.0
        val valueB = 13.5

        val trend = when {
            valueB > valueA -> "Increased"
            valueB < valueA -> "Decreased"
            else -> "Stable"
        }

        assertEquals("Increased", trend)
    }

    @Test
    fun testUnitConversionLogic() {
        val unitA = "mg/dL"
        val unitB = "g/L"
        var valB = 140.0
        var isConverted = false

        if (unitA.equals("mg/dL", true) && unitB.equals("g/L", true)) {
            valB *= 10.0
            isConverted = true
        }

        assertTrue(isConverted)
        assertEquals(1400.0, valB, 0.01)
    }
}
