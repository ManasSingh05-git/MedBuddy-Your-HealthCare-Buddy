package com.manas.medbuddy.expenses

import java.util.UUID

enum class ExpenseCategory {
    ALL,
    MEDICINE,
    CONSULTATION,
    LAB_TEST,
    INSURANCE_PREMIUM,
    PROCEDURE,
    OTHER
}

data class HealthcareExpense(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val currency: String = "₹",
    val category: String = ExpenseCategory.MEDICINE.name,
    val date: String = "",
    val paymentMethod: String = "UPI / Online",
    val vendorName: String = "",
    val localReceiptPath: String = "",
    val hasReceipt: Boolean = false,
    val notes: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

data class CategoryBreakdownItem(
    val category: ExpenseCategory,
    val totalAmount: Double,
    val percentage: Float
)
