package com.manas.medbuddy.vault

import android.content.Context
import android.net.Uri
import android.os.Environment
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

class MedicalVaultRepository(private val context: Context) {

    private val database: FirebaseDatabase? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
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

    private val userPrivateStorageDir: File
        get() {
            val dir = File(context.filesDir, "medical_vault/$currentUserId")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun getVaultReference(): DatabaseReference? = database?.reference?.child("medicalVault")?.child(currentUserId)

    fun listenToRecords(
        onUpdate: (List<MedicalRecord>) -> Unit,
        onError: (String) -> Unit = {}
    ): ValueEventListener? {
        val ref = getVaultReference() ?: run {
            onError("Database service unavailable")
            return null
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val rec = child.getValue(MedicalRecord::class.java)
                    rec?.copy(id = child.key ?: rec.id)
                }.sortedByDescending { it.createdAtTimestamp }
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
            getVaultReference()?.removeEventListener(listener)
        }
    }

    fun saveDocument(
        sourceUri: Uri,
        title: String,
        category: String,
        doctorName: String,
        documentDate: String,
        tags: List<String>,
        notes: String,
        onProgress: (Int) -> Unit,
        onComplete: (Boolean, String?) -> Unit
    ) {
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(sourceUri) ?: "application/octet-stream"
            val isPdf = mimeType.contains("pdf", ignoreCase = true) || sourceUri.toString().endsWith(".pdf", ignoreCase = true)
            val extension = if (isPdf) ".pdf" else ".jpg"

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val targetFile = File(userPrivateStorageDir, "vault_$timestamp$extension")

            val inputStream: InputStream = contentResolver.openInputStream(sourceUri) ?: run {
                onComplete(false, "Cannot read source file stream")
                return
            }

            val afd = contentResolver.openAssetFileDescriptor(sourceUri, "r")
            val totalBytes = afd?.length ?: 1L
            afd?.close()
            val maxAllowedBytes = 25 * 1024 * 1024L // 25 MB Limit

            if (totalBytes > maxAllowedBytes) {
                onComplete(false, "File exceeds maximum 25 MB size limit.")
                return
            }

            val outputStream = FileOutputStream(targetFile)
            val buffer = ByteArray(8192)
            var bytesCopied = 0L
            var read: Int

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesCopied += read
                        if (totalBytes > 0) {
                            val progress = ((bytesCopied * 100) / totalBytes).toInt().coerceIn(0, 100)
                            onProgress(progress)
                        }
                    }
                }
            }

            val fileSizeBytes = targetFile.length()
            val fileSizeFormatted = formatFileSize(fileSizeBytes)

            val recordId = UUID.randomUUID().toString()
            val record = MedicalRecord(
                id = recordId,
                userId = currentUserId,
                title = title.ifBlank { "Medical Document" },
                category = category,
                doctorName = doctorName,
                documentDate = documentDate.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) },
                localFilePath = targetFile.absolutePath,
                fileSizeBytes = fileSizeBytes,
                fileSizeFormatted = fileSizeFormatted,
                mimeType = mimeType,
                isPdf = isPdf,
                tags = tags,
                notes = notes,
                createdAtTimestamp = System.currentTimeMillis()
            )

            val ref = getVaultReference()
            if (ref != null) {
                ref.child(recordId).setValue(record)
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { err -> onComplete(false, err.message) }
            } else {
                // Local fallback if offline / database reference is unavailable
                onComplete(true, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(false, "Error storing document: ${e.message}")
        }
    }

    fun deleteRecord(record: MedicalRecord, onComplete: (Boolean, String?) -> Unit) {
        try {
            // Remove local file
            if (record.localFilePath.isNotBlank()) {
                val file = File(record.localFilePath)
                if (file.exists()) file.delete()
            }

            val ref = getVaultReference()
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

    fun exportRecordToDownloads(record: MedicalRecord, onComplete: (Boolean, String?) -> Unit) {
        try {
            val sourceFile = File(record.localFilePath)
            if (!sourceFile.exists()) {
                onComplete(false, "Local source file not found")
                return
            }

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val sanitizedTitle = record.title.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val extension = if (record.isPdf) ".pdf" else ".jpg"
            val targetFile = File(downloadsDir, "MedBuddy_${sanitizedTitle}$extension")

            sourceFile.copyTo(targetFile, overwrite = true)
            onComplete(true, targetFile.absolutePath)
        } catch (e: Exception) {
            onComplete(false, "Export failed: ${e.message}")
        }
    }

    private fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.getDefault(), "%.0f KB", kb)
            else -> "$bytes B"
        }
    }
}
