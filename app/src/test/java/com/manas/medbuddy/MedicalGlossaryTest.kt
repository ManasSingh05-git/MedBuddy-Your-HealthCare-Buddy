package com.manas.medbuddy

import com.manas.medbuddy.data.model.MedicalTerm
import org.junit.Assert.*
import org.junit.Test

class MedicalGlossaryTest {

    @Test
    fun testMedicalTerm_multipleMeaningsWarning() {
        val term = MedicalTerm(
            term = "BP",
            simpleExplanation = "Blood Pressure",
            contextualExample = "120/80 mmHg",
            multipleMeaningsWarning = "Note: 'BP' can refer to Blood Pressure or Biphasic Positive Airway Pressure."
        )

        assertNotNull(term.multipleMeaningsWarning)
        assertTrue(term.multipleMeaningsWarning!!.contains("Blood Pressure"))
    }

    @Test
    fun testMedicalTerm_favoritesToggle() {
        val term = MedicalTerm(
            term = "CBC",
            simpleExplanation = "Complete Blood Count",
            contextualExample = "Blood test",
            isFavorite = false
        )

        val updated = term.copy(isFavorite = !term.isFavorite)
        assertTrue(updated.isFavorite)
    }

    @Test
    fun testMedicalTerm_searchFiltering() {
        val terms = listOf(
            MedicalTerm(term = "BP", simpleExplanation = "Blood Pressure", contextualExample = "Example"),
            MedicalTerm(term = "CBC", simpleExplanation = "Complete Blood Count", contextualExample = "Example")
        )

        val query = "pressure"
        val results = terms.filter {
            it.term.lowercase().contains(query) || it.simpleExplanation.lowercase().contains(query)
        }

        assertEquals(1, results.size)
        assertEquals("BP", results[0].term)
    }
}
