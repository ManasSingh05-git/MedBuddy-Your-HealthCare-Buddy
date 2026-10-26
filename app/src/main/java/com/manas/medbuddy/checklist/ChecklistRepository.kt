package com.manas.medbuddy.checklist

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class ChecklistRepository(private val context: Context? = null) {

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

    fun getChecklistReference(): DatabaseReference? = database?.reference?.child("hospitalChecklists")?.child(currentUserId)

    fun listenToChecklists(
        onUpdate: (List<HospitalVisitChecklist>) -> Unit,
        onError: (String) -> Unit = {}
    ): ValueEventListener? {
        val ref = getChecklistReference() ?: run {
            onError("Database service unavailable")
            return null
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val item = child.getValue(HospitalVisitChecklist::class.java)
                    item?.copy(id = child.key ?: item.id)
                }.sortedByDescending { it.lastUpdatedTimestamp }
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
            getChecklistReference()?.removeEventListener(listener)
        }
    }

    fun calculateProgress(items: List<ChecklistItem>): Float {
        if (items.isEmpty()) return 0f
        val completed = items.count { it.isCompleted }
        return (completed.toFloat() / items.size.toFloat()) * 100f
    }

    fun saveChecklist(
        checklist: HospitalVisitChecklist,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val checklistId = if (checklist.id.isBlank()) UUID.randomUUID().toString() else checklist.id
        val progress = calculateProgress(checklist.items)
        val updated = checklist.copy(
            id = checklistId,
            userId = currentUserId,
            completionPercentage = progress,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )

        val ref = getChecklistReference()
        if (ref != null) {
            ref.child(checklistId).setValue(updated)
                .addOnSuccessListener { onComplete(true, checklistId) }
                .addOnFailureListener { err -> onComplete(false, err.message) }
        } else {
            onComplete(true, checklistId)
        }
    }

    fun toggleItemCompletion(
        checklist: HospitalVisitChecklist,
        itemId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val updatedItems = checklist.items.map { item ->
            if (item.id == itemId) item.copy(isCompleted = !item.isCompleted) else item
        }
        val updatedChecklist = checklist.copy(items = updatedItems)
        saveChecklist(updatedChecklist, onComplete)
    }

    fun addItemToChecklist(
        checklist: HospitalVisitChecklist,
        newItem: ChecklistItem,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val updatedItems = checklist.items + newItem
        val updatedChecklist = checklist.copy(items = updatedItems)
        saveChecklist(updatedChecklist, onComplete)
    }

    fun removeItemFromChecklist(
        checklist: HospitalVisitChecklist,
        itemId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val updatedItems = checklist.items.filterNot { it.id == itemId }
        val updatedChecklist = checklist.copy(items = updatedItems)
        saveChecklist(updatedChecklist, onComplete)
    }

    fun deleteChecklist(checklistId: String, onComplete: (Boolean, String?) -> Unit) {
        val ref = getChecklistReference() ?: run {
            onComplete(false, "Database service unavailable")
            return
        }

        ref.child(checklistId).removeValue()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { err -> onComplete(false, err.message) }
    }
}
