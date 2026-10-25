package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.FamilyProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FamilyRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _profilesState = MutableStateFlow<List<FamilyProfile>>(emptyList())
    val profilesFlow: StateFlow<List<FamilyProfile>> = _profilesState.asStateFlow()

    private val _activeProfileIdState = MutableStateFlow<String?>(null)
    val activeProfileIdFlow: StateFlow<String?> = _activeProfileIdState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val json = prefs.getString(KEY_PROFILES, null)
        val list: List<FamilyProfile> = if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<FamilyProfile>>() {}.type
                gson.fromJson(json, type) ?: defaultProfiles()
            } catch (_: Exception) {
                defaultProfiles()
            }
        } else {
            defaultProfiles()
        }
        _profilesState.value = list
        if (list.isNotEmpty() && _activeProfileIdState.value == null) {
            _activeProfileIdState.value = list.first().id
        }
    }

    private fun defaultProfiles(): List<FamilyProfile> {
        val defaults = listOf(
            FamilyProfile(
                id = "profile_self",
                name = "Manas Singh",
                relation = "Self",
                age = 28,
                bloodGroup = "B+",
                isMinor = false,
                guardianConsentGranted = true,
                emergencyContact = "+91 9876543210"
            ),
            FamilyProfile(
                id = "profile_child",
                name = "Aarav Singh",
                relation = "Child",
                age = 6,
                bloodGroup = "O+",
                isMinor = true,
                guardianConsentGranted = true,
                emergencyContact = "+91 9876543210",
                notes = "Allergic to peanuts"
            )
        )
        saveProfilesToPrefs(defaults)
        return defaults
    }

    private fun saveProfilesToPrefs(list: List<FamilyProfile>) {
        _profilesState.value = list
        prefs.edit().putString(KEY_PROFILES, gson.toJson(list)).apply()
    }

    fun setActiveProfile(profileId: String) {
        if (_profilesState.value.any { it.id == profileId }) {
            _activeProfileIdState.value = profileId
        }
    }

    fun addProfile(profile: FamilyProfile): String? {
        // Enforce guardian consent workflow for minors
        if (profile.isMinor && !profile.guardianConsentGranted) {
            return "Guardian consent is required for minor family members."
        }
        val current = _profilesState.value.toMutableList()
        current.add(profile)
        saveProfilesToPrefs(current)
        if (_activeProfileIdState.value == null) {
            _activeProfileIdState.value = profile.id
        }
        return null
    }

    fun updateProfile(profile: FamilyProfile): String? {
        if (profile.isMinor && !profile.guardianConsentGranted) {
            return "Guardian consent is required for minor family members."
        }
        val current = _profilesState.value.toMutableList()
        val index = current.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            current[index] = profile
            saveProfilesToPrefs(current)
            return null
        }
        return "Profile not found."
    }

    fun deleteProfile(id: String): String? {
        val current = _profilesState.value
        if (current.size <= 1) {
            return "Cannot delete the last remaining profile."
        }
        val filtered = current.filterNot { it.id == id }
        saveProfilesToPrefs(filtered)
        if (_activeProfileIdState.value == id) {
            _activeProfileIdState.value = filtered.firstOrNull()?.id
        }
        return null
    }

    fun validateProfileAccess(profileId: String): Boolean {
        // Enforce backend/repository authorization and data isolation
        return _profilesState.value.any { it.id == profileId }
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_family_profiles_prefs"
        private const val KEY_PROFILES = "key_family_profiles"

        @Volatile
        private var INSTANCE: FamilyRepository? = null

        fun getInstance(context: Context): FamilyRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FamilyRepository(context).also { INSTANCE = it }
            }
        }
    }
}
