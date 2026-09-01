package com.r2h_widget.widget.weather

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.roundToInt

class OpenMeteoWeatherRepository(
    context: Context,
) : WeatherRepository {

    private val appContext = context.applicationContext

    override suspend fun current(
        configuration: WeatherWidgetConfiguration,
    ): WeatherResult = withContext(Dispatchers.IO) {
        val location = WeatherLocationStore.read(appContext)
            ?: return@withContext WeatherResult.Unavailable(
                "Waiting for current location",
            )

        try {
            val unit = when (configuration.units) {
                WeatherUnits.METRIC -> "celsius"
                WeatherUnits.IMPERIAL -> "fahrenheit"
            }

            val endpoint = String.format(
                Locale.US,
                "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=%.6f" +
                    "&longitude=%.6f" +
                    "&current=temperature_2m,weather_code" +
                    "&daily=temperature_2m_max,temperature_2m_min" +
                    "&forecast_days=1" +
                    "&timezone=auto" +
                    "&temperature_unit=%s",
                location.latitude,
                location.longitude,
                unit,
            )

            val response = get(endpoint)
            val root = JSONObject(response)

            val current = root.getJSONObject("current")
            val temperature = current
                .getDouble("temperature_2m")
                .roundToInt()

            val weatherCode = current.getInt("weather_code")

            val daily = root.optJSONObject("daily")
            val maxTemperature = daily
                ?.optJSONArray("temperature_2m_max")
                ?.optDouble(0, Double.NaN)
                ?.takeUnless { it.isNaN() }
                ?.roundToInt()

            val minTemperature = daily
                ?.optJSONArray("temperature_2m_min")
                ?.optDouble(0, Double.NaN)
                ?.takeUnless { it.isNaN() }
                ?.roundToInt()

            val highLow =
                if (maxTemperature != null && minTemperature != null) {
                    "H ${maxTemperature}° · L ${minTemperature}°"
                } else {
                    null
                }

            val locationName =
                configuration.locationName
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: resolveLocationName(
                        location.latitude,
                        location.longitude,
                    )

            WeatherResult.Available(
                temperature = "${temperature}°",
                condition = conditionFor(weatherCode),
                locationName = locationName,
                highLow = highLow,
            )
        } catch (_: Exception) {
            WeatherResult.Unavailable(
                "Weather data could not be loaded",
            )
        }
    }

    private fun get(endpoint: String): String {
        val connection =
            URL(endpoint).openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty(
                "Accept",
                "application/json",
            )

            val status = connection.responseCode

            if (status !in 200..299) {
                throw IllegalStateException(
                    "Weather HTTP $status",
                )
            }

            connection.inputStream
                .bufferedReader()
                .use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveLocationName(
        latitude: Double,
        longitude: Double,
    ): String {
        if (!Geocoder.isPresent()) {
            return "Current location"
        }

        return runCatching {
            val address = Geocoder(
                appContext,
                Locale.getDefault(),
            ).getFromLocation(
                latitude,
                longitude,
                1,
            )?.firstOrNull()

            address?.locality
                ?.takeIf { it.isNotBlank() }
                ?: address?.subAdminArea
                    ?.takeIf { it.isNotBlank() }
                ?: address?.adminArea
                    ?.takeIf { it.isNotBlank() }
                ?: address?.countryName
                    ?.takeIf { it.isNotBlank() }
                ?: "Current location"
        }.getOrDefault("Current location")
    }

    private fun conditionFor(code: Int): String =
        when (code) {
            0 -> "Clear sky"

            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"

            45, 48 -> "Fog"

            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing drizzle"

            61 -> "Light rain"
            63 -> "Rain"
            65 -> "Heavy rain"

            66, 67 -> "Freezing rain"

            71 -> "Light snow"
            73 -> "Snow"
            75 -> "Heavy snow"
            77 -> "Snow grains"

            80 -> "Light showers"
            81 -> "Rain showers"
            82 -> "Heavy showers"

            85, 86 -> "Snow showers"

            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"

            else -> "Current weather"
        }
}
