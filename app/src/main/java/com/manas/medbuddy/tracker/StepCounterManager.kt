package com.manas.medbuddy.tracker

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StepData(
    val stepsToday: Int = 5420,
    val goalSteps: Int = 10000,
    val distanceKm: Float = 3.2f,
    val caloriesKcal: Int = 320,
    val activeMinutes: Int = 45
) {
    fun getFormattedProgress(): Float = (stepsToday.toFloat() / goalSteps.toFloat()).coerceIn(0f, 1f)
}

class StepCounterManager(context: Context) : SensorEventListener {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val sensorManager: SensorManager? = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _stepDataState = MutableStateFlow(loadStepData())
    val stepDataFlow: StateFlow<StepData> = _stepDataState.asStateFlow()

    init {
        checkDailyReset()
        registerSensorListener()
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun checkDailyReset() {
        val todayStr = getCurrentDateString()
        val lastDateStr = prefs.getString(KEY_LAST_DATE, "")

        if (todayStr != lastDateStr) {
            // New day detected -> Midnight Auto-Reset!
            val currentSensorSteps = prefs.getInt(KEY_HARDWARE_SENSOR_STEPS, 0)
            prefs.edit()
                .putString(KEY_LAST_DATE, todayStr)
                .putInt(KEY_MIDNIGHT_BASELINE, currentSensorSteps)
                .putInt(KEY_STEPS_TODAY, 0)
                .apply()

            _stepDataState.value = calculateMetrics(0)
        }
    }

    fun registerSensorListener() {
        checkDailyReset()
        stepSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun unregisterSensorListener() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return

        checkDailyReset()

        val rawHardwareSteps = event.values[0].toInt()
        prefs.edit().putInt(KEY_HARDWARE_SENSOR_STEPS, rawHardwareSteps).apply()

        var midnightBaseline = prefs.getInt(KEY_MIDNIGHT_BASELINE, -1)
        if (midnightBaseline == -1 || midnightBaseline > rawHardwareSteps) {
            // First time or sensor rebooted
            midnightBaseline = rawHardwareSteps
            prefs.edit().putInt(KEY_MIDNIGHT_BASELINE, midnightBaseline).apply()
        }

        val todaySteps = (rawHardwareSteps - midnightBaseline).coerceAtLeast(0)
        prefs.edit().putInt(KEY_STEPS_TODAY, todaySteps).apply()

        _stepDataState.value = calculateMetrics(todaySteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun addManualSteps(stepIncrement: Int) {
        checkDailyReset()
        val currentSteps = _stepDataState.value.stepsToday + stepIncrement
        prefs.edit().putInt(KEY_STEPS_TODAY, currentSteps).apply()
        _stepDataState.value = calculateMetrics(currentSteps)
    }

    private fun loadStepData(): StepData {
        checkDailyReset()
        val savedSteps = prefs.getInt(KEY_STEPS_TODAY, 5420)
        return calculateMetrics(savedSteps)
    }

    private fun calculateMetrics(steps: Int): StepData {
        val distance = steps * 0.00075f // ~0.75m per step
        val calories = (steps * 0.04f).toInt() // ~0.04 kcal per step
        val minutes = (steps / 100).coerceAtLeast(0) // ~100 steps per active minute

        return StepData(
            stepsToday = steps,
            goalSteps = 10000,
            distanceKm = distance,
            caloriesKcal = calories,
            activeMinutes = minutes
        )
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_step_counter_prefs"
        private const val KEY_LAST_DATE = "key_last_step_date"
        private const val KEY_MIDNIGHT_BASELINE = "key_midnight_step_baseline"
        private const val KEY_HARDWARE_SENSOR_STEPS = "key_hardware_sensor_steps"
        private const val KEY_STEPS_TODAY = "key_steps_today"

        @Volatile
        private var INSTANCE: StepCounterManager? = null

        fun getInstance(context: Context): StepCounterManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StepCounterManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
