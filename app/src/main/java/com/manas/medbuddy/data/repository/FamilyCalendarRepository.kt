package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.CalendarEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FamilyCalendarRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _eventsState = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val eventsFlow: StateFlow<List<CalendarEvent>> = _eventsState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val json = prefs.getString(KEY_EVENTS, null)
        val list: List<CalendarEvent> = if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<CalendarEvent>>() {}.type
                gson.fromJson(json, type) ?: defaultEvents()
            } catch (_: Exception) {
                defaultEvents()
            }
        } else {
            defaultEvents()
        }
        _eventsState.value = list.sortedBy { it.timestamp }
    }

    private fun defaultEvents(): List<CalendarEvent> {
        val defaults = listOf(
            CalendarEvent(
                title = "Dr. Sharma - General Checkup",
                category = "Appointment",
                memberName = "Manas Singh",
                dateTimeString = "Tomorrow, 10:00 AM",
                timestamp = System.currentTimeMillis() + 86400000L,
                isPrivate = false
            ),
            CalendarEvent(
                title = "Polio Booster Vaccination",
                category = "Vaccination",
                memberName = "Aarav Singh",
                dateTimeString = "Oct 30, 2025 - 11:30 AM",
                timestamp = System.currentTimeMillis() + 432000000L,
                isPrivate = false
            ),
            CalendarEvent(
                title = "Daily Vitamin D3 Supplement",
                category = "Medicine",
                memberName = "Manas Singh",
                dateTimeString = "Daily, 09:00 AM",
                timestamp = System.currentTimeMillis() + 3600000L,
                isPrivate = false
            )
        )
        saveToPrefs(defaults)
        return defaults
    }

    private fun saveToPrefs(list: List<CalendarEvent>) {
        val sorted = list.sortedBy { it.timestamp }
        _eventsState.value = sorted
        prefs.edit().putString(KEY_EVENTS, gson.toJson(sorted)).apply()
    }

    fun addEvent(event: CalendarEvent): String? {
        // Duplicate prevention check: check if event with same title and timestamp already exists
        val isDuplicate = _eventsState.value.any {
            it.title.equals(event.title, true) && kotlin.math.abs(it.timestamp - event.timestamp) < 60000L
        }
        if (isDuplicate) {
            return "Duplicate event detected: A similar event at this time already exists."
        }

        val current = _eventsState.value.toMutableList()
        current.add(event)
        saveToPrefs(current)
        return null
    }

    fun deleteEvent(id: String) {
        val filtered = _eventsState.value.filterNot { it.id == id }
        saveToPrefs(filtered)
    }

    fun getFilteredEvents(categoryFilter: String, memberFilter: String, includePrivate: Boolean): List<CalendarEvent> {
        return _eventsState.value.filter { event ->
            val matchesCategory = categoryFilter == "All" || event.category.equals(categoryFilter, true)
            val matchesMember = memberFilter == "All" || event.memberName.equals(memberFilter, true)
            val matchesPrivacy = includePrivate || !event.isPrivate
            matchesCategory && matchesMember && matchesPrivacy
        }
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_family_calendar_prefs"
        private const val KEY_EVENTS = "key_calendar_events"

        @Volatile
        private var INSTANCE: FamilyCalendarRepository? = null

        fun getInstance(context: Context): FamilyCalendarRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FamilyCalendarRepository(context).also { INSTANCE = it }
            }
        }
    }
}
