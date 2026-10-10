package com.manas.medbuddy.data.model

import java.util.Locale

data class Hospital(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val latitude: Double,
    val longitude: Double,
    val distanceInMeters: Float
) {
    fun getFormattedDistance(): String {
        return if (distanceInMeters >= 1000f) {
            String.format(Locale.US, "%.1f km away", distanceInMeters / 1000f)
        } else {
            "${distanceInMeters.toInt()} m away"
        }
    }
}
