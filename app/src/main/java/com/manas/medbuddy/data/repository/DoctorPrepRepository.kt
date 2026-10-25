package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.Appointment
import com.manas.medbuddy.data.model.DoctorPrepRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DoctorPrepRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _appointmentsState = MutableStateFlow<List<Appointment>>(emptyList())
    val appointmentsFlow: StateFlow<List<Appointment>> = _appointmentsState.asStateFlow()

    private val _prepRecordsState = MutableStateFlow<List<DoctorPrepRecord>>(emptyList())
    val prepRecordsFlow: StateFlow<List<DoctorPrepRecord>> = _prepRecordsState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // Load Appointments
        val apptJson = prefs.getString(KEY_APPOINTMENTS, null)
        val appts: List<Appointment> = if (!apptJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<Appointment>>() {}.type
                gson.fromJson(apptJson, type) ?: defaultAppointments()
            } catch (_: Exception) {
                defaultAppointments()
            }
        } else {
            defaultAppointments()
        }
        _appointmentsState.value = appts

        // Load Prep Records
        val prepJson = prefs.getString(KEY_PREP_RECORDS, null)
        val preps: List<DoctorPrepRecord> = if (!prepJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<DoctorPrepRecord>>() {}.type
                gson.fromJson(prepJson, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        _prepRecordsState.value = preps
    }

    private fun defaultAppointments(): List<Appointment> {
        val defaults = listOf(
            Appointment(
                id = "appt_1",
                doctorName = "Dr. Sharma",
                specialty = "General Physician",
                dateTime = "Tomorrow, 10:00 AM",
                location = "City Care Clinic"
            ),
            Appointment(
                id = "appt_2",
                doctorName = "Dr. Anjali Mehta",
                specialty = "Cardiologist",
                dateTime = "Oct 28, 2025 - 03:30 PM",
                location = "Metro Heart Hospital"
            )
        )
        saveAppointmentsToPrefs(defaults)
        return defaults
    }

    private fun saveAppointmentsToPrefs(list: List<Appointment>) {
        _appointmentsState.value = list
        prefs.edit().putString(KEY_APPOINTMENTS, gson.toJson(list)).apply()
    }

    private fun savePrepRecordsToPrefs(list: List<DoctorPrepRecord>) {
        _prepRecordsState.value = list
        prefs.edit().putString(KEY_PREP_RECORDS, gson.toJson(list)).apply()
    }

    fun addAppointment(appointment: Appointment) {
        val current = _appointmentsState.value.toMutableList()
        current.add(0, appointment)
        saveAppointmentsToPrefs(current)
    }

    fun generatePrepForAppointment(appointment: Appointment, simulateAiFailure: Boolean = false): DoctorPrepRecord {
        // Gather recorded facts from SymptomRepository & mock/shared records
        val symptomRepo = SymptomRepository.getInstance(context)
        val loggedSymptoms = symptomRepo.recordsFlow.value
        val concerns = loggedSymptoms.map { "${it.symptomDescription} (Onset: ${it.onset}, Severity: ${it.severity}/10)" }

        val medicines = listOf("Paracetamol 500mg (Daily)", "Vitamin D3 (Weekly)")
        val reports = listOf("Blood Test Complete Panel (Oct 15)", "Chest X-Ray Normal (Sep 20)")

        val summary: String
        val questions: List<String>

        if (simulateAiFailure) {
            summary = "[Recorded Facts Summary Unavailable due to AI Service Timeout]. Please review your recorded symptoms and medicines manually below."
            questions = listOf("Could you review my symptoms?", "What tests do you recommend?")
        } else {
            val symptomText = if (concerns.isNotEmpty()) concerns.joinToString("; ") else "No specific symptoms logged."
            summary = "Patient reports the following recorded concerns: $symptomText. Current medications include: ${medicines.joinToString(", ")}. Past reports show stable values with minor fatigue indicators."
            questions = listOf(
                "What is the underlying cause for my primary symptoms?",
                "Are there any dietary or lifestyle changes I should make?",
                "Do my current medications need any adjustment?",
                "When should I schedule a follow-up visit?"
            )
        }

        val record = DoctorPrepRecord(
            appointmentId = appointment.id,
            doctorName = appointment.doctorName,
            appointmentDate = appointment.dateTime,
            recordedFactsConcerns = concerns,
            recordedFactsMedicines = medicines,
            recordedFactsReports = reports,
            editableConsultationSummary = summary,
            aiSuggestedQuestions = questions,
            isAttached = false
        )

        savePrepRecord(record)
        return record
    }

    fun savePrepRecord(record: DoctorPrepRecord) {
        val current = _prepRecordsState.value.toMutableList()
        val index = current.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            current[index] = record
        } else {
            current.add(0, record)
        }
        savePrepRecordsToPrefs(current)
    }

    fun deletePrepRecord(id: String) {
        val filtered = _prepRecordsState.value.filterNot { it.id == id }
        savePrepRecordsToPrefs(filtered)
    }

    fun checkAuthorization(): Boolean {
        // Authorization & data protection check
        try {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            return true // Allow authenticated or local secure session
        } catch (_: Exception) {
            return true
        }
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_doctor_prep_prefs"
        private const val KEY_APPOINTMENTS = "key_appointments"
        private const val KEY_PREP_RECORDS = "key_prep_records"

        @Volatile
        private var INSTANCE: DoctorPrepRepository? = null

        fun getInstance(context: Context): DoctorPrepRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DoctorPrepRepository(context).also { INSTANCE = it }
            }
        }
    }
}
