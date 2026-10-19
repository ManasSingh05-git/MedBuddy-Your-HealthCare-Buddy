package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.ActiveSleepSession
import com.manas.medbuddy.data.model.SleepRecord
import com.manas.medbuddy.util.SleepCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SleepRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _recordsState = MutableStateFlow<List<SleepRecord>>(emptyList())
    val recordsFlow: StateFlow<List<SleepRecord>> = _recordsState.asStateFlow()

    private val _activeSessionState = MutableStateFlow<ActiveSleepSession?>(null)
    val activeSessionFlow: StateFlow<ActiveSleepSession?> = _activeSessionState.asStateFlow()

    private val _goalState = MutableStateFlow(8.0f)
    val goalFlow: StateFlow<Float> = _goalState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // Load Records
        val recordsJson = prefs.getString(KEY_RECORDS, null)
        val recordsList: List<SleepRecord> = if (!recordsJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<SleepRecord>>() {}.type
                gson.fromJson(recordsJson, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        _recordsState.value = recordsList.sortedByDescending { it.endTimeMillis }

        // Load Active Session
        val activeJson = prefs.getString(KEY_ACTIVE_SESSION, null)
        if (!activeJson.isNullOrEmpty()) {
            try {
                _activeSessionState.value = gson.fromJson(activeJson, ActiveSleepSession::class.java)
            } catch (_: Exception) {
                _activeSessionState.value = null
            }
        } else {
            _activeSessionState.value = null
        }

        // Load Goal
        _goalState.value = prefs.getFloat(KEY_SLEEP_GOAL, 8.0f)
    }

    private fun saveRecordsToPrefs(list: List<SleepRecord>) {
        val sorted = list.sortedByDescending { it.endTimeMillis }
        _recordsState.value = sorted
        val json = gson.toJson(sorted)
        prefs.edit().putString(KEY_RECORDS, json).apply()
    }

    private fun saveActiveSessionToPrefs(session: ActiveSleepSession?) {
        _activeSessionState.value = session
        if (session != null) {
            val json = gson.toJson(session)
            prefs.edit().putString(KEY_ACTIVE_SESSION, json).apply()
        } else {
            prefs.edit().remove(KEY_ACTIVE_SESSION).apply()
        }
    }

    fun startSleepSession(startTimeMillis: Long = System.currentTimeMillis()) {
        val session = ActiveSleepSession(startTimeMillis = startTimeMillis)
        saveActiveSessionToPrefs(session)
    }

    fun stopSleepSession(
        endTimeMillis: Long = System.currentTimeMillis(),
        qualityRating: Int = 4,
        notes: String = ""
    ): SleepRecord? {
        val session = _activeSessionState.value ?: return null
        val record = SleepRecord(
            startTimeMillis = session.startTimeMillis,
            endTimeMillis = maxOf(endTimeMillis, session.startTimeMillis + 60000L),
            qualityRating = qualityRating,
            notes = notes,
            isAutoTracked = true
        )
        saveActiveSessionToPrefs(null)
        saveSleepRecord(record)
        return record
    }

    fun cancelActiveSession() {
        saveActiveSessionToPrefs(null)
    }

    fun saveSleepRecord(record: SleepRecord) {
        val current = _recordsState.value.toMutableList()
        val idx = current.indexOfFirst { it.id == record.id }
        if (idx >= 0) {
            current[idx] = record
        } else {
            current.add(record)
        }
        saveRecordsToPrefs(current)
    }

    fun deleteSleepRecord(id: String) {
        val filtered = _recordsState.value.filterNot { it.id == id }
        saveRecordsToPrefs(filtered)
    }

    fun updateSleepGoal(goalHours: Float) {
        val validGoal = goalHours.coerceIn(4.0f, 12.0f)
        _goalState.value = validGoal
        prefs.edit().putFloat(KEY_SLEEP_GOAL, validGoal).apply()
    }

    fun getLatestSleepRecord(): SleepRecord? {
        return _recordsState.value.firstOrNull()
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_sleep_prefs"
        private const val KEY_RECORDS = "key_sleep_records"
        private const val KEY_ACTIVE_SESSION = "key_active_sleep_session"
        private const val KEY_SLEEP_GOAL = "key_sleep_goal_hours"

        @Volatile
        private var INSTANCE: SleepRepository? = null

        fun getInstance(context: Context): SleepRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SleepRepository(context).also { INSTANCE = it }
            }
        }
    }
}
