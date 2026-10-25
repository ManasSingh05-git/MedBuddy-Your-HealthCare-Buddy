package com.manas.medbuddy.card

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.allergy.AllergyRecord
import com.manas.medbuddy.data.model.EmergencyContact
import com.manas.medbuddy.model.Medicine
import java.util.Locale

class HealthCardRepository(private val context: Context? = null) {

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

    fun getCardReference(): DatabaseReference? = database?.reference?.child("healthCard")?.child(currentUserId)

    fun listenToHealthCardConfig(
        onUpdate: (HealthCardConfig) -> Unit,
        onError: (String) -> Unit = {}
    ): ValueEventListener? {
        val ref = getCardReference() ?: run {
            onError("Database service unavailable")
            return null
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val config = snapshot.getValue(HealthCardConfig::class.java) ?: HealthCardConfig(userId = currentUserId)
                onUpdate(config.copy(userId = currentUserId))
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
            getCardReference()?.removeEventListener(listener)
        }
    }

    fun saveHealthCardConfig(config: HealthCardConfig, onComplete: (Boolean, String?) -> Unit) {
        val ref = getCardReference() ?: run {
            onComplete(false, "Database service unavailable")
            return
        }

        val updatedConfig = config.copy(
            userId = currentUserId,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )

        ref.setValue(updatedConfig)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { err -> onComplete(false, err.message) }
    }

    fun generateShareableSummaryText(
        config: HealthCardConfig,
        contacts: List<EmergencyContact>,
        medicines: List<Medicine>,
        allergies: List<AllergyRecord>,
        isPrivacyRedacted: Boolean
    ): String {
        val sb = StringBuilder()
        sb.append("💳 EMERGENCY PERSONAL HEALTH CARD\n")
        sb.append("=====================================\n")
        sb.append("Patient: ${config.fullName.ifBlank { "Unspecified" }}\n")

        if (config.showBloodGroup) {
            sb.append("Blood Group: ${config.bloodGroup}\n")
        }

        if (config.showOrganDonor) {
            sb.append("Organ Donor: ${if (config.isOrganDonor) "Yes (Registered)" else "No"}\n")
        }

        if (config.primaryPhysicianName.isNotBlank()) {
            sb.append("Primary Doctor: ${config.primaryPhysicianName} (${config.primaryPhysicianPhone})\n")
        }

        if (config.showAllergies) {
            sb.append("\n⚠️ RECORDED ALLERGIES:\n")
            if (allergies.isEmpty()) {
                sb.append("• No known allergies recorded.\n")
            } else {
                allergies.forEach { allergy ->
                    val verification = if (allergy.isVerified) "Physician-Verified" else "Self-Reported"
                    if (isPrivacyRedacted) {
                        sb.append("• [REDACTED ALLERGY DETAILS - UNLOCK TO VIEW]\n")
                    } else {
                        sb.append("• ${allergy.substance} (${allergy.severity} - $verification): ${allergy.reportedReaction}\n")
                    }
                }
            }
        }

        if (config.showMedicines) {
            sb.append("\n💊 CURRENT ACTIVE MEDICINES:\n")
            if (medicines.isEmpty()) {
                sb.append("• No active medicines scheduled.\n")
            } else {
                medicines.forEach { med ->
                    if (isPrivacyRedacted) {
                        sb.append("• [REDACTED MEDICINE NAME - UNLOCK TO VIEW]\n")
                    } else {
                        sb.append("• ${med.name} (${med.dosage}) - ${med.time}\n")
                    }
                }
            }
        }

        if (config.showEmergencyContacts) {
            sb.append("\n📞 EMERGENCY CONTACTS:\n")
            if (contacts.isEmpty()) {
                sb.append("• No emergency contacts configured.\n")
            } else {
                contacts.forEach { contact ->
                    val primaryBadge = if (contact.isPrimary) " (Primary)" else ""
                    sb.append("• ${contact.name}$primaryBadge: ${contact.phoneNumber} [${contact.relationship}]\n")
                }
            }
        }

        if (config.showEmergencyNotes && config.emergencyNotes.isNotBlank()) {
            sb.append("\n📝 IMPORTANT MEDICAL NOTES:\n")
            if (isPrivacyRedacted) {
                sb.append("• [REDACTED SENSITIVE NOTES]\n")
            } else {
                sb.append("• ${config.emergencyNotes}\n")
            }
        }

        sb.append("\n=====================================\n")
        sb.append("Generated by MedBuddy Health App\n")

        return sb.toString()
    }
}
