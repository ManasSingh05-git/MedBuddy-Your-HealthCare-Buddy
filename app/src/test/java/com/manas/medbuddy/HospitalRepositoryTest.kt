package com.manas.medbuddy

import com.manas.medbuddy.data.model.Hospital
import com.manas.medbuddy.data.model.HospitalSearchResult
import com.manas.medbuddy.util.PhoneNumberValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HospitalRepositoryTest {

    @Test
    fun hospitalFormattedDistance_metersAndKilometers() {
        val closeHospital = Hospital(
            id = "1",
            name = "City Care Hospital",
            latitude = 12.9716,
            longitude = 77.5946,
            distanceInMeters = 450f
        )
        assertEquals("450 m away", closeHospital.getFormattedDistance())

        val farHospital = Hospital(
            id = "2",
            name = "General Hospital",
            latitude = 12.9816,
            longitude = 77.6046,
            distanceInMeters = 3200f
        )
        assertEquals("3.2 km away", farHospital.getFormattedDistance())
    }

    @Test
    fun hospitalSortingByDistance_correctOrder() {
        val h1 = Hospital("1", "Far Hospital", latitude = 0.0, longitude = 0.0, distanceInMeters = 5000f)
        val h2 = Hospital("2", "Near Hospital", latitude = 0.0, longitude = 0.0, distanceInMeters = 1200f)
        val h3 = Hospital("3", "Closest Hospital", latitude = 0.0, longitude = 0.0, distanceInMeters = 300f)

        val unsortedList = listOf(h1, h2, h3)
        val sortedList = unsortedList.sortedBy { item -> item.distanceInMeters }

        assertEquals("Closest Hospital", sortedList[0].name)
        assertEquals("Near Hospital", sortedList[1].name)
        assertEquals("Far Hospital", sortedList[2].name)
    }

    @Test
    fun hospitalPhoneNumberValidation_filtersInvalidNumbers() {
        val validPhone = "+1 555-0199"
        val invalidPhone = "123"

        assertTrue(PhoneNumberValidator.isValid(validPhone))
        assertFalse(PhoneNumberValidator.isValid(invalidPhone))
    }

    @Test
    fun hospitalSearchResult_successListTypeCheck() {
        val h1 = Hospital("1", "City Hospital", latitude = 0.0, longitude = 0.0, distanceInMeters = 500f)
        val result: HospitalSearchResult = HospitalSearchResult.SuccessList(listOf(h1))

        if (result is HospitalSearchResult.SuccessList) {
            assertEquals(1, result.hospitals.size)
            assertEquals("City Hospital", result.hospitals[0].name)
        }
    }
}
