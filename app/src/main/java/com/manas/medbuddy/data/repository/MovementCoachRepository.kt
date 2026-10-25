package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.MovementRoutine
import com.manas.medbuddy.data.model.MovementSessionLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MovementCoachRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _routinesState = MutableStateFlow<List<MovementRoutine>>(defaultRoutines())
    val routinesFlow: StateFlow<List<MovementRoutine>> = _routinesState.asStateFlow()

    private val _logsState = MutableStateFlow<List<MovementSessionLog>>(emptyList())
    val logsFlow: StateFlow<List<MovementSessionLog>> = _logsState.asStateFlow()

    private val _intervalMinutesState = MutableStateFlow(45)
    val intervalMinutesFlow: StateFlow<Int> = _intervalMinutesState.asStateFlow()

    private val _remindersEnabledState = MutableStateFlow(true)
    val remindersEnabledFlow: StateFlow<Boolean> = _remindersEnabledState.asStateFlow()

    private val _quietHoursState = MutableStateFlow("22:00 - 07:00")
    val quietHoursFlow: StateFlow<String> = _quietHoursState.asStateFlow()

    private val _reducedMotionState = MutableStateFlow(false)
    val reducedMotionFlow: StateFlow<Boolean> = _reducedMotionState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val interval = prefs.getInt(KEY_INTERVAL, 45)
        _intervalMinutesState.value = interval

        val remEnabled = prefs.getBoolean(KEY_REMINDERS, true)
        _remindersEnabledState.value = remEnabled

        val quiet = prefs.getString(KEY_QUIET, "22:00 - 07:00") ?: "22:00 - 07:00"
        _quietHoursState.value = quiet

        val reduced = prefs.getBoolean(KEY_REDUCED_MOTION, false)
        _reducedMotionState.value = reduced

        val json = prefs.getString(KEY_LOGS, null)
        val list: List<MovementSessionLog> = if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<MovementSessionLog>>() {}.type
                gson.fromJson(json, type) ?: defaultLogs()
            } catch (_: Exception) {
                defaultLogs()
            }
        } else {
            defaultLogs()
        }
        _logsState.value = list.sortedByDescending { it.completedAt }
    }

    private fun defaultRoutines(): List<MovementRoutine> {
        return listOf(
            MovementRoutine(
                title = "Neck & Shoulder Release",
                durationSeconds = 120, // 2 mins
                instructions = listOf(
                    "Sit straight and relax your shoulders.",
                    "Slowly tilt your right ear toward your right shoulder for 15 seconds.",
                    "Repeat on the left side.",
                    "Roll your shoulders backward in gentle circles 5 times."
                )
            ),
            MovementRoutine(
                title = "Desk Spinal Decompression",
                durationSeconds = 180, // 3 mins
                instructions = listOf(
                    "Stand up slowly from your chair.",
                    "Reach both hands overhead and stretch upward as tall as possible.",
                    "Gently arch your upper back and take 3 deep breaths.",
                    "Touch your toes with a relaxed lower back."
                )
            ),
            MovementRoutine(
                title = "Wrist & Hand Mobility",
                durationSeconds = 90, // 1.5 mins
                instructions = listOf(
                    "Extend your right arm forward with palm facing out.",
                    "Gently pull fingers back with your left hand for 15 seconds.",
                    "Rotate both wrists in clockwise and counter-clockwise circles."
                )
            )
        )
    }

    private fun defaultLogs(): List<MovementSessionLog> {
        val defaults = listOf(
            MovementSessionLog(routineTitle = "Neck & Shoulder Release", dateString = "Yesterday"),
            MovementSessionLog(routineTitle = "Desk Spinal Decompression", dateString = "Today")
        )
        saveLogsToPrefs(defaults)
        return defaults
    }

    private fun saveLogsToPrefs(list: List<MovementSessionLog>) {
        val sorted = list.sortedByDescending { it.completedAt }
        _logsState.value = sorted
        prefs.edit().putString(KEY_LOGS, gson.toJson(sorted)).apply()
    }

    fun updatePreferences(interval: Int, reminders: Boolean, quietHours: String, reducedMotion: Boolean) {
        _intervalMinutesState.value = interval
        _remindersEnabledState.value = reminders
        _quietHoursState.value = quietHours
        _reducedMotionState.value = reducedMotion

        prefs.edit()
            .putInt(KEY_INTERVAL, interval)
            .putBoolean(KEY_REMINDERS, reminders)
            .putString(KEY_QUIET, quietHours)
            .putBoolean(KEY_REDUCED_MOTION, reducedMotion)
            .apply()
    }

    fun logCompletedSession(routineTitle: String) {
        val log = MovementSessionLog(routineTitle = routineTitle)
        val current = _logsState.value.toMutableList()
        current.add(0, log)
        saveLogsToPrefs(current)
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_movement_coach_prefs"
        private const val KEY_INTERVAL = "key_break_interval"
        private const val KEY_REMINDERS = "key_reminders_enabled"
        private const val KEY_QUIET = "key_quiet_hours"
        private const val KEY_REDUCED_MOTION = "key_reduced_motion"
        private const val KEY_LOGS = "key_movement_logs"

        @Volatile
        private var INSTANCE: MovementCoachRepository? = null

        fun getInstance(context: Context): MovementCoachRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MovementCoachRepository(context).also { INSTANCE = it }
            }
        }
    }
}
