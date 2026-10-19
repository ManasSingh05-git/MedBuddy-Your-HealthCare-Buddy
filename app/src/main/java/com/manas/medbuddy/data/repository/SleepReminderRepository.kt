package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.manas.medbuddy.data.model.SleepReminderConfig
import com.manas.medbuddy.util.SleepReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SleepReminderRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _configState = MutableStateFlow(SleepReminderConfig())
    val configFlow: StateFlow<SleepReminderConfig> = _configState.asStateFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val json = prefs.getString(KEY_CONFIG, null)
        val config = if (!json.isNullOrEmpty()) {
            try {
                gson.fromJson(json, SleepReminderConfig::class.java) ?: SleepReminderConfig()
            } catch (_: Exception) {
                SleepReminderConfig()
            }
        } else {
            SleepReminderConfig()
        }
        _configState.value = config
    }

    fun getConfig(): SleepReminderConfig {
        return _configState.value
    }

    fun saveConfig(config: SleepReminderConfig) {
        _configState.value = config
        val json = gson.toJson(config)
        prefs.edit().putString(KEY_CONFIG, json).apply()

        // Reschedule alarms immediately
        SleepReminderScheduler.scheduleSleepReminder(appContext, config)
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_sleep_reminder_prefs"
        private const val KEY_CONFIG = "key_sleep_reminder_config"

        @Volatile
        private var INSTANCE: SleepReminderRepository? = null

        fun getInstance(context: Context): SleepReminderRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SleepReminderRepository(context).also { INSTANCE = it }
            }
        }
    }
}
