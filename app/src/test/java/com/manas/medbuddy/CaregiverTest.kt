package com.manas.medbuddy

import com.manas.medbuddy.data.model.CaregiverPermission
import org.junit.Assert.*
import org.junit.Test

class CaregiverTest {

    @Test
    fun testRevokedPermissions_blocksAccess() {
        val perm = CaregiverPermission(
            id = "perm_1",
            caregiverName = "Dr. Fox",
            caregiverEmail = "fox@med.com",
            canViewAdherence = true,
            isRevoked = true
        )

        val summary = if (perm.isRevoked || !perm.canViewAdherence) {
            "Access Denied: Adherence data permission is revoked or unauthorized."
        } else {
            "Adherence Summary: OK"
        }

        assertTrue(summary.contains("Access Denied"))
    }

    @Test
    fun testSharedDataFiltering_basedOnPermissions() {
        val permWithoutAppointments = CaregiverPermission(
            id = "perm_2",
            caregiverName = "Family",
            caregiverEmail = "fam@test.com",
            canViewAppointments = false,
            isRevoked = false
        )

        val appointments = if (!permWithoutAppointments.canViewAppointments || permWithoutAppointments.isRevoked) {
            emptyList()
        } else {
            listOf("Appointment 1")
        }

        assertTrue(appointments.isEmpty())
    }

    @Test
    fun testActivePermission_grantsAccess() {
        val perm = CaregiverPermission(
            id = "perm_3",
            caregiverName = "Dr. Smith",
            caregiverEmail = "smith@med.com",
            canViewAdherence = true,
            isRevoked = false
        )

        val hasAccess = !perm.isRevoked && perm.canViewAdherence
        assertTrue(hasAccess)
    }
}
