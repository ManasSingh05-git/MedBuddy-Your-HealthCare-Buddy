package com.manas.medbuddy.data.model

sealed class HospitalSearchResult {
    data class Success(val hospital: Hospital) : HospitalSearchResult()
    object PermissionDenied : HospitalSearchResult()
    object LocationUnavailable : HospitalSearchResult()
    object NoHospitalWithPhoneFound : HospitalSearchResult()
    data class Error(val message: String) : HospitalSearchResult()
}
