package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.WellnessGoal
import com.manas.medbuddy.data.model.WellnessSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WellnessCoachRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _goalsState = MutableStateFlow<List<WellnessGoal>>(emptyList())
    val goalsFlow: StateFlow<List<WellnessGoal>> = _goalsState.asStateFlow()

    private val _suggestionsState = MutableStateFlow<List<WellnessSuggestion>>(emptyList())
    val suggestionsFlow: StateFlow<List<WellnessSuggestion>> = _suggestionsState.asStateFlow()

    init {
        loadData()
        syncTrackerProgress()
    }

    private fun loadData() {
        val goalsJson = prefs.getString(KEY_GOALS, null)
        val goalsList: List<WellnessGoal> = if (!goalsJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<WellnessGoal>>() {}.type
                gson.fromJson(goalsJson, type) ?: defaultGoals()
            } catch (_: Exception) {
                defaultGoals()
            }
        } else {
            defaultGoals()
        }
        _goalsState.value = goalsList

        val suggJson = prefs.getString(KEY_SUGGESTIONS, null)
        val suggList: List<WellnessSuggestion> = if (!suggJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<WellnessSuggestion>>() {}.type
                gson.fromJson(suggJson, type) ?: defaultSuggestions(false)
            } catch (_: Exception) {
                defaultSuggestions(false)
            }
        } else {
            defaultSuggestions(false)
        }
        _suggestionsState.value = suggList
    }

    private fun defaultGoals(): List<WellnessGoal> {
        val defaults = listOf(
            WellnessGoal(title = "Daily Water Intake", targetValue = 2.5f, currentProgress = 1.8f, unit = "L"),
            WellnessGoal(title = "Nightly Sleep", targetValue = 8.0f, currentProgress = 7.2f, unit = "hours"),
            WellnessGoal(title = "Daily Step Count", targetValue = 10000f, currentProgress = 5420f, unit = "steps")
        )
        saveGoalsToPrefs(defaults)
        return defaults
    }

    private fun defaultSuggestions(isFallback: Boolean): List<WellnessSuggestion> {
        return if (isFallback) {
            listOf(
                WellnessSuggestion(
                    title = "Fallback: Consistent Hydration Routine",
                    description = "Keep a water bottle at your desk to sip water steadily throughout the day.",
                    rationale = "Deterministic Fallback: Generated locally due to temporary AI service unavailability."
                ),
                WellnessSuggestion(
                    title = "Fallback: Wind Down Before Bed",
                    description = "Turn off screens 30 minutes before sleep to improve rest quality.",
                    rationale = "Deterministic Fallback: Based on standard wellness best practices."
                )
            )
        } else {
            listOf(
                WellnessSuggestion(
                    title = "Increase Evening Hydration Pace",
                    description = "Try to finish 80% of your water goal by 6:00 PM to avoid nighttime disruption.",
                    rationale = "Why this suggestion: Your recent hydration tracking shows water intake often clusters late in the evening."
                ),
                WellnessSuggestion(
                    title = "Maintain Consistent Sleep Schedule",
                    description = "Try going to bed within the same 30-minute window each night.",
                    rationale = "Why this suggestion: Your Sleep Tracker logs show varying sleep onset times over the past 7 days."
                )
            )
        }
    }

    private fun saveGoalsToPrefs(list: List<WellnessGoal>) {
        _goalsState.value = list
        prefs.edit().putString(KEY_GOALS, gson.toJson(list)).apply()
    }

    private fun saveSuggestionsToPrefs(list: List<WellnessSuggestion>) {
        _suggestionsState.value = list
        prefs.edit().putString(KEY_SUGGESTIONS, gson.toJson(list)).apply()
    }

    fun syncTrackerProgress() {
        // Pull actual sleep progress from SleepRepository
        try {
            val sleepRepo = SleepRepository.getInstance(context)
            val latestSleep = sleepRepo.getLatestSleepRecord()
            if (latestSleep != null) {
                val hours = latestSleep.durationHoursFloat
                val current = _goalsState.value.toMutableList()
                val idx = current.indexOfFirst { it.title.contains("Sleep", true) }
                if (idx >= 0) {
                    current[idx] = current[idx].copy(currentProgress = hours)
                    saveGoalsToPrefs(current)
                }
            }
        } catch (_: Exception) { }
    }

    fun addGoal(goal: WellnessGoal) {
        val current = _goalsState.value.toMutableList()
        current.add(goal)
        saveGoalsToPrefs(current)
    }

    fun updateGoal(goal: WellnessGoal) {
        val current = _goalsState.value.toMutableList()
        val index = current.indexOfFirst { it.id == goal.id }
        if (index >= 0) {
            current[index] = goal
            saveGoalsToPrefs(current)
        }
    }

    fun deleteGoal(id: String) {
        val filtered = _goalsState.value.filterNot { it.id == id }
        saveGoalsToPrefs(filtered)
    }

    fun toggleSuggestionCompletion(id: String) {
        val current = _suggestionsState.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        saveSuggestionsToPrefs(current)
    }

    fun generateWeeklySuggestions(simulateFailure: Boolean) {
        val suggs = defaultSuggestions(simulateFailure)
        saveSuggestionsToPrefs(suggs)
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_wellness_prefs"
        private const val KEY_GOALS = "key_wellness_goals"
        private const val KEY_SUGGESTIONS = "key_wellness_suggestions"

        @Volatile
        private var INSTANCE: WellnessCoachRepository? = null

        fun getInstance(context: Context): WellnessCoachRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WellnessCoachRepository(context).also { INSTANCE = it }
            }
        }
    }
}
