package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.CycleRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenstrualRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _consentState = MutableStateFlow(false)
    val consentFlow: StateFlow<Boolean> = _consentState.asStateFlow()

    private val _recordsState = MutableStateFlow<List<CycleRecord>>(emptyList())
    val recordsFlow: StateFlow<List<CycleRecord>> = _recordsState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val consented = prefs.getBoolean(KEY_CONSENT, false)
        _consentState.value = consented

        val json = prefs.getString(KEY_RECORDS, null)
        val list: List<CycleRecord> = if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<CycleRecord>>() {}.type
                gson.fromJson(json, type) ?: defaultRecords()
            } catch (_: Exception) {
                defaultRecords()
            }
        } else {
            defaultRecords()
        }
        _recordsState.value = list.sortedByDescending { it.timestamp }
    }

    private fun defaultRecords(): List<CycleRecord> {
        val now = System.currentTimeMillis()
        val defaults = listOf(
            CycleRecord(
                startDate = "Oct 01, 2025",
                endDate = "Oct 06, 2025",
                cycleLengthDays = 28,
                symptoms = "Cramps, mild fatigue",
                notes = "Regular flow",
                timestamp = now - 2592000000L
            )
        )
        saveRecordsToPrefs(defaults)
        return defaults
    }

    private fun saveRecordsToPrefs(list: List<CycleRecord>) {
        val sorted = list.sortedByDescending { it.timestamp }
        _recordsState.value = sorted
        prefs.edit().putString(KEY_RECORDS, gson.toJson(sorted)).apply()
    }

    fun setConsent(granted: Boolean) {
        _consentState.value = granted
        prefs.edit().putBoolean(KEY_CONSENT, granted).apply()
    }

    fun addRecord(record: CycleRecord) {
        val current = _recordsState.value.toMutableList()
        current.add(0, record)
        saveRecordsToPrefs(current)
    }

    fun updateRecord(record: CycleRecord) {
        val current = _recordsState.value.toMutableList()
        val index = current.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            current[index] = record
            saveRecordsToPrefs(current)
        }
    }

    fun deleteRecord(id: String) {
        val filtered = _recordsState.value.filterNot { it.id == id }
        saveRecordsToPrefs(filtered)
    }

    fun calculateEstimatedNextPeriod(): String {
        val latest = _recordsState.value.firstOrNull() ?: return "No estimate available"
        // Simple estimation: add average cycle length (e.g. 28 days) to last timestamp
        val nextMillis = latest.timestamp + (latest.cycleLengthDays * 24L * 60L * 60L * 1000L)
        val date = java.util.Date(nextMillis)
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US)
        return sdf.format(date)
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_private_menstrual_prefs"
        private const val KEY_CONSENT = "key_menstrual_consent"
        private const val KEY_RECORDS = "key_menstrual_records"

        @Volatile
        private var INSTANCE: MenstrualRepository? = null

        fun getInstance(context: Context): MenstrualRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MenstrualRepository(context).also { INSTANCE = it }
            }
        }
    }
}
