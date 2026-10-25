package com.manas.medbuddy.appointments

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

class AppointmentRepository(private val context: Context? = null) {

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
            val dir = File(context.filesDir, "appointments/$currentUserId")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun getAppointmentsReference(): DatabaseReference? = database?.reference?.child("appointments")?.child(currentUserId)

    fun listenToAppointments(
        onUpdate: (List<DoctorAppointment>) -> Unit,
        onError: (String) -> Unit = {}
    ): ValueEventListener? {
        val ref = getAppointmentsReference() ?: run {
            onError("Database service unavailable")
            return null
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val item = child.getValue(DoctorAppointment::class.java)
                    item?.copy(id = child.key ?: item.id)
                }.sortedBy { it.appointmentDate }
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
            getAppointmentsReference()?.removeEventListener(listener)
        }
    }

    fun saveAppointment(
        appointment: DoctorAppointment,
        sourceUri: Uri? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        var attachmentPath = appointment.localAttachmentPath
        var hasAttach = appointment.hasAttachment

        if (context != null && sourceUri != null) {
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(sourceUri) ?: ""
                val isPdf = mimeType.contains("pdf", ignoreCase = true) || sourceUri.toString().endsWith(".pdf", ignoreCase = true)
                val ext = if (isPdf) ".pdf" else ".jpg"

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val targetFile = File(userPrivateStorageDir, "appt_attach_$timestamp$ext")

                val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
                val outputStream = FileOutputStream(targetFile)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                attachmentPath = targetFile.absolutePath
                hasAttach = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val apptId = if (appointment.id.isBlank()) UUID.randomUUID().toString() else appointment.id
        val updatedAppt = appointment.copy(
            id = apptId,
            userId = currentUserId,
            localAttachmentPath = attachmentPath,
            hasAttachment = hasAttach
        )

        val ref = getAppointmentsReference()
        if (ref != null) {
            ref.child(apptId).setValue(updatedAppt)
                .addOnSuccessListener { onComplete(true, apptId) }
                .addOnFailureListener { err -> onComplete(false, err.message) }
        } else {
            onComplete(true, apptId)
        }
    }

    fun rescheduleAppointment(
        appointment: DoctorAppointment,
        newDate: String,
        newTime: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val updated = appointment.copy(
            previousRescheduledDate = "${appointment.appointmentDate} at ${appointment.appointmentTime}",
            appointmentDate = newDate,
            appointmentTime = newTime,
            status = AppointmentStatus.RESCHEDULED.name
        )
        saveAppointment(updated, sourceUri = null, onComplete = onComplete)
    }

    fun cancelAppointment(
        appointment: DoctorAppointment,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val updated = appointment.copy(status = AppointmentStatus.CANCELLED.name, reminderEnabled = false)
        saveAppointment(updated, sourceUri = null, onComplete = onComplete)
    }

    fun deleteAppointment(
        appointment: DoctorAppointment,
        onComplete: (Boolean, String?) -> Unit
    ) {
        try {
            if (appointment.localAttachmentPath.isNotBlank()) {
                val file = File(appointment.localAttachmentPath)
                if (file.exists()) file.delete()
            }

            val ref = getAppointmentsReference()
            if (ref != null) {
                ref.child(appointment.id).removeValue()
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { err -> onComplete(false, err.message) }
            } else {
                onComplete(true, null)
            }
        } catch (e: Exception) {
            onComplete(false, e.message)
        }
    }
}
