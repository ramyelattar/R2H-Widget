package com.r2h_widget.widget.weather

import com.r2h_widget.widget.common.WidgetActionConfiguration
import com.r2h_widget.widget.common.WidgetBackgroundConfiguration

/** Arrangement of the weather readout. */
enum class WeatherWidgetLayout {
    /** Centered temperature over condition and place. */
    EXPANDED,

    /** Temperature and condition on one line for small cells. */
    COMPACT,
}

enum class WeatherUnits(val label: String) {
    METRIC("Celsius"),
    IMPERIAL("Fahrenheit"),
}

/**
 * Typed configuration for the single Weather product.
 *
 * The production widget uses the Open-Meteo repository when location and network
 * data are available; the typed fields still support an explicit unavailable state.
 */
data class WeatherWidgetConfiguration(
    val layout: WeatherWidgetLayout = WeatherWidgetLayout.EXPANDED,
    val showTemperature: Boolean = true,
    val showCondition: Boolean = true,
    val showLocation: Boolean = true,
    val showHighLow: Boolean = false,
    val showIcon: Boolean = true,
    val units: WeatherUnits = WeatherUnits.METRIC,
    val textScale: Float = 1f,
    val temperatureColorHex: String = "#F4F2FA",
    val secondaryColorHex: String = "#A7A4B5",
    val iconTintHex: String = "#7DD3FC",
    val contentPaddingDp: Float = 16f,
    val alignStart: Boolean = false,
    val locationName: String? = null,
    val background: WidgetBackgroundConfiguration = WidgetBackgroundConfiguration(),
    val action: WidgetActionConfiguration = WidgetActionConfiguration(),
)

/** Readout values a weather provider returns; `null` high-low means the source has no forecast. */
sealed interface WeatherResult {
    data class Available(
        val temperature: String,
        val condition: String,
        val locationName: String,
        val highLow: String? = null,
    ) : WeatherResult

    data class Unavailable(val reason: String) : WeatherResult
}

interface WeatherRepository {
    suspend fun current(configuration: WeatherWidgetConfiguration): WeatherResult
}

class UnavailableWeatherRepository : WeatherRepository {
    override suspend fun current(configuration: WeatherWidgetConfiguration): WeatherResult =
        WeatherResult.Unavailable("Weather source is not connected")
}
