package com.manas.medbuddy.timeline

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.allergy.AllergyRecord
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.navigation.Routes
import com.manas.medbuddy.vault.MedicalRecord
import com.manas.medbuddy.vaccination.VaccinationRecord
import com.manas.medbuddy.vaccination.VaccinationStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class HealthTimelineRepository(private val context: Context? = null) {

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

    fun getTimelineReference(): DatabaseReference? = database?.reference?.child("healthTimeline")?.child(currentUserId)

    fun listenToCustomEvents(
        onUpdate: (List<TimelineEvent>) -> Unit,
        onError: (String) -> Unit = {}
    ): ValueEventListener? {
        val ref = getTimelineReference() ?: run {
            onError("Database service unavailable")
            return null
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val item = child.getValue(TimelineEvent::class.java)
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
            getTimelineReference()?.removeEventListener(listener)
        }
    }

    fun saveCustomEvent(event: TimelineEvent, onComplete: (Boolean, String?) -> Unit) {
        val ref = getTimelineReference() ?: run {
            onComplete(false, "Database service unavailable")
            return
        }

        val eventId = if (event.id.isBlank()) UUID.randomUUID().toString() else event.id
        val updatedEvent = event.copy(
            id = eventId,
            userId = currentUserId,
            isCustomUserEvent = true,
            sourceType = TimelineSourceType.USER_REPORTED.name,
            eventType = TimelineEventType.USER_EVENT.name
        )

        ref.child(eventId).setValue(updatedEvent)
            .addOnSuccessListener { onComplete(true, eventId) }
            .addOnFailureListener { err -> onComplete(false, err.message) }
    }

    fun deleteCustomEvent(eventId: String, onComplete: (Boolean, String?) -> Unit) {
        val ref = getTimelineReference() ?: run {
            onComplete(false, "Database service unavailable")
            return
        }

        ref.child(eventId).removeValue()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { err -> onComplete(false, err.message) }
    }

    fun aggregateAllTimelineEvents(
        customEvents: List<TimelineEvent>,
        vaultRecords: List<MedicalRecord>,
        vaccinations: List<VaccinationRecord>,
        medicines: List<Medicine>
    ): List<TimelineEvent> {
        val aggregated = mutableListOf<TimelineEvent>()

        // 1. Add custom user timeline events
        aggregated.addAll(customEvents)

        // 2. Add Medical Vault records
        vaultRecords.forEach { record ->
            val isClinicianFact = record.doctorName.isNotBlank() || record.category.contains("LAB", ignoreCase = true) || record.category.contains("DISCHARGE", ignoreCase = true)
            aggregated.add(
                TimelineEvent(
                    id = "vault_${record.id}",
                    userId = currentUserId,
                    title = record.title,
                    eventType = TimelineEventType.REPORT.name,
                    sourceType = if (isClinicianFact) TimelineSourceType.CLINICIAN_FACT.name else TimelineSourceType.USER_REPORTED.name,
                    eventDate = record.documentDate,
                    providerOrLocation = record.doctorName.ifBlank { "App Storage Vault" },
                    description = "Category: ${record.category} • Size: ${record.fileSizeFormatted}",
                    targetRoute = Routes.MedicalVault,
                    sourceRecordId = record.id
                )
            )
        }

        // 3. Add Vaccinations
        vaccinations.forEach { vac ->
            val isClinician = vac.status == VaccinationStatus.COMPLETED.name && vac.hasCertificate
            aggregated.add(
                TimelineEvent(
                    id = "vac_${vac.id}",
                    userId = currentUserId,
                    title = "Vaccine: ${vac.vaccineName} (${vac.doseNumber})",
                    eventType = TimelineEventType.VACCINATION.name,
                    sourceType = if (isClinician) TimelineSourceType.CLINICIAN_FACT.name else TimelineSourceType.USER_REPORTED.name,
                    eventDate = vac.date.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(vac.createdAtTimestamp)) },
                    providerOrLocation = vac.providerName.ifBlank { "Health Clinic" },
                    description = "Status: ${vac.status} • Certificate: ${if (vac.hasCertificate) "Attached" else "None"}",
                    targetRoute = Routes.VaccinationManager,
                    sourceRecordId = vac.id
                )
            )
        }

        // 4. Add Medicines
        medicines.forEach { med ->
            aggregated.add(
                TimelineEvent(
                    id = "med_${med.id}",
                    userId = currentUserId,
                    title = "Prescribed Medicine: ${med.name}",
                    eventType = TimelineEventType.MEDICINE.name,
                    sourceType = TimelineSourceType.CLINICIAN_FACT.name,
                    eventDate = med.startDate.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) },
                    providerOrLocation = med.instructions.ifBlank { "Prescribed Treatment" },
                    description = "Dosage: ${med.dosage} • Schedules: ${med.time}",
                    targetRoute = "${Routes.AddEditMedicine}?medicineId=${med.id}",
                    sourceRecordId = med.id
                )
            )
        }

        // Deduplicate by sourceRecordId and sort chronologically (newest first)
        return aggregated.distinctBy { it.id }.sortedByDescending { it.eventDate }
    }

    fun filterEventsByDate(
        events: List<TimelineEvent>,
        dateFilter: TimelineDateFilter
    ): List<TimelineEvent> {
        if (dateFilter == TimelineDateFilter.ALL_TIME) return events

        val calendar = Calendar.getInstance()

        val daysToSubtract = when (dateFilter) {
            TimelineDateFilter.LAST_30_DAYS -> 30
            TimelineDateFilter.LAST_6_MONTHS -> 180
            TimelineDateFilter.LAST_YEAR -> 365
            else -> 0
        }

        calendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
        val cutoffDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        return events.filter { event ->
            event.eventDate >= cutoffDateStr
        }
    }
}
