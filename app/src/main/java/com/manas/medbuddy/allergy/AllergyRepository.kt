package com.manas.medbuddy.allergy

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale
import java.util.UUID

class AllergyRepository(private val context: Context? = null) {

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

    fun getAllergiesReference(): DatabaseReference? = database?.reference?.child("allergies")?.child(currentUserId)

    fun getProfileStatusReference(): DatabaseReference? = database?.reference?.child("allergyProfileStatus")?.child(currentUserId)

    fun listenToAllergies(
        onUpdate: (records: List<AllergyRecord>, profileStatus: AllergyProfileStatus) -> Unit,
        onError: (String) -> Unit = {}
    ): ValueEventListener? {
        val ref = getAllergiesReference() ?: run {
            onError("Database unavailable")
            return null
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val item = child.getValue(AllergyRecord::class.java)
                    item?.copy(id = child.key ?: item.id)
                }.sortedByDescending { it.createdAtTimestamp }

                getProfileStatusReference()?.get()?.addOnSuccessListener { statusSnap ->
                    val statusStr = statusSnap.getValue(String::class.java)
                    val status = when {
                        list.isNotEmpty() -> AllergyProfileStatus.HAS_ALLERGIES
                        statusStr == AllergyProfileStatus.NO_KNOWN_ALLERGIES.name -> AllergyProfileStatus.NO_KNOWN_ALLERGIES
                        else -> AllergyProfileStatus.UNRECORDED
                    }
                    onUpdate(list, status)
                }?.addOnFailureListener {
                    val status = if (list.isNotEmpty()) AllergyProfileStatus.HAS_ALLERGIES else AllergyProfileStatus.UNRECORDED
                    onUpdate(list, status)
                } ?: run {
                    val status = if (list.isNotEmpty()) AllergyProfileStatus.HAS_ALLERGIES else AllergyProfileStatus.UNRECORDED
                    onUpdate(list, status)
                }
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
            getAllergiesReference()?.removeEventListener(listener)
        }
    }

    fun saveAllergyRecord(record: AllergyRecord, onComplete: (Boolean, String?) -> Unit) {
        val ref = getAllergiesReference() ?: run {
            onComplete(false, "Database service unavailable")
            return
        }

        val recordId = if (record.id.isBlank()) UUID.randomUUID().toString() else record.id
        val updatedRecord = record.copy(id = recordId, userId = currentUserId)

        ref.child(recordId).setValue(updatedRecord)
            .addOnSuccessListener {
                getProfileStatusReference()?.setValue(AllergyProfileStatus.HAS_ALLERGIES.name)
                onComplete(true, recordId)
            }
            .addOnFailureListener { err -> onComplete(false, err.message) }
    }

    fun deleteAllergyRecord(recordId: String, onComplete: (Boolean, String?) -> Unit) {
        val ref = getAllergiesReference() ?: run {
            onComplete(false, "Database service unavailable")
            return
        }

        ref.child(recordId).removeValue()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { err -> onComplete(false, err.message) }
    }

    fun setNoKnownAllergiesStatus(isNkda: Boolean, onComplete: (Boolean, String?) -> Unit) {
        val ref = getProfileStatusReference() ?: run {
            onComplete(false, "Database service unavailable")
            return
        }

        val newStatus = if (isNkda) AllergyProfileStatus.NO_KNOWN_ALLERGIES.name else AllergyProfileStatus.UNRECORDED.name
        ref.setValue(newStatus)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { err -> onComplete(false, err.message) }
    }

    fun checkMedicineAllergyMatch(
        medicineName: String,
        allergies: List<AllergyRecord>
    ): AllergyMatchResult {
        if (medicineName.isBlank() || allergies.isEmpty()) {
            return AllergyMatchResult(hasMatch = false)
        }

        val lowerMed = medicineName.lowercase(Locale.getDefault())

        val matchedAllergy = allergies.find { allergy ->
            val lowerSubstance = allergy.substance.lowercase(Locale.getDefault())
            lowerSubstance.isNotBlank() && (
                    lowerMed.contains(lowerSubstance) ||
                    lowerSubstance.contains(lowerMed) ||
                    (lowerSubstance.contains("penicillin") && lowerMed.contains("amoxicillin")) ||
                    (lowerSubstance.contains("penicillin") && lowerMed.contains("ampicillin"))
            )
        }

        if (matchedAllergy != null) {
            val verificationText = if (matchedAllergy.isVerified) "physician-verified" else "self-reported / unverified"
            val warning = "⚠️ Potential Allergy Match: Patient has a $verificationText allergy to ${matchedAllergy.substance} (${matchedAllergy.severity} reaction: ${matchedAllergy.reportedReaction}). Consult your doctor or pharmacist to verify safety."
            return AllergyMatchResult(
                hasMatch = true,
                matchedSubstance = matchedAllergy.substance,
                allergyRecord = matchedAllergy,
                isVerified = matchedAllergy.isVerified,
                warningMessage = warning
            )
        }

        return AllergyMatchResult(hasMatch = false)
    }
}
