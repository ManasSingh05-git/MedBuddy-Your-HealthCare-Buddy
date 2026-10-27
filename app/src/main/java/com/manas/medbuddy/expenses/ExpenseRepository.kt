package com.manas.medbuddy.expenses

import android.content.Context
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ExpenseRepository(private val context: Context? = null) {

    private val database: FirebaseDatabase? by lazy {
        try {
            if (context != null && FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseDatabase.getInstance().apply {
                try {
                    setPersistenceEnabled(true)
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            null
        }
    }

    val currentUserId: String
        get() = try {
            FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        } catch (_: Exception) {
            "guest_user"
        }

    private val userPrivateStorageDir: File?
        get() {
            if (context == null) return null
            val dir = File(context.filesDir, "expenses/$currentUserId")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun getExpensesReference(): DatabaseReference? = database?.reference?.child("healthExpenses")?.child(currentUserId)

    fun listenToExpenses(
        onUpdate: (List<HealthcareExpense>) -> Unit,
        onError: (String) -> Unit = {}
    ): ValueEventListener? {
        val ref = getExpensesReference() ?: run {
            onError("Database service unavailable")
            return null
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val item = child.getValue(HealthcareExpense::class.java)
                    item?.copy(id = child.key ?: item.id)
                }.sortedByDescending { it.date }
                onUpdate(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        ref.addValueEventListener(listener)
        return listener
    }

    fun removeListener(listener: ValueEventListener?) {
        if (listener != null) {
            getExpensesReference()?.removeEventListener(listener)
        }
    }

    fun saveExpense(
        expense: HealthcareExpense,
        sourceUri: Uri? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        var receiptPath = expense.localReceiptPath
        var hasRcpt = expense.hasReceipt

        if (context != null && sourceUri != null) {
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(sourceUri) ?: ""
                val isPdf = mimeType.contains("pdf", ignoreCase = true) || sourceUri.toString().endsWith(".pdf", ignoreCase = true)
                val ext = if (isPdf) ".pdf" else ".jpg"

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val targetFile = File(userPrivateStorageDir, "receipt_$timestamp$ext")

                val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
                val outputStream = FileOutputStream(targetFile)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                receiptPath = targetFile.absolutePath
                hasRcpt = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val expenseId = if (expense.id.isBlank()) UUID.randomUUID().toString() else expense.id
        val updatedExpense = expense.copy(
            id = expenseId,
            userId = currentUserId,
            localReceiptPath = receiptPath,
            hasReceipt = hasRcpt
        )

        val ref = getExpensesReference()
        if (ref != null) {
            ref.child(expenseId).setValue(updatedExpense)
                .addOnSuccessListener { onComplete(true, expenseId) }
                .addOnFailureListener { err -> onComplete(false, err.message) }
        } else {
            onComplete(true, expenseId)
        }
    }

    fun deleteExpense(expense: HealthcareExpense, onComplete: (Boolean, String?) -> Unit) {
        try {
            if (expense.localReceiptPath.isNotBlank()) {
                val file = File(expense.localReceiptPath)
                if (file.exists()) file.delete()
            }

            val ref = getExpensesReference()
            if (ref != null) {
                ref.child(expense.id).removeValue()
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { err -> onComplete(false, err.message) }
            } else {
                onComplete(true, null)
            }
        } catch (e: Exception) {
            onComplete(false, e.message)
        }
    }

    fun calculateTotalSpend(expenses: List<HealthcareExpense>): Double {
        return expenses.sumOf { it.amount }
    }

    fun calculateCategoryBreakdown(expenses: List<HealthcareExpense>): List<CategoryBreakdownItem> {
        val total = calculateTotalSpend(expenses)
        if (total <= 0.0) return emptyList()

        return ExpenseCategory.values()
            .filter { it != ExpenseCategory.ALL }
            .map { cat ->
                val categoryTotal = expenses.filter { it.category == cat.name }.sumOf { it.amount }
                val percentage = ((categoryTotal / total) * 100.0).toFloat()
                CategoryBreakdownItem(
                    category = cat,
                    totalAmount = categoryTotal,
                    percentage = percentage
                )
            }
            .filter { it.totalAmount > 0.0 }
            .sortedByDescending { it.totalAmount }
    }

    fun filterExpensesByMonth(expenses: List<HealthcareExpense>, monthYearPrefix: String): List<HealthcareExpense> {
        if (monthYearPrefix.isBlank() || monthYearPrefix.contains("ALL", ignoreCase = true)) return expenses
        return expenses.filter { it.date.startsWith(monthYearPrefix) }
    }

    fun generateCsvExportText(expenses: List<HealthcareExpense>, rangeLabel: String): String {
        val sb = StringBuilder()
        sb.append("MedBuddy Healthcare Expense Report - $rangeLabel\n")
        sb.append("Date,Title,Category,Amount,Currency,Payment Method,Vendor,Notes\n")

        expenses.forEach { exp ->
            val cleanTitle = exp.title.replace(",", " ")
            val cleanVendor = exp.vendorName.replace(",", " ")
            val cleanNotes = exp.notes.replace(",", " ")
            sb.append("${exp.date},$cleanTitle,${exp.category},${exp.amount},${exp.currency},${exp.paymentMethod},$cleanVendor,$cleanNotes\n")
        }

        val total = calculateTotalSpend(expenses)
        val symbol = expenses.firstOrNull()?.currency ?: "₹"
        sb.append("\nTOTAL SPEND:,$symbol $total\n")

        return sb.toString()
    }
}
