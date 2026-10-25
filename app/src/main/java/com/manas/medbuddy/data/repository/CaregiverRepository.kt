package com.manas.medbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manas.medbuddy.data.model.CaregiverPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CaregiverRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _permissionsState = MutableStateFlow<List<CaregiverPermission>>(emptyList())
    val permissionsFlow: StateFlow<List<CaregiverPermission>> = _permissionsState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val json = prefs.getString(KEY_PERMISSIONS, null)
        val list: List<CaregiverPermission> = if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<CaregiverPermission>>() {}.type
                gson.fromJson(json, type) ?: defaultPermissions()
            } catch (_: Exception) {
                defaultPermissions()
            }
        } else {
            defaultPermissions()
        }
        _permissionsState.value = list
    }

    private fun defaultPermissions(): List<CaregiverPermission> {
        val defaults = listOf(
            CaregiverPermission(
                id = "perm_1",
                caregiverName = "Dr. Robert Fox (Family Doctor)",
                caregiverEmail = "dr.fox@clinic.com",
                canViewAdherence = true,
                canViewAppointments = true,
                canViewVitals = false,
                isRevoked = false
            ),
            CaregiverPermission(
                id = "perm_2",
                caregiverName = "Sarah Jenkins (Daughter)",
                caregiverEmail = "sarah@gmail.com",
                canViewAdherence = true,
                canViewAppointments = true,
                canViewVitals = true,
                isRevoked = false
            )
        )
        saveToPrefs(defaults)
        return defaults
    }

    private fun saveToPrefs(list: List<CaregiverPermission>) {
        _permissionsState.value = list
        prefs.edit().putString(KEY_PERMISSIONS, gson.toJson(list)).apply()
    }

    fun grantPermission(permission: CaregiverPermission) {
        val current = _permissionsState.value.toMutableList()
        current.add(0, permission)
        saveToPrefs(current)
    }

    fun updatePermission(permission: CaregiverPermission) {
        val current = _permissionsState.value.toMutableList()
        val index = current.indexOfFirst { it.id == permission.id }
        if (index >= 0) {
            current[index] = permission
            saveToPrefs(current)
        }
    }

    fun revokePermission(id: String) {
        val current = _permissionsState.value.map {
            if (it.id == id) it.copy(isRevoked = true) else it
        }
        saveToPrefs(current)
    }

    fun deletePermission(id: String) {
        val filtered = _permissionsState.value.filterNot { it.id == id }
        saveToPrefs(filtered)
    }

    // Server-side / repository authorization & shared-data filtering
    fun getSharedAdherenceSummary(permissionId: String): String {
        val perm = _permissionsState.value.find { it.id == permissionId }
        if (perm == null || perm.isRevoked || !perm.canViewAdherence) {
            return "Access Denied: Adherence data permission is revoked or unauthorized."
        }
        return "Shared Adherence Summary: 100% medication adherence over the past 7 days. All doses taken on time."
    }

    fun getSharedUpcomingAppointments(permissionId: String): List<String> {
        val perm = _permissionsState.value.find { it.id == permissionId }
        if (perm == null || perm.isRevoked || !perm.canViewAppointments) {
            return emptyList()
        }
        return listOf(
            "Dr. Sharma - General Physician (Tomorrow, 10:00 AM)",
            "Dr. Anjali Mehta - Cardiologist (Oct 28, 2025)"
        )
    }

    companion object {
        private const val PREFS_NAME = "medbuddy_caregiver_prefs"
        private const val KEY_PERMISSIONS = "key_caregiver_permissions"

        @Volatile
        private var INSTANCE: CaregiverRepository? = null

        fun getInstance(context: Context): CaregiverRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CaregiverRepository(context).also { INSTANCE = it }
            }
        }
    }
}
