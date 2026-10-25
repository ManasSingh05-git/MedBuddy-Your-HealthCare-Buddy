package com.manas.medbuddy

import com.manas.medbuddy.data.model.SymptomRecord
import com.manas.medbuddy.util.SymptomSafetyChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SymptomJournalTest {

    @Test
    fun testSymptomSafetyChecker_detectsSeriousSymptoms() {
        // Serious keywords
        assertTrue(SymptomSafetyChecker.checkIsSerious("chest pain and dizziness", ""))
        assertTrue(SymptomSafetyChecker.checkIsSerious("difficulty breathing", "shortness of breath"))
        assertTrue(SymptomSafetyChecker.checkIsSerious("severe bleeding from arm", ""))

        // Non-serious symptoms / Hinglish mild
        assertFalse(SymptomSafetyChecker.checkIsSerious("halka sir dard ho raha hai", "mild fatigue"))
        assertFalse(SymptomSafetyChecker.checkIsSerious("running nose and cough", ""))
    }

    @Test
    fun testSymptomRecord_severityLabels() {
        val mildRecord = SymptomRecord(symptomDescription = "Cold", severity = 2)
        assertEquals("Mild", mildRecord.getSeverityLabel())

        val moderateRecord = SymptomRecord(symptomDescription = "Back pain", severity = 5)
        assertEquals("Moderate", moderateRecord.getSeverityLabel())

        val severeRecord = SymptomRecord(symptomDescription = "Migraine", severity = 9)
        assertEquals("Severe", severeRecord.getSeverityLabel())
    }

    @Test
    fun testUrgentCareGuidance_returnsWarningWhenSerious() {
        val guidance = SymptomSafetyChecker.getUrgentCareGuidance("chest pain", "sweating")
        assertNotNull(guidance)
        assertTrue(guidance!!.contains("Potentially serious symptoms"))

        val normalGuidance = SymptomSafetyChecker.getUrgentCareGuidance("headache", "")
        assertTrue(normalGuidance == null)
    }
}
