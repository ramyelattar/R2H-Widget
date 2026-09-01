package com.r2h_widget.data

import com.r2h_widget.widget.analog.AnalogCapStyle
import com.r2h_widget.widget.analog.AnalogClockConfiguration
import com.r2h_widget.widget.analog.AnalogNumberStyle
import com.r2h_widget.widget.battery.BatteryWidgetConfiguration
import com.r2h_widget.widget.battery.BatteryWidgetLayout
import com.r2h_widget.widget.common.WidgetBackgroundFill
import com.r2h_widget.widget.search.SearchTarget
import com.r2h_widget.widget.search.SearchWidgetConfiguration
import com.r2h_widget.widget.weather.WeatherWidgetConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.nio.file.Files

class TypedWidgetInstanceRepositoryTest {
    @Test
    fun typedConfigurations_roundTripAndRemainIndependent() = runBlocking {
        val repository = InMemoryTypedWidgetInstanceRepository()
        val first = TypedWidgetInstance(
            21,
            "battery",
            TypedWidgetConfiguration.Battery(BatteryWidgetConfiguration(showPlugSource = true)),
        )
        val second = TypedWidgetInstance(
            22,
            "search",
            TypedWidgetConfiguration.Search(SearchWidgetConfiguration(target = SearchTarget.ASSISTANT)),
        )

        repository.save(first)
        repository.save(second)

        assertEquals(first, repository.get(21))
        assertEquals(second, repository.get(22))

        repository.delete(21)
        assertNull(repository.get(21))
        assertEquals(second, repository.get(22))
    }

    @Test
    fun codec_roundTripsEveryDeepConfiguration() {
        val instances = listOf(
            TypedWidgetInstance(
                1,
                "battery",
                TypedWidgetConfiguration.Battery(
                    BatteryWidgetConfiguration(
                        layout = BatteryWidgetLayout.HORIZONTAL,
                        showPercentage = false,
                        showPlugSource = true,
                        textScale = 1.25f,
                        progressColorHex = "#38BDF8",
                        lowBatteryColorHex = "#FB7185",
                        contentPaddingDp = 12f,
                        alignStart = true,
                        background = com.r2h_widget.widget.common.WidgetBackgroundConfiguration(
                            fill = WidgetBackgroundFill.SOLID,
                            solidColorHex = "#101018",
                            cornerRadiusDp = 18f,
                        ),
                    ),
                ),
            ),
            TypedWidgetInstance(
                2,
                "weather",
                TypedWidgetConfiguration.Weather(
                    WeatherWidgetConfiguration(locationName = "Dubai", showHighLow = true, textScale = 1.1f),
                ),
            ),
            TypedWidgetInstance(
                3,
                "analog-clock",
                TypedWidgetConfiguration.Analog(
                    AnalogClockConfiguration(
                        numberStyle = AnalogNumberStyle.ALL,
                        capStyle = AnalogCapStyle.RING,
                        showSecondHand = false,
                        contentScale = 1.1f,
                    ),
                ),
            ),
            TypedWidgetInstance(
                4,
                "search",
                TypedWidgetConfiguration.Search(
                    SearchWidgetConfiguration(hintText = "Ask anything", showVoiceIcon = false),
                ),
            ),
        )

        instances.forEach { original ->
            assertEquals(
                original,
                TypedWidgetInstanceCodec.decode(original.appWidgetId, TypedWidgetInstanceCodec.encode(original)),
            )
        }
    }

    @Test
    fun legacyBatteryRecord_decodesIntoDeepConfigurationWithCanonicalId() {
        val legacy = "#!1|battery-foundation|BATTERY|percentage=false;charging=true;plug=true"

        val decoded = requireNotNull(TypedWidgetInstanceCodec.decode(11, legacy))

        assertEquals("battery", decoded.productId)
        val configuration = decoded.configuration as TypedWidgetConfiguration.Battery
        assertEquals(false, configuration.value.showPercentage)
        assertEquals(true, configuration.value.showChargingState)
        assertEquals(true, configuration.value.showPlugSource)
        assertEquals(BatteryWidgetLayout.STACKED, configuration.value.layout)
    }

    @Test
    fun legacyWeatherRecord_migratesToCanonicalProductId() {
        val legacy = "#!1|weather-foundation|WEATHER|location=Dubai"

        val decoded = requireNotNull(TypedWidgetInstanceCodec.decode(12, legacy))

        assertEquals("weather", decoded.productId)
        val configuration = decoded.configuration as TypedWidgetConfiguration.Weather
        assertEquals("Dubai", configuration.value.locationName)
    }

    @Test
    fun legacyCalendarRecord_stillDecodesForPinnedWidgets() {
        val legacy = "#!1|calendar-foundation|CALENDAR|weekday=false;month=true"

        val decoded = requireNotNull(TypedWidgetInstanceCodec.decode(13, legacy))

        assertEquals("calendar-foundation", decoded.productId)
    }

    @Test
    fun dataStoreRepository_persistsByAppWidgetId() = runBlocking {
        val directory = Files.createTempDirectory("typed-widget").toFile()
        val file = File(directory, "store.preferences_pb")
        val repository = DataStoreTypedWidgetInstanceRepository(
            androidx.datastore.preferences.core.PreferenceDataStoreFactory.create { file },
        )
        val instance = TypedWidgetInstance(
            303,
            "battery",
            TypedWidgetConfiguration.Battery(BatteryWidgetConfiguration(showPercentage = false)),
        )

        repository.save(instance)
        assertEquals(instance, repository.get(303))
        file.delete()
        directory.delete()
        Unit
    }
}
