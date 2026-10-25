package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.MedicalTerm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MedicalGlossaryRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _termsState = MutableStateFlow<List<MedicalTerm>>(emptyList())
    val termsFlow: StateFlow<List<MedicalTerm>> = _termsState.asStateFlow()

    private val _historyState = MutableStateFlow<List<String>>(emptyList())
    val historyFlow: StateFlow<List<String>> = _historyState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val json = prefs.getString(KEY_TERMS, null)
        val list: List<MedicalTerm> = if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<MedicalTerm>>() {}.type
                gson.fromJson(json, type) ?: defaultTerms()
            } catch (_: Exception) {
                defaultTerms()
            }
        } else {
            defaultTerms()
        }
        _termsState.value = list

        val historyJson = prefs.getString(KEY_HISTORY, null)
        val historyList: List<String> = if (!historyJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(historyJson, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        _historyState.value = historyList
    }

    private fun defaultTerms(): List<MedicalTerm> {
        val defaults = listOf(
            MedicalTerm(
                term = "BP",
                category = "Cardiology",
                simpleExplanation = "Blood Pressure - Khoon ka dabav jo arteries ki diwaro par lagta hai.",
                contextualExample = "Doctor ne kaha aapka BP 120/80 mmHg hai jo normal range me hai.",
                multipleMeaningsWarning = "Note: 'BP' primarily means Blood Pressure, but can occasionally refer to Biphasic Positive Airway pressure in respiratory care."
            ),
            MedicalTerm(
                term = "CBC",
                category = "Hematology",
                simpleExplanation = "Complete Blood Count - Ek blood test jo red blood cells, white blood cells aur platelets ki ginti check karta hai.",
                contextualExample = "Infection ya weakness ka pata lagane ke liye doctor ne CBC test likha hai."
            ),
            MedicalTerm(
                term = "ECG / EKG",
                category = "Cardiology",
                simpleExplanation = "Electrocardiogram - Dil ki electrical activity ko record karne wala test.",
                contextualExample = "Chene me dard (chest pain) ke baad doctor ne heart rhythm check karne ke liye ECG kiya."
            ),
            MedicalTerm(
                term = "MRI",
                category = "Radiology",
                simpleExplanation = "Magnetic Resonance Imaging - Magnet aur radio waves ka use karke body ke andar ke organs ki clear picture lena.",
                contextualExample = "G ghutne ki injury ya brain scan ke liye MRI scan kiya gaya."
            ),
            MedicalTerm(
                term = "UTI",
                category = "Urology",
                simpleExplanation = "Urinary Tract Infection - Peshab ki nali me infection jisme jalan ya dard ho sakta hai.",
                contextualExample = "Bar bar peshab aana ya jalan hona UTI ke lakshan ho sakte hain."
            ),
            MedicalTerm(
                term = "Hypertension",
                category = "Cardiology",
                simpleExplanation = "High Blood Pressure - Jab khoon ka dabav normal se zyada rehta hai.",
                contextualExample = "Hypertension ko control karne ke liye namak kam khana aur regular walk karna zaroori hai."
            ),
            MedicalTerm(
                term = "Anemia",
                category = "Hematology",
                simpleExplanation = "Khoon ki kami - Jab body me healthy red blood cells ya hemoglobin ki matra kam ho jati hai.",
                contextualExample = "Thakan aur kamzori anemia ke main symptoms hain; iron-rich diet se isme aaram milta hai."
            )
        )
        saveTermsToPrefs(defaults)
        return defaults
    }

    private fun saveTermsToPrefs(list: List<MedicalTerm>) {
        _termsState.value = list
        prefs.edit().putString(KEY_TERMS, gson.toJson(list)).apply()
    }

    private fun saveHistoryToPrefs(list: List<String>) {
        _historyState.value = list
        prefs.edit().putString(KEY_HISTORY, gson.toJson(list)).apply()
    }

    fun toggleFavorite(termId: String) {
        val current = _termsState.value.map {
            if (it.id == termId) it.copy(isFavorite = !it.isFavorite) else it
        }
        saveTermsToPrefs(current)
    }

    fun addSearchQueryToHistory(query: String) {
        if (query.isBlank()) return
        val current = _historyState.value.toMutableList()
        current.remove(query)
        current.add(0, query)
        if (current.size > 10) current.removeAt(current.size - 1)
        saveHistoryToPrefs(current)
    }

    fun searchTerms(query: String): List<MedicalTerm> {
        val terms = _termsState.value
        if (query.isBlank()) return terms
        val q = query.lowercase()
        return terms.filter {
            it.term.lowercase().contains(q) ||
                    it.simpleExplanation.lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
        }
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_medical_glossary_prefs"
        private const val KEY_TERMS = "key_medical_terms"
        private const val KEY_HISTORY = "key_search_history"

        @Volatile
        private var INSTANCE: MedicalGlossaryRepository? = null

        fun getInstance(context: Context): MedicalGlossaryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MedicalGlossaryRepository(context).also { INSTANCE = it }
            }
        }
    }
}
