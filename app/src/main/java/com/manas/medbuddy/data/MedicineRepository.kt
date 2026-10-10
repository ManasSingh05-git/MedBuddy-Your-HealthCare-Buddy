package com.manas.medbuddy.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.model.DoseLog
import com.manas.medbuddy.model.Medicine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicineRepository(private val context: Context? = null) {

    private val database: FirebaseDatabase? by lazy {
        try {
            if (context != null && FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseDatabase.getInstance().apply {
                try {
                    setPersistenceEnabled(true)
                } catch (_: Exception) {
                    // Ignore if persistence was already enabled
                }
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

    fun getMedicinesReference(): DatabaseReference? = database?.reference?.child("medicines")?.child(currentUserId)

    fun getDoseLogsReference(): DatabaseReference? = database?.reference?.child("doseLogs")?.child(currentUserId)

    fun listenToMedicines(onUpdate: (List<Medicine>) -> Unit, onError: (String) -> Unit = {}): ValueEventListener? {
        val ref = getMedicinesReference() ?: run {
            onError("Firebase database unavailable")
            return null
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val med = child.getValue(Medicine::class.java)
                    med?.copy(id = child.key ?: med.id)
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

    fun removeMedicinesListener(listener: ValueEventListener?) {
        if (listener != null) {
            getMedicinesReference()?.removeEventListener(listener)
        }
    }

    fun saveMedicine(medicine: Medicine, onComplete: (Boolean, String?) -> Unit) {
        val ref = getMedicinesReference() ?: run {
            onComplete(false, "Firebase database unavailable")
            return
        }
        val medId = if (medicine.id.isBlank()) ref.push().key ?: System.currentTimeMillis().toString() else medicine.id
        
        val timeSummary = if (medicine.reminderTimes.isNotEmpty()) {
            medicine.reminderTimes.joinToString(", ") { it.time }
        } else {
            medicine.time
        }

        val updatedMed = medicine.copy(id = medId, time = timeSummary)

        ref.child(medId).setValue(updatedMed)
            .addOnSuccessListener { onComplete(true, medId) }
            .addOnFailureListener { err -> onComplete(false, err.message) }
    }

    fun deleteMedicine(medicineId: String, onComplete: (Boolean, String?) -> Unit) {
        val ref = getMedicinesReference() ?: run {
            onComplete(false, "Firebase database unavailable")
            return
        }
        if (medicineId.isBlank()) {
            onComplete(false, "Invalid medicine ID")
            return
        }
        ref.child(medicineId).removeValue()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { err -> onComplete(false, err.message) }
    }

    fun listenToDoseLogs(onUpdate: (List<DoseLog>) -> Unit, onError: (String) -> Unit = {}): ValueEventListener? {
        val ref = getDoseLogsReference() ?: run {
            onError("Firebase database unavailable")
            return null
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val log = child.getValue(DoseLog::class.java)
                    log?.copy(id = child.key ?: log.id)
                }.sortedByDescending { it.timestamp }
                onUpdate(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }
        ref.addValueEventListener(listener)
        return listener
    }

    fun removeDoseLogsListener(listener: ValueEventListener?) {
        if (listener != null) {
            getDoseLogsReference()?.removeEventListener(listener)
        }
    }

    fun logDoseStatus(
        medicine: Medicine,
        scheduledTime: String,
        status: String,
        dateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val ref = getDoseLogsReference() ?: run {
            onComplete(false, "Firebase database unavailable")
            return
        }
        val logId = "${medicine.id}_${dateString}_${scheduledTime.replace(" ", "_")}"
        
        val doseLog = DoseLog(
            id = logId,
            medicineId = medicine.id,
            medicineName = medicine.name,
            dosage = medicine.dosage,
            scheduledTime = scheduledTime,
            date = dateString,
            status = status,
            timestamp = System.currentTimeMillis()
        )

        ref.child(logId).setValue(doseLog)
            .addOnSuccessListener { onComplete(true, logId) }
            .addOnFailureListener { err -> onComplete(false, err.message) }
    }
}
