package com.manas.medbuddy.refill

import java.util.UUID

enum class RefillStockStatus {
    SUFFICIENT,     // Stock is good (> threshold)
    LOW_STOCK,      // Running low (<= threshold)
    OUT_OF_STOCK    // Stock is 0
}

data class MedicineRefillConfig(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val medicineId: String = "",
    val medicineName: String = "Medicine",
    val dosageInfo: String = "1 Tablet",
    val totalPackageQuantity: Int = 30,
    val currentStockRemaining: Int = 30,
    val dailyDoseConsumption: Double = 2.0,
    val lowStockThresholdDays: Int = 3,
    val lastRefillDate: String = "",
    val estimatedRunOutDate: String = "",
    val estimatedRefillAlertDate: String = "",
    val stockStatus: String = RefillStockStatus.SUFFICIENT.name,
    val reminderEnabled: Boolean = true,
    val pharmacyNotes: String = "",
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
