package com.manas.medbuddy.insurance

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

class InsuranceRepository(private val context: Context? = null) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
            val dir = File(context.filesDir, "insurance/$currentUserId")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun getInsuranceReference(): DatabaseReference? = database?.reference?.child("healthInsurance")?.child(currentUserId)

    fun listenToPolicies(
        onUpdate: (List<InsurancePolicy>) -> Unit,
        onError: (String) -> Unit = {}
    ): ValueEventListener? {
        val ref = getInsuranceReference() ?: run {
            onError("Database service unavailable")
            return null
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val item = child.getValue(InsurancePolicy::class.java)
                    item?.copy(id = child.key ?: item.id)
                }.sortedBy { it.expiryDate }
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
            getInsuranceReference()?.removeEventListener(listener)
        }
    }

    fun savePolicy(
        policy: InsurancePolicy,
        sourceUri: Uri? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        var docPath = policy.localDocumentPath
        var hasDoc = policy.hasDocument

        if (context != null && sourceUri != null) {
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(sourceUri) ?: ""
                val isPdf = mimeType.contains("pdf", ignoreCase = true) || sourceUri.toString().endsWith(".pdf", ignoreCase = true)
                val ext = if (isPdf) ".pdf" else ".jpg"

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val targetFile = File(userPrivateStorageDir, "policy_doc_$timestamp$ext")

                val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
                val outputStream = FileOutputStream(targetFile)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                docPath = targetFile.absolutePath
                hasDoc = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val policyId = if (policy.id.isBlank()) UUID.randomUUID().toString() else policy.id
        val updatedPolicy = policy.copy(
            id = policyId,
            userId = currentUserId,
            localDocumentPath = docPath,
            hasDocument = hasDoc,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )

        val ref = getInsuranceReference()
        if (ref != null) {
            ref.child(policyId).setValue(updatedPolicy)
                .addOnSuccessListener { onComplete(true, policyId) }
                .addOnFailureListener { err -> onComplete(false, err.message) }
        } else {
            onComplete(true, policyId)
        }
    }

    fun addClaimToPolicy(
        policy: InsurancePolicy,
        claim: InsuranceClaim,
        billUri: Uri? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        var billPath = claim.localBillPath
        var hasBill = claim.hasBillAttachment

        if (context != null && billUri != null) {
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(billUri) ?: ""
                val isPdf = mimeType.contains("pdf", ignoreCase = true) || billUri.toString().endsWith(".pdf", ignoreCase = true)
                val ext = if (isPdf) ".pdf" else ".jpg"

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val targetFile = File(userPrivateStorageDir, "claim_bill_$timestamp$ext")

                val inputStream: InputStream? = contentResolver.openInputStream(billUri)
                val outputStream = FileOutputStream(targetFile)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                billPath = targetFile.absolutePath
                hasBill = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val claimId = if (claim.id.isBlank()) UUID.randomUUID().toString() else claim.id
        val updatedClaim = claim.copy(
            id = claimId,
            localBillPath = billPath,
            hasBillAttachment = hasBill
        )

        val updatedClaimsList = policy.claims.filterNot { it.id == claimId } + updatedClaim
        val updatedPolicy = policy.copy(claims = updatedClaimsList)

        savePolicy(updatedPolicy, sourceUri = null, onComplete = onComplete)
    }

    fun calculateDaysToExpiry(expiryDateStr: String, fromDate: Date = Date()): Int {
        if (expiryDateStr.isBlank()) return 365
        return try {
            val expDate = dateFormat.parse(expiryDateStr) ?: return 365
            val diffMs = expDate.time - fromDate.time
            (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } catch (_: Exception) {
            365
        }
    }

    fun deletePolicy(policy: InsurancePolicy, onComplete: (Boolean, String?) -> Unit) {
        try {
            if (policy.localDocumentPath.isNotBlank()) {
                val file = File(policy.localDocumentPath)
                if (file.exists()) file.delete()
            }

            val ref = getInsuranceReference()
            if (ref != null) {
                ref.child(policy.id).removeValue()
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
