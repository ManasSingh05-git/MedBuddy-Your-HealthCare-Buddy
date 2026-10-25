package com.manas.medbuddy

import com.manas.medbuddy.reports.LabTestItem
import com.manas.medbuddy.reports.LabValueStatus
import com.manas.medbuddy.reports.ReportExplainerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReportExplainerTest {

    private lateinit var engine: ReportExplainerEngine

    @Before
    fun setUp() {
        engine = ReportExplainerEngine()
    }

    @Test
    fun testParseRawTextToLabItems() {
        val sampleReportText = """
            City Diagnostic Lab
            Patient: John Doe
            Hemoglobin: 10.2 g/dL (12.0 - 16.0)
            Fasting Blood Sugar: 145.0 mg/dL (70.0 - 99.0)
            Serum Creatinine: 0.9 mg/dL (0.6 - 1.2)
        """.trimIndent()

        val items = engine.parseRawTextToLabItems(sampleReportText)

        assertNotNull(items)
        assertTrue(items.isNotEmpty())

        val hbItem = items.find { it.name.contains("Hemoglobin", ignoreCase = true) }
        assertNotNull(hbItem)
        assertEquals(LabValueStatus.LOW, hbItem?.status)
        assertTrue(hbItem?.isOutofRange == true)

        val sugarItem = items.find { it.name.contains("Sugar", ignoreCase = true) }
        assertNotNull(sugarItem)
        assertEquals(LabValueStatus.HIGH, sugarItem?.status)
        assertTrue(sugarItem?.isOutofRange == true)

        val creatinineItem = items.find { it.name.contains("Creatinine", ignoreCase = true) }
        assertNotNull(creatinineItem)
        assertEquals(LabValueStatus.NORMAL, creatinineItem?.status)
    }

    @Test
    fun testEvaluateStatus() {
        assertEquals(LabValueStatus.LOW, engine.evaluateStatus(10.0, 12.0, 16.0))
        assertEquals(LabValueStatus.HIGH, engine.evaluateStatus(18.0, 12.0, 16.0))
        assertEquals(LabValueStatus.NORMAL, engine.evaluateStatus(14.0, 12.0, 16.0))
        assertEquals(LabValueStatus.UNKNOWN, engine.evaluateStatus(null, 12.0, 16.0))
    }

    @Test
    fun testGenerateSummariesEnglishAndHinglish() {
        val labItems = listOf(
            LabTestItem(name = "Hemoglobin", value = "10.2", unit = "g/dL", status = LabValueStatus.LOW, isOutofRange = true),
            LabTestItem(name = "Fasting Blood Sugar", value = "145", unit = "mg/dL", status = LabValueStatus.HIGH, isOutofRange = true)
        )

        val (eng, hing) = engine.generateSummaries(labItems)

        assertTrue(eng.contains("High values detected", ignoreCase = true))
        assertTrue(eng.contains("Low values detected", ignoreCase = true))
        assertTrue(hing.contains("normal se kam hai", ignoreCase = true) || hing.contains("bada hua hai", ignoreCase = true))
    }

    @Test
    fun testGenerateDoctorQuestions() {
        val labItems = listOf(
            LabTestItem(name = "Hemoglobin (Hb)", value = "10.2", unit = "g/dL", referenceRange = "12.0 - 16.0", status = LabValueStatus.LOW, isOutofRange = true)
        )

        val questions = engine.generateDoctorQuestions(labItems)

        assertTrue(questions.isNotEmpty())
        assertTrue(questions.first().contains("iron supplements", ignoreCase = true) || questions.first().contains("Hemoglobin", ignoreCase = true))
    }
}
