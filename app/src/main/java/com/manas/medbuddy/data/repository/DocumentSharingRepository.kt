package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.DocumentAuditLog
import com.manas.medbuddy.data.model.SharedDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DocumentSharingRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _documentsState = MutableStateFlow<List<SharedDocument>>(emptyList())
    val documentsFlow: StateFlow<List<SharedDocument>> = _documentsState.asStateFlow()

    private val _auditState = MutableStateFlow<List<DocumentAuditLog>>(emptyList())
    val auditFlow: StateFlow<List<DocumentAuditLog>> = _auditState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val docJson = prefs.getString(KEY_DOCS, null)
        val docs: List<SharedDocument> = if (!docJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<SharedDocument>>() {}.type
                gson.fromJson(docJson, type) ?: defaultDocs()
            } catch (_: Exception) {
                defaultDocs()
            }
        } else {
            defaultDocs()
        }
        _documentsState.value = docs.sortedByDescending { it.sharedAt }

        val auditJson = prefs.getString(KEY_AUDIT, null)
        val audit: List<DocumentAuditLog> = if (!auditJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<DocumentAuditLog>>() {}.type
                gson.fromJson(auditJson, type) ?: defaultAudit()
            } catch (_: Exception) {
                defaultAudit()
            }
        } else {
            defaultAudit()
        }
        _auditState.value = audit.sortedByDescending { it.timestamp }
    }

    private fun defaultDocs(): List<SharedDocument> {
        val defaults = listOf(
            SharedDocument(
                documentTitle = "Blood Test Complete Panel - Oct 2025",
                ownerName = "Manas Singh",
                recipientName = "Dr. Fox",
                secureHandle = "medbuddy-secure-token-a8f921bc",
                isRevoked = false
            )
        )
        saveDocsToPrefs(defaults)
        return defaults
    }

    private fun defaultAudit(): List<DocumentAuditLog> {
        val defaults = listOf(
            DocumentAuditLog(
                action = "Shared",
                documentTitle = "Blood Test Complete Panel - Oct 2025",
                details = "Shared securely with Dr. Fox via tokenized handle."
            )
        )
        saveAuditToPrefs(defaults)
        return defaults
    }

    private fun saveDocsToPrefs(list: List<SharedDocument>) {
        val sorted = list.sortedByDescending { it.sharedAt }
        _documentsState.value = sorted
        prefs.edit().putString(KEY_DOCS, gson.toJson(sorted)).apply()
    }

    private fun saveAuditToPrefs(list: List<DocumentAuditLog>) {
        val sorted = list.sortedByDescending { it.timestamp }
        _auditState.value = sorted
        prefs.edit().putString(KEY_AUDIT, gson.toJson(sorted)).apply()
    }

    private fun logAudit(action: String, title: String, details: String) {
        val current = _auditState.value.toMutableList()
        current.add(0, DocumentAuditLog(action = action, documentTitle = title, details = details))
        saveAuditToPrefs(current)
    }

    fun shareDocument(title: String, owner: String, recipient: String): String? {
        if (title.isBlank() || recipient.isBlank()) {
            return "Please provide a document title and recipient."
        }
        val doc = SharedDocument(
            documentTitle = title,
            ownerName = owner,
            recipientName = recipient,
            isRevoked = false
        )
        val current = _documentsState.value.toMutableList()
        current.add(0, doc)
        saveDocsToPrefs(current)
        logAudit("Shared", title, "Explicitly shared with $recipient using secure tokenized handle.")
        return null
    }

    fun revokeDocument(id: String) {
        val current = _documentsState.value.map {
            if (it.id == id) {
                logAudit("Revoked", it.documentTitle, "Access revoked for recipient ${it.recipientName}.")
                it.copy(isRevoked = true)
            } else {
                it
            }
        }
        saveDocsToPrefs(current)
    }

    // Repository-level authorization check: returns secure handle ONLY if active and unrevoked
    fun accessDocument(id: String): String? {
        val doc = _documentsState.value.find { it.id == id }
        if (doc == null || doc.isRevoked) {
            logAudit("Access Attempted", doc?.documentTitle ?: "Unknown", "Unauthorized or revoked access attempt blocked.")
            return null
        }
        logAudit("Access Attempted", doc.documentTitle, "Authorized access granted via secure handle.")
        return doc.secureHandle
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_document_sharing_prefs"
        private const val KEY_DOCS = "key_shared_docs"
        private const val KEY_AUDIT = "key_document_audit"

        @Volatile
        private var INSTANCE: DocumentSharingRepository? = null

        fun getInstance(context: Context): DocumentSharingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DocumentSharingRepository(context).also { INSTANCE = it }
            }
        }
    }
}
