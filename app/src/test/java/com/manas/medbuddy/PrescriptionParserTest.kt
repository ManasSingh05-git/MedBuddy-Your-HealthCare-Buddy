package com.manas.medbuddy

import com.manas.medbuddy.scanner.ExtractedMedicineCandidate
import com.manas.medbuddy.scanner.PrescriptionScannerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrescriptionParserTest {

    private lateinit var scannerEngine: PrescriptionScannerEngine

    @Before
    fun setUp() {
        scannerEngine = PrescriptionScannerEngine()
    }

    @Test
    fun testParseClearPrescriptionText() {
        val sampleText = """
            Dr. Smith Clinic
            Date: 2026-10-25
            Rx
            Tab Amoxicillin 500mg
            1 tab twice daily
            Take after food
        """.trimIndent()

        val candidates = scannerEngine.parseRawTextToCandidates(sampleText)

        assertNotNull(candidates)
        assertTrue(candidates.isNotEmpty())

        val candidate = candidates.first()
        assertTrue(candidate.name.contains("Amoxicillin", ignoreCase = true))
        assertEquals("500mg", candidate.strength)
        assertTrue(candidate.dosage.isNotBlank())
        assertTrue(candidate.frequency.isNotBlank())
    }

    @Test
    fun testParseUnclearHandwritingFlagsPharmacistReview() {
        val ambiguousText = """
            Tab Paracetamol ??? mg
            1-0-1?
            Take as needed
        """.trimIndent()

        val candidates = scannerEngine.parseRawTextToCandidates(ambiguousText)

        assertTrue(candidates.isNotEmpty())
        val candidate = candidates.first()
        assertTrue(candidate.needsPharmacistReview)
        assertTrue(candidate.flagReason.contains("Unclear", ignoreCase = true) || candidate.flagReason.contains("Incomplete", ignoreCase = true) || candidate.flagReason.isNotBlank())
    }

    @Test
    fun testCandidateModelDefaults() {
        val candidate = ExtractedMedicineCandidate(
            name = "Lipitor",
            strength = "20mg",
            dosage = "1 Tablet",
            frequency = "Once Daily",
            instructions = "At bedtime"
        )

        assertEquals("Lipitor", candidate.name)
        assertEquals("20mg", candidate.strength)
        assertEquals("1 Tablet", candidate.dosage)
        assertEquals("Once Daily", candidate.frequency)
        assertEquals("At bedtime", candidate.instructions)
        assertFalse(candidate.needsPharmacistReview)
    }
}
