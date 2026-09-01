package com.r2h_widget.widget.weather

import android.content.Context
import android.location.Location

data class WeatherLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long,
)

object WeatherLocationStore {

    private const val PREFS = "weather_location"
    private const val LATITUDE = "latitude"
    private const val LONGITUDE = "longitude"
    private const val ACCURACY = "accuracy"
    private const val TIMESTAMP = "timestamp"

    fun save(context: Context, location: Location) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(
                LATITUDE,
                java.lang.Double.doubleToRawLongBits(location.latitude),
            )
            .putLong(
                LONGITUDE,
                java.lang.Double.doubleToRawLongBits(location.longitude),
            )
            .putFloat(ACCURACY, location.accuracy)
            .putLong(TIMESTAMP, location.time)
            .apply()
    }

    fun read(context: Context): WeatherLocation? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        if (!prefs.contains(LATITUDE) || !prefs.contains(LONGITUDE)) {
            return null
        }

        return WeatherLocation(
            latitude = java.lang.Double.longBitsToDouble(
                prefs.getLong(LATITUDE, 0L),
            ),
            longitude = java.lang.Double.longBitsToDouble(
                prefs.getLong(LONGITUDE, 0L),
            ),
            accuracyMeters = prefs.getFloat(ACCURACY, Float.MAX_VALUE),
            timestampMillis = prefs.getLong(TIMESTAMP, 0L),
        )
    }
}
