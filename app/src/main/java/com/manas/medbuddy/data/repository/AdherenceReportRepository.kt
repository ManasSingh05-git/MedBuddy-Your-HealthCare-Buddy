package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.AdherenceLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdherenceSummary(
    val totalScheduled: Int,
    val takenCount: Int,
    val skippedCount: Int,
    val unconfirmedCount: Int,
    val adherencePercentage: Float,
    val formulaDocumentation: String
)

class AdherenceReportRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _logsState = MutableStateFlow<List<AdherenceLog>>(emptyList())
    val logsFlow: StateFlow<List<AdherenceLog>> = _logsState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val json = prefs.getString(KEY_LOGS, null)
        val list: List<AdherenceLog> = if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<AdherenceLog>>() {}.type
                gson.fromJson(json, type) ?: defaultLogs()
            } catch (_: Exception) {
                defaultLogs()
            }
        } else {
            defaultLogs()
        }
        _logsState.value = list.sortedByDescending { it.timestamp }
    }

    private fun defaultLogs(): List<AdherenceLog> {
        val now = System.currentTimeMillis()
        val defaults = listOf(
            AdherenceLog(medicineName = "Paracetamol 500mg", memberName = "Manas Singh", status = "Taken", timestamp = now - 86400000L, dateString = "Yesterday"),
            AdherenceLog(medicineName = "Paracetamol 500mg", memberName = "Manas Singh", status = "Taken", timestamp = now - 43200000L, dateString = "Yesterday"),
            AdherenceLog(medicineName = "Vitamin D3", memberName = "Manas Singh", status = "Skipped", timestamp = now - 172800000L, dateString = "2 days ago"),
            AdherenceLog(medicineName = "Amlodipine 5mg", memberName = "Aarav Singh", status = "Unconfirmed", timestamp = now, dateString = "Today"),
            AdherenceLog(medicineName = "Vitamin D3", memberName = "Manas Singh", status = "Taken", timestamp = now, dateString = "Today")
        )
        saveToPrefs(defaults)
        return defaults
    }

    private fun saveToPrefs(list: List<AdherenceLog>) {
        val sorted = list.sortedByDescending { it.timestamp }
        _logsState.value = sorted
        prefs.edit().putString(KEY_LOGS, gson.toJson(sorted)).apply()
    }

    fun addLog(log: AdherenceLog) {
        val current = _logsState.value.toMutableList()
        current.add(0, log)
        saveToPrefs(current)
    }

    fun calculateSummary(
        medicineFilter: String,
        memberFilter: String,
        isAuthorized: Boolean
    ): AdherenceSummary {
        if (!isAuthorized) {
            return AdherenceSummary(0, 0, 0, 0, 0f, "Unauthorized access: Adherence summary restricted.")
        }

        val logs = _logsState.value.filter { log ->
            val matchMed = medicineFilter == "All" || log.medicineName.equals(medicineFilter, true)
            val matchMem = memberFilter == "All" || log.memberName.equals(memberFilter, true)
            matchMed && matchMem
        }

        val taken = logs.count { it.status.equals("Taken", true) }
        val skipped = logs.count { it.status.equals("Skipped", true) }
        val unconfirmed = logs.count { it.status.equals("Unconfirmed", true) }
        val totalScheduled = taken + skipped + unconfirmed

        // Adherence calculation formula: (Taken / (Taken + Skipped)) * 100.
        // Unconfirmed doses are kept separate and excluded from definitive denominator so they are never treated as definitely taken or missed.
        val denominator = taken + skipped
        val percentage = if (denominator > 0) {
            (taken.toFloat() / denominator.toFloat()) * 100f
        } else {
            0f
        }

        val formulaDesc = "Adherence Rate (%) = [Taken Doses / (Taken Doses + Skipped Doses)] * 100. Unconfirmed doses ($unconfirmed) are tracked separately and excluded from definitive adherence calculations."

        return AdherenceSummary(
            totalScheduled = totalScheduled,
            takenCount = taken,
            skippedCount = skipped,
            unconfirmedCount = unconfirmed,
            adherencePercentage = percentage,
            formulaDocumentation = formulaDesc
        )
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_adherence_reports_prefs"
        private const val KEY_LOGS = "key_adherence_logs"

        @Volatile
        private var INSTANCE: AdherenceReportRepository? = null

        fun getInstance(context: Context): AdherenceReportRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdherenceReportRepository(context).also { INSTANCE = it }
            }
        }
    }
}
