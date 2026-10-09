package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.EmergencyContact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EmergencyContactRepository(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _contactsState = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val contactsFlow: StateFlow<List<EmergencyContact>> = _contactsState.asStateFlow()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        val json = prefs.getString(KEY_CONTACTS, null)
        val list: List<EmergencyContact> = if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<EmergencyContact>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
        _contactsState.value = ensureSinglePrimary(list)
    }

    private fun saveContactsToPrefs(contacts: List<EmergencyContact>) {
        val sanitizedList = ensureSinglePrimary(contacts)
        _contactsState.value = sanitizedList
        val json = gson.toJson(sanitizedList)
        prefs.edit().putString(KEY_CONTACTS, json).apply()
    }

    fun getContacts(): List<EmergencyContact> {
        return _contactsState.value
    }

    fun getPrimaryContact(): EmergencyContact? {
        return _contactsState.value.firstOrNull { it.isPrimary } ?: _contactsState.value.firstOrNull()
    }

    fun saveContact(contact: EmergencyContact) {
        val currentList = getContacts().toMutableList()
        val index = currentList.indexOfFirst { it.id == contact.id }

        if (index >= 0) {
            // Update existing contact
            currentList[index] = contact
        } else {
            // Add new contact (make primary if it's the first contact)
            val isFirst = currentList.isEmpty()
            currentList.add(contact.copy(isPrimary = contact.isPrimary || isFirst))
        }

        if (contact.isPrimary) {
            // Unmark primary from all other contacts
            for (i in currentList.indices) {
                if (currentList[i].id != contact.id) {
                    currentList[i] = currentList[i].copy(isPrimary = false)
                }
            }
        }

        saveContactsToPrefs(currentList)
    }

    fun deleteContact(contactId: String) {
        val currentList = getContacts().filterNot { it.id == contactId }.toMutableList()
        saveContactsToPrefs(currentList)
    }

    fun setPrimaryContact(contactId: String) {
        val currentList = getContacts().map { contact ->
            contact.copy(isPrimary = (contact.id == contactId))
        }
        saveContactsToPrefs(currentList)
    }

    private fun ensureSinglePrimary(list: List<EmergencyContact>): List<EmergencyContact> {
        if (list.isEmpty()) return emptyList()
        val primaryCount = list.count { it.isPrimary }
        if (primaryCount == 1) return list

        val mutable = list.toMutableList()
        if (primaryCount == 0) {
            mutable[0] = mutable[0].copy(isPrimary = true)
        } else {
            var foundPrimary = false
            for (i in mutable.indices) {
                if (mutable[i].isPrimary) {
                    if (foundPrimary) {
                        mutable[i] = mutable[i].copy(isPrimary = false)
                    } else {
                        foundPrimary = true
                    }
                }
            }
        }
        return mutable
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_sos_prefs"
        private const val KEY_CONTACTS = "key_emergency_contacts"

        @Volatile
        private var INSTANCE: EmergencyContactRepository? = null

        fun getInstance(context: Context): EmergencyContactRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EmergencyContactRepository(context).also { INSTANCE = it }
            }
        }
    }
}
