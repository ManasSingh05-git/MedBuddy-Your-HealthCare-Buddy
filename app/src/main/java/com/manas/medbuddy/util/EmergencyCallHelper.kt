package com.manas.medbuddy.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat

object EmergencyCallHelper {

    fun initiateCall(context: Context, rawPhoneNumber: String) {
        val formatted = PhoneNumberValidator.formatForCall(rawPhoneNumber)
        if (formatted.isEmpty()) return

        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPermission) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$formatted")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$formatted")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to dial intent if direct call fails
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$formatted")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(dialIntent)
            } catch (_: Exception) {
                // Ignore if device has no dialer
            }
        }
    }
}
