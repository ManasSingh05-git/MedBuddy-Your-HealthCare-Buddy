package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.SymptomRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SymptomRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _recordsState = MutableStateFlow<List<SymptomRecord>>(emptyList())
    val recordsFlow: StateFlow<List<SymptomRecord>> = _recordsState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val json = prefs.getString(KEY_SYMPTOMS, null)
        val list: List<SymptomRecord> = if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<SymptomRecord>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        _recordsState.value = list.sortedByDescending { it.timestamp }
    }

    private fun saveToPrefs(list: List<SymptomRecord>) {
        val sorted = list.sortedByDescending { it.timestamp }
        _recordsState.value = sorted
        val json = gson.toJson(sorted)
        prefs.edit().putString(KEY_SYMPTOMS, json).apply()
    }

    fun addSymptom(record: SymptomRecord) {
        val current = _recordsState.value.toMutableList()
        current.add(0, record)
        saveToPrefs(current)
    }

    fun updateSymptom(record: SymptomRecord) {
        val current = _recordsState.value.toMutableList()
        val index = current.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            current[index] = record
            saveToPrefs(current)
        }
    }

    fun deleteSymptom(id: String) {
        val filtered = _recordsState.value.filterNot { it.id == id }
        saveToPrefs(filtered)
    }

    fun generateNeutralSummary(): String {
        val records = _recordsState.value
        if (records.isEmpty()) {
            return "No symptoms recorded yet. Log your symptoms above to view a neutral summary of your health entries. Note: MedBuddy does not diagnose conditions."
        }
        val totalCount = records.size
        val avgSeverity = if (totalCount > 0) records.map { it.severity }.average() else 0.0
        val latest = records.first()

        return "You have recorded $totalCount symptom entry/entries (Average severity: ${String.format(java.util.Locale.US, "%.1f", avgSeverity)}/10). Most recent entry: \"${latest.symptomDescription}\" (${latest.onset}, severity ${latest.severity}/10). Note: This summary reflects user-entered information only and does not constitute a medical diagnosis."
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_symptom_prefs"
        private const val KEY_SYMPTOMS = "key_symptom_records"

        @Volatile
        private var INSTANCE: SymptomRepository? = null

        fun getInstance(context: Context): SymptomRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SymptomRepository(context).also { INSTANCE = it }
            }
        }
    }
}
