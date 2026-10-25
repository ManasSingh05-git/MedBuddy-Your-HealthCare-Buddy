package com.manas.medbuddy.refill

import com.manas.medbuddy.model.Medicine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RefillCalculator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun calculateRemainingDays(currentStock: Int, dailyConsumption: Double): Int {
        if (currentStock <= 0 || dailyConsumption <= 0.0) return 0
        return (currentStock / dailyConsumption).toInt()
    }

    fun calculateRunOutDate(currentStock: Int, dailyConsumption: Double, fromDate: Date = Date()): String {
        val days = calculateRemainingDays(currentStock, dailyConsumption)
        val cal = Calendar.getInstance().apply {
            time = fromDate
            add(Calendar.DAY_OF_YEAR, days)
        }
        return dateFormat.format(cal.time)
    }

    fun calculateRefillAlertDate(currentStock: Int, dailyConsumption: Double, thresholdDays: Int, fromDate: Date = Date()): String {
        val days = (calculateRemainingDays(currentStock, dailyConsumption) - thresholdDays).coerceAtLeast(0)
        val cal = Calendar.getInstance().apply {
            time = fromDate
            add(Calendar.DAY_OF_YEAR, days)
        }
        return dateFormat.format(cal.time)
    }

    fun evaluateStockStatus(currentStock: Int, dailyConsumption: Double, thresholdDays: Int): RefillStockStatus {
        if (currentStock <= 0) return RefillStockStatus.OUT_OF_STOCK
        val remainingDays = calculateRemainingDays(currentStock, dailyConsumption)
        return if (remainingDays <= thresholdDays) {
            RefillStockStatus.LOW_STOCK
        } else {
            RefillStockStatus.SUFFICIENT
        }
    }

    fun estimateDefaultDailyConsumption(medicine: Medicine): Double {
        if (medicine.reminderTimes.isNotEmpty()) {
            return medicine.reminderTimes.size.toDouble().coerceAtLeast(1.0)
        }
        val lowerTime = medicine.time.lowercase(Locale.getDefault())
        return when {
            lowerTime.contains("thrice") || lowerTime.contains("3 times") || lowerTime.contains("tds") -> 3.0
            lowerTime.contains("twice") || lowerTime.contains("2 times") || lowerTime.contains("bd") -> 2.0
            else -> 1.0
        }
    }

    fun updateRefillConfig(
        config: MedicineRefillConfig,
        newStock: Int,
        newDailyConsumption: Double? = null,
        newThresholdDays: Int? = null,
        fromDate: Date = Date()
    ): MedicineRefillConfig {
        val consumption = (newDailyConsumption ?: config.dailyDoseConsumption).coerceAtLeast(0.1)
        val threshold = newThresholdDays ?: config.lowStockThresholdDays

        val runOutDate = calculateRunOutDate(newStock, consumption, fromDate)
        val alertDate = calculateRefillAlertDate(newStock, consumption, threshold, fromDate)
        val status = evaluateStockStatus(newStock, consumption, threshold)

        return config.copy(
            currentStockRemaining = newStock.coerceAtLeast(0),
            dailyDoseConsumption = consumption,
            lowStockThresholdDays = threshold,
            estimatedRunOutDate = runOutDate,
            estimatedRefillAlertDate = alertDate,
            stockStatus = status.name,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }
}
