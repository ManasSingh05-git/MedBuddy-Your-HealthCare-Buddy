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

    suspend fun findNearestHospital(): HospitalSearchResult = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext HospitalSearchResult.PermissionDenied
        }

        val location = fetchCurrentLocation() ?: return@withContext HospitalSearchResult.LocationUnavailable

        val userLat = location.latitude
        val userLng = location.longitude

        // 1. Try Google Places API if key configured
        val placesApiKey = getPlacesApiKey()
        if (placesApiKey.isNotBlank()) {
            val placesResult = searchGooglePlaces(userLat, userLng, placesApiKey)
            if (placesResult is HospitalSearchResult.Success) {
                return@withContext placesResult
            }
        }

        // 2. Query Overpass API (OpenStreetMap)
        val overpassResult = searchOverpassHospitals(userLat, userLng)
        return@withContext overpassResult
    }

    private fun hasLocationPermission(): Boolean {
        val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return finePerm == PackageManager.PERMISSION_GRANTED || coarsePerm == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchCurrentLocation(): Location? {
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await() ?: fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }

    private fun getPlacesApiKey(): String {
        return try {
            BuildConfig.PLACES_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    private fun searchGooglePlaces(lat: Double, lng: Double, apiKey: String): HospitalSearchResult {
        return try {
            val urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=$lat,$lng&radius=15000&type=hospital&key=$apiKey"
            val jsonStr = executeHttpGet(urlString) ?: return HospitalSearchResult.Error("Places API network error")
            val root = JsonParser.parseString(jsonStr).asJsonObject
            val results = root.getAsJsonArray("results") ?: JsonArray()

            val candidateHospitals = mutableListOf<Hospital>()

            for (element in results) {
                val obj = element.asJsonObject
                val placeId = obj.get("place_id")?.asString ?: continue
                val name = obj.get("name")?.asString ?: "Hospital"
                val locationObj = obj.getAsJsonObject("geometry")?.getAsJsonObject("location") ?: continue
                val hospLat = locationObj.get("lat").asDouble
                val hospLng = locationObj.get("lng").asDouble

                val phone = fetchPlacePhoneNumber(placeId, apiKey) ?: continue
                if (!PhoneNumberValidator.isValid(phone)) continue

                val distance = calculateDistance(lat, lng, hospLat, hospLng)
                candidateHospitals.add(Hospital(placeId, name, phone, hospLat, hospLng, distance))
            }

            val nearest = candidateHospitals.minByOrNull { it.distanceInMeters }
            if (nearest != null) {
                HospitalSearchResult.Success(nearest)
            } else {
                HospitalSearchResult.NoHospitalWithPhoneFound
            }
        } catch (e: Exception) {
            HospitalSearchResult.Error(e.localizedMessage ?: "Google Places API error")
        }
    }

    private fun fetchPlacePhoneNumber(placeId: String, apiKey: String): String? {
        return try {
            val detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json" +
                    "?place_id=$placeId&fields=formatted_phone_number,international_phone_number&key=$apiKey"
            val jsonStr = executeHttpGet(detailsUrl) ?: return null
            val root = JsonParser.parseString(jsonStr).asJsonObject
            val result = root.getAsJsonObject("result") ?: return null
            val phone = result.get("international_phone_number")?.asString
                ?: result.get("formatted_phone_number")?.asString
            phone
        } catch (e: Exception) {
            null
        }
    }

    private fun searchOverpassHospitals(userLat: Double, userLng: Double): HospitalSearchResult {
        return try {
            val radiusMeters = 15000
            val overpassQuery = "[out:json][timeout:15];" +
                    "(node[\"amenity\"=\"hospital\"](around:$radiusMeters,$userLat,$userLng);" +
                    "way[\"amenity\"=\"hospital\"](around:$radiusMeters,$userLat,$userLng););" +
                    "out center;"

            val encodedQuery = URLEncoder.encode(overpassQuery, "UTF-8")
            val urlString = "https://overpass-api.de/api/interpreter?data=$encodedQuery"

            val jsonStr = executeHttpGet(urlString) ?: return HospitalSearchResult.Error("Unable to reach hospital location service")
            val root = JsonParser.parseString(jsonStr).asJsonObject
            val elements = root.getAsJsonArray("elements") ?: JsonArray()

            val hospitalsWithPhone = mutableListOf<Hospital>()

            for (elem in elements) {
                val obj = elem.asJsonObject
                val id = obj.get("id")?.asString ?: continue
                val tags = obj.getAsJsonObject("tags") ?: continue

                val name = tags.get("name")?.asString
                    ?: tags.get("name:en")?.asString
                    ?: "Emergency Hospital"

                val phoneTag = tags.get("phone")?.asString
                    ?: tags.get("contact:phone")?.asString
                    ?: tags.get("emergency:phone")?.asString
                    ?: tags.get("phone:emergency")?.asString
                    ?: continue

                if (!PhoneNumberValidator.isValid(phoneTag)) continue

                val hospLat = obj.get("lat")?.asDouble
                    ?: obj.getAsJsonObject("center")?.get("lat")?.asDouble
                    ?: continue
                val hospLng = obj.get("lon")?.asDouble
                    ?: obj.getAsJsonObject("center")?.get("lon")?.asDouble
                    ?: continue

                val dist = calculateDistance(userLat, userLng, hospLat, hospLng)
                hospitalsWithPhone.add(Hospital(id, name, phoneTag, hospLat, hospLng, dist))
            }

            val nearest = hospitalsWithPhone.minByOrNull { it.distanceInMeters }
            if (nearest != null) {
                HospitalSearchResult.Success(nearest)
            } else {
                HospitalSearchResult.NoHospitalWithPhoneFound
            }
        } catch (e: Exception) {
            HospitalSearchResult.Error("Error searching nearby hospitals: ${e.localizedMessage}")
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
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
