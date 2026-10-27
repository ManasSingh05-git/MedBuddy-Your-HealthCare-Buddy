package com.manas.medbuddy.tracker

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WaterData(
    val currentLiters: Float = 1.8f,
    val goalLiters: Float = 3.0f
) {
    fun getProgressFraction(): Float = (currentLiters / goalLiters).coerceIn(0f, 1f)
    fun getProgressPercentage(): Int = (getProgressFraction() * 100).toInt()
}

class WaterRepository(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _waterDataState = MutableStateFlow(loadWaterData())
    val waterDataFlow: StateFlow<WaterData> = _waterDataState.asStateFlow()

    init {
        checkDailyReset()
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun checkDailyReset() {
        val todayStr = getCurrentDateString()
        val lastDateStr = prefs.getString(KEY_LAST_WATER_DATE, "")

        if (todayStr != lastDateStr) {
            // New day detected -> Reset Water Intake to 0.0 L!
            prefs.edit()
                .putString(KEY_LAST_WATER_DATE, todayStr)
                .putFloat(KEY_CURRENT_WATER_LITERS, 0.0f)
                .apply()

            _waterDataState.value = WaterData(currentLiters = 0.0f, goalLiters = 3.0f)
        }
    }

    fun addWater(incrementLiters: Float) {
        checkDailyReset()
        val currentLiters = prefs.getFloat(KEY_CURRENT_WATER_LITERS, 0.0f)
        val newLiters = (currentLiters + incrementLiters).coerceAtMost(6.0f)

        prefs.edit().putFloat(KEY_CURRENT_WATER_LITERS, newLiters).apply()
        _waterDataState.value = WaterData(currentLiters = newLiters, goalLiters = 3.0f)
    }

    private fun loadWaterData(): WaterData {
        checkDailyReset()
        val currentLiters = prefs.getFloat(KEY_CURRENT_WATER_LITERS, 1.8f)
        return WaterData(currentLiters = currentLiters, goalLiters = 3.0f)
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_water_tracker_prefs"
        private const val KEY_LAST_WATER_DATE = "key_last_water_date"
        private const val KEY_CURRENT_WATER_LITERS = "key_current_water_liters"

        @Volatile
        private var INSTANCE: WaterRepository? = null

        fun getInstance(context: Context): WaterRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WaterRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
