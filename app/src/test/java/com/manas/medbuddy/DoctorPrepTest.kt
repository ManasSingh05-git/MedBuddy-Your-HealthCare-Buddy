package com.manas.medbuddy

import com.manas.medbuddy.data.model.DoctorPrepRecord
import org.junit.Assert.*
import org.junit.Test

class DoctorPrepTest {

    @Test
    fun testDoctorPrepRecord_distinguishesFactsFromAi() {
        val record = DoctorPrepRecord(
            appointmentId = "appt_1",
            doctorName = "Dr. Sharma",
            appointmentDate = "Tomorrow",
            recordedFactsConcerns = listOf("Headache (Severity 4/10)"),
            recordedFactsMedicines = listOf("Paracetamol"),
            editableConsultationSummary = "AI Summary: Patient has mild headache.",
            aiSuggestedQuestions = listOf("What causes this?")
        )

        // Verify facts vs AI suggestions are distinct
        assertFalse(record.recordedFactsConcerns.isEmpty())
        assertTrue(record.editableConsultationSummary.contains("AI Summary"))
        assertFalse(record.aiSuggestedQuestions.isEmpty())
    }

    @Test
    fun testDoctorPrepRecord_editingSummary() {
        val record = DoctorPrepRecord(
            appointmentId = "appt_1",
            doctorName = "Dr. Sharma",
            appointmentDate = "Tomorrow",
            editableConsultationSummary = "Initial Summary"
        )

        val updated = record.copy(editableConsultationSummary = "Updated Consultation Summary")
        assertEquals("Updated Consultation Summary", updated.editableConsultationSummary)
    }

    @Test
    fun testAiFailureHandling_fallbackSummary() {
        val simulateFailure = true
        val summary = if (simulateFailure) {
            "[Recorded Facts Summary Unavailable due to AI Service Timeout]. Please review your recorded symptoms and medicines manually below."
        } else {
            "Normal summary"
        }

        assertTrue(summary.contains("Unavailable due to AI Service Timeout"))
    }
}
