package com.example.furever.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val city: String
)

object LocationHelper {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        context: Context,
        onSuccess: (LocationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val client = LocationServices.getFusedLocationProviderClient(context)

        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val city = getCityFromCoordinates(context, location.latitude, location.longitude)
                    onSuccess(LocationResult(location.latitude, location.longitude, city))
                } else {
                    onError("No se pudo obtener la ubicación")
                }
            }
            .addOnFailureListener {
                onError(it.message ?: "Error de ubicación")
            }
    }

    private fun getCityFromCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String {
        return try {
            val geocoder = Geocoder(context, Locale("es", "AR"))
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = addresses?.firstOrNull()


            address?.locality                          // ciudad principal
                ?: address?.subAdminArea               // municipio
                ?: address?.adminArea                  // provincia como último recurso
                ?: "Ciudad desconocida"
        } catch (e: Exception) {
            "Ciudad desconocida"
        }
    }
}