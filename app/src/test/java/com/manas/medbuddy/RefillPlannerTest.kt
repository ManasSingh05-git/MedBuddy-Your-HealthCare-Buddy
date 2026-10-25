package com.manas.medbuddy

import com.manas.medbuddy.refill.MedicineRefillConfig
import com.manas.medbuddy.refill.RefillCalculator
import com.manas.medbuddy.refill.RefillStockStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RefillPlannerTest {

    private lateinit var calculator: RefillCalculator
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Before
    fun setUp() {
        calculator = RefillCalculator()
    }

    @Test
    fun testRemainingDaysAndRunOutDateCalculation() {
        val currentStock = 30
        val dailyConsumption = 2.0 // 15 days remaining

        val remainingDays = calculator.calculateRemainingDays(currentStock, dailyConsumption)
        assertEquals(15, remainingDays)

        val startDate = dateFormat.parse("2026-10-01") ?: Date()
        val runOutDate = calculator.calculateRunOutDate(currentStock, dailyConsumption, startDate)
        assertEquals("2026-10-16", runOutDate)
    }

    @Test
    fun testAlertDateCalculationAndStockStatus() {
        val currentStock = 6 // 3 days remaining at 2/day
        val dailyConsumption = 2.0
        val thresholdDays = 3

        val status = calculator.evaluateStockStatus(currentStock, dailyConsumption, thresholdDays)
        assertEquals(RefillStockStatus.LOW_STOCK, status)

        val outOfStockStatus = calculator.evaluateStockStatus(0, dailyConsumption, thresholdDays)
        assertEquals(RefillStockStatus.OUT_OF_STOCK, outOfStockStatus)
    }

    @Test
    fun testRestockAndDoseDeduction() {
        val initialConfig = MedicineRefillConfig(
            medicineName = "Amoxicillin",
            totalPackageQuantity = 30,
            currentStockRemaining = 10,
            dailyDoseConsumption = 2.0,
            lowStockThresholdDays = 3
        )

        val startDate = dateFormat.parse("2026-10-01") ?: Date()
        val updatedConfig = calculator.updateRefillConfig(initialConfig, newStock = 40, fromDate = startDate)

        assertEquals(40, updatedConfig.currentStockRemaining)
        assertEquals("2026-10-21", updatedConfig.estimatedRunOutDate)
        assertEquals(RefillStockStatus.SUFFICIENT.name, updatedConfig.stockStatus)
    }
}
