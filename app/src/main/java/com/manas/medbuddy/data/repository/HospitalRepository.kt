package com.manas.medbuddy.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.manas.medbuddy.BuildConfig
import com.manas.medbuddy.data.model.Hospital
import com.manas.medbuddy.data.model.HospitalSearchResult
import com.manas.medbuddy.util.PhoneNumberValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class HospitalRepository(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    fun hasLocationPermission(): Boolean {
        val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return finePerm == PackageManager.PERMISSION_GRANTED || coarsePerm == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getUserLocation(): Location? {
        if (!hasLocationPermission()) return null
        return fetchCurrentLocation()
    }

    suspend fun findNearestHospital(): HospitalSearchResult = withContext(Dispatchers.IO) {
        val result = searchNearbyHospitalsList(radiusMeters = 15000)
        return@withContext when (result) {
            is HospitalSearchResult.SuccessList -> {
                val withPhone = result.hospitals.filter { it.phoneNumber.isNotBlank() && PhoneNumberValidator.isValid(it.phoneNumber) }
                val nearest = withPhone.minByOrNull { it.distanceInMeters } ?: result.hospitals.minByOrNull { it.distanceInMeters }
                if (nearest != null) HospitalSearchResult.Success(nearest) else HospitalSearchResult.NoHospitalFound
            }
            else -> result
        }
    }

    suspend fun searchNearbyHospitalsList(radiusMeters: Int = 15000): HospitalSearchResult = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext HospitalSearchResult.PermissionDenied
        }

        val location = fetchCurrentLocation() ?: return@withContext HospitalSearchResult.LocationUnavailable

        val userLat = location.latitude
        val userLng = location.longitude

        return@withContext fetchHospitalsAroundCoordinates(userLat, userLng, radiusMeters)
    }

    suspend fun searchHospitalsByManualQuery(query: String, radiusMeters: Int = 20000): HospitalSearchResult = withContext(Dispatchers.IO) {
        if (query.trim().isEmpty()) {
            return@withContext HospitalSearchResult.Error("Search query cannot be empty")
        }

        try {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            val geocodeUrl = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=1"
            val jsonStr = executeHttpGet(geocodeUrl) ?: return@withContext HospitalSearchResult.Error("Unable to locate specified address")
            val array = JsonParser.parseString(jsonStr).asJsonArray
            if (array.size() == 0) {
                return@withContext HospitalSearchResult.Error("Location '$query' not found")
            }
            val first = array.get(0).asJsonObject
            val lat = first.get("lat").asString.toDouble()
            val lon = first.get("lon").asString.toDouble()

            return@withContext fetchHospitalsAroundCoordinates(lat, lon, radiusMeters)
        } catch (e: Exception) {
            return@withContext HospitalSearchResult.Error("Manual search failed: ${e.localizedMessage}")
        }
    }

    private fun fetchHospitalsAroundCoordinates(userLat: Double, userLng: Double, radiusMeters: Int): HospitalSearchResult {
        val placesApiKey = getPlacesApiKey()
        if (placesApiKey.isNotBlank()) {
            val googleList = searchGooglePlacesList(userLat, userLng, placesApiKey, radiusMeters)
            if (googleList.isNotEmpty()) {
                return HospitalSearchResult.SuccessList(googleList.sortedBy { it.distanceInMeters })
            }
        }

        val overpassList = searchOverpassHospitalsList(userLat, userLng, radiusMeters)
        return if (overpassList.isNotEmpty()) {
            HospitalSearchResult.SuccessList(overpassList.sortedBy { it.distanceInMeters })
        } else {
            HospitalSearchResult.NoHospitalFound
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchCurrentLocation(): Location? {
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await() ?: fusedLocationClient.lastLocation.await()
        } catch (_: Exception) {
            null
        }
    }

    private fun getPlacesApiKey(): String {
        return try {
            BuildConfig.PLACES_API_KEY
        } catch (_: Exception) {
            ""
        }
    }

    private fun searchGooglePlacesList(lat: Double, lng: Double, apiKey: String, radiusMeters: Int): List<Hospital> {
        return try {
            val urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=$lat,$lng&radius=$radiusMeters&type=hospital&key=$apiKey"
            val jsonStr = executeHttpGet(urlString) ?: return emptyList()
            val root = JsonParser.parseString(jsonStr).asJsonObject
            val results = root.getAsJsonArray("results") ?: JsonArray()

            val hospitals = mutableListOf<Hospital>()

            for (element in results) {
                val obj = element.asJsonObject
                val placeId = obj.get("place_id")?.asString ?: continue
                val name = obj.get("name")?.asString ?: "Hospital"
                val vicinity = obj.get("vicinity")?.asString ?: ""
                val rating = obj.get("rating")?.asDouble
                val userRatingsTotal = obj.get("user_ratings_total")?.asInt
                val openNow = obj.getAsJsonObject("opening_hours")?.get("open_now")?.asBoolean

                val locationObj = obj.getAsJsonObject("geometry")?.getAsJsonObject("location") ?: continue
                val hospLat = locationObj.get("lat").asDouble
                val hospLng = locationObj.get("lng").asDouble

                val phone = fetchPlacePhoneNumber(placeId, apiKey) ?: ""
                val distance = calculateDistance(lat, lng, hospLat, hospLng)

                hospitals.add(
                    Hospital(
                        id = placeId,
                        name = name,
                        phoneNumber = phone,
                        address = vicinity,
                        rating = rating,
                        userRatingsTotal = userRatingsTotal,
                        isOpenNow = openNow,
                        latitude = hospLat,
                        longitude = hospLng,
                        distanceInMeters = distance
                    )
                )
            }
            hospitals
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun fetchPlacePhoneNumber(placeId: String, apiKey: String): String? {
        return try {
            val detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json" +
                    "?place_id=$placeId&fields=formatted_phone_number,international_phone_number&key=$apiKey"
            val jsonStr = executeHttpGet(detailsUrl) ?: return null
            val root = JsonParser.parseString(jsonStr).asJsonObject
            val result = root.getAsJsonObject("result") ?: return null
            result.get("international_phone_number")?.asString
                ?: result.get("formatted_phone_number")?.asString
        } catch (_: Exception) {
            null
        }
    }

    private fun searchOverpassHospitalsList(userLat: Double, userLng: Double, radiusMeters: Int): List<Hospital> {
        return try {
            val overpassQuery = "[out:json][timeout:15];" +
                    "(node[\"amenity\"=\"hospital\"](around:$radiusMeters,$userLat,$userLng);" +
                    "way[\"amenity\"=\"hospital\"](around:$radiusMeters,$userLat,$userLng););" +
                    "out center;"

            val encodedQuery = URLEncoder.encode(overpassQuery, "UTF-8")
            val urlString = "https://overpass-api.de/api/interpreter?data=$encodedQuery"

            val jsonStr = executeHttpGet(urlString) ?: return emptyList()
            val root = JsonParser.parseString(jsonStr).asJsonObject
            val elements = root.getAsJsonArray("elements") ?: JsonArray()

            val list = mutableListOf<Hospital>()

            for (elem in elements) {
                val obj = elem.asJsonObject
                val id = obj.get("id")?.asString ?: continue
                val tags = obj.getAsJsonObject("tags") ?: continue

                val name = tags.get("name")?.asString
                    ?: tags.get("name:en")?.asString
                    ?: "Medical Center / Hospital"

                val phoneTag = tags.get("phone")?.asString
                    ?: tags.get("contact:phone")?.asString
                    ?: tags.get("emergency:phone")?.asString
                    ?: tags.get("phone:emergency")?.asString
                    ?: ""

                val street = tags.get("addr:street")?.asString ?: ""
                val city = tags.get("addr:city")?.asString ?: ""
                val address = listOf(street, city).filter { it.isNotBlank() }.joinToString(", ")

                val hospLat = obj.get("lat")?.asDouble
                    ?: obj.getAsJsonObject("center")?.get("lat")?.asDouble
                    ?: continue
                val hospLng = obj.get("lon")?.asDouble
                    ?: obj.getAsJsonObject("center")?.get("lon")?.asDouble
                    ?: continue

                val dist = calculateDistance(userLat, userLng, hospLat, hospLng)
                list.add(
                    Hospital(
                        id = id,
                        name = name,
                        phoneNumber = phoneTag,
                        address = address,
                        rating = null,
                        userRatingsTotal = null,
                        isOpenNow = null,
                        latitude = hospLat,
                        longitude = hospLng,
                        distanceInMeters = dist
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    private fun executeHttpGet(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", "MedBuddy-AndroidApp/1.0")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
