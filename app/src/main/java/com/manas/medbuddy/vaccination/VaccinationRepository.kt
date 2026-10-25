package com.manas.medbuddy.vaccination

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

class VaccinationRepository(private val context: Context? = null) {

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
            val dir = File(context.filesDir, "vaccinations/$currentUserId")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun getVaccinationsReference(): DatabaseReference? = database?.reference?.child("vaccinations")?.child(currentUserId)

    fun listenToVaccinations(
        onUpdate: (List<VaccinationRecord>) -> Unit,
        onError: (String) -> Unit = {}
    ): ValueEventListener? {
        val ref = getVaccinationsReference() ?: run {
            onError("Database service unavailable")
            return null
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val item = child.getValue(VaccinationRecord::class.java)
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
            getVaccinationsReference()?.removeEventListener(listener)
        }
    }

    fun saveVaccinationRecord(
        record: VaccinationRecord,
        sourceUri: Uri? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        var certificatePath = record.localCertificatePath
        var hasCert = record.hasCertificate

        if (context != null && sourceUri != null) {
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(sourceUri) ?: ""
                val isPdf = mimeType.contains("pdf", ignoreCase = true) || sourceUri.toString().endsWith(".pdf", ignoreCase = true)
                val ext = if (isPdf) ".pdf" else ".jpg"

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val targetFile = File(userPrivateStorageDir, "vac_cert_$timestamp$ext")

                val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
                val outputStream = FileOutputStream(targetFile)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                certificatePath = targetFile.absolutePath
                hasCert = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val recordId = if (record.id.isBlank()) UUID.randomUUID().toString() else record.id
        val updatedRecord = record.copy(
            id = recordId,
            userId = currentUserId,
            localCertificatePath = certificatePath,
            hasCertificate = hasCert
        )

        val ref = getVaccinationsReference()
        if (ref != null) {
            ref.child(recordId).setValue(updatedRecord)
                .addOnSuccessListener { onComplete(true, recordId) }
                .addOnFailureListener { err -> onComplete(false, err.message) }
        } else {
            onComplete(true, recordId)
        }
    }

    fun deleteVaccinationRecord(record: VaccinationRecord, onComplete: (Boolean, String?) -> Unit) {
        try {
            if (record.localCertificatePath.isNotBlank()) {
                val file = File(record.localCertificatePath)
                if (file.exists()) file.delete()
            }

            val ref = getVaccinationsReference()
            if (ref != null) {
                ref.child(record.id).removeValue()
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
