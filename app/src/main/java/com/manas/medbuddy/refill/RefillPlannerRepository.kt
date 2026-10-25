package com.manas.medbuddy.refill

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RefillPlannerRepository(private val context: Context? = null) {

    private val calculator = RefillCalculator()

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

    fun getRefillReference(): DatabaseReference? = database?.reference?.child("refillPlanner")?.child(currentUserId)

    fun listenToRefillConfigs(
        onUpdate: (List<MedicineRefillConfig>) -> Unit,
        onError: (String) -> Unit = {}
    ): ValueEventListener? {
        val ref = getRefillReference() ?: run {
            onError("Database service unavailable")
            return null
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val item = child.getValue(MedicineRefillConfig::class.java)
                    item?.copy(id = child.key ?: item.id)
                }
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
            getRefillReference()?.removeEventListener(listener)
        }
    }

    fun saveRefillConfig(config: MedicineRefillConfig, onComplete: (Boolean, String?) -> Unit) {
        val ref = getRefillReference() ?: run {
            onComplete(false, "Database service unavailable")
            return
        }

        val configId = if (config.id.isBlank()) UUID.randomUUID().toString() else config.id
        val updated = calculator.updateRefillConfig(
            config = config.copy(id = configId, userId = currentUserId),
            newStock = config.currentStockRemaining
        )

        ref.child(configId).setValue(updated)
            .addOnSuccessListener { onComplete(true, configId) }
            .addOnFailureListener { err -> onComplete(false, err.message) }
    }

    fun restockPackage(config: MedicineRefillConfig, additionalStock: Int, onComplete: (Boolean, String?) -> Unit) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val newStock = config.currentStockRemaining + additionalStock
        val updated = calculator.updateRefillConfig(
            config = config.copy(lastRefillDate = todayStr),
            newStock = newStock
        )
        saveRefillConfig(updated, onComplete)
    }

    fun deductStockForDose(config: MedicineRefillConfig, dosesTaken: Double = 1.0, onComplete: (Boolean, String?) -> Unit) {
        val newStock = (config.currentStockRemaining - dosesTaken.toInt()).coerceAtLeast(0)
        val updated = calculator.updateRefillConfig(
            config = config,
            newStock = newStock
        )
        saveRefillConfig(updated, onComplete)
    }

    fun deleteRefillConfig(configId: String, onComplete: (Boolean, String?) -> Unit) {
        val ref = getRefillReference() ?: run {
            onComplete(false, "Database service unavailable")
            return
        }

        ref.child(configId).removeValue()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { err -> onComplete(false, err.message) }
    }
}
