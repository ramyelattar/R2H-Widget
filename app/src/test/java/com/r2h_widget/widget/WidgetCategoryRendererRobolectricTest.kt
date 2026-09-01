package com.r2h_widget.widget

import android.widget.ProgressBar
import android.widget.TextView
import android.widget.TextClock
import android.widget.FrameLayout
import android.content.Context
import android.view.View
import com.r2h_widget.R
import com.r2h_widget.widget.battery.BatteryWidgetConfiguration
import com.r2h_widget.widget.battery.BatteryWidgetLayout
import com.r2h_widget.widget.battery.BatteryWidgetRenderer
import com.r2h_widget.widget.battery.BatteryWidgetState
import com.r2h_widget.widget.battery.BatteryPlugSource
import com.r2h_widget.widget.calendar.CalendarWidgetConfiguration
import com.r2h_widget.widget.calendar.CalendarWidgetRenderer
import com.r2h_widget.widget.weather.WeatherResult
import com.r2h_widget.widget.weather.WeatherWidgetConfiguration
import com.r2h_widget.widget.weather.WeatherWidgetRenderer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetCategoryRendererRobolectricTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun batteryRenderer_usesActualStateAndConfiguration() {
        val views = BatteryWidgetRenderer.render(
            context = context,
            configuration = BatteryWidgetConfiguration(showPlugSource = true),
            state = BatteryWidgetState(73, isCharging = true, plugSource = BatteryPlugSource.USB, isLow = false),
        )
        val inflated = inflate(views)

        assertEquals("73%", inflated.findViewById<TextView>(R.id.battery_percent).text.toString())
        assertEquals("Charging · Usb", inflated.findViewById<TextView>(R.id.battery_status).text.toString())
        assertEquals(73, inflated.findViewById<ProgressBar>(R.id.battery_progress).progress)
    }

    @Test
    fun batteryRenderer_hiddenPercentageRemovesTheReadout() {
        val inflated = inflate(
            BatteryWidgetRenderer.render(
                context = context,
                configuration = BatteryWidgetConfiguration(showPercentage = false),
                state = BatteryWidgetState(50, false, BatteryPlugSource.UNKNOWN, false),
            ),
        )

        assertEquals(View.GONE, inflated.findViewById<View>(R.id.battery_percent).visibility)
    }

    @Test
    fun batteryRenderer_compactModeDropsTheStatusLine() {
        val inflated = inflate(
            BatteryWidgetRenderer.render(
                context = context,
                configuration = BatteryWidgetConfiguration(layout = BatteryWidgetLayout.COMPACT),
                state = BatteryWidgetState(64, false, BatteryPlugSource.UNKNOWN, false),
            ),
        )

        assertEquals("64%", inflated.findViewById<TextView>(R.id.battery_percent).text.toString())
        assertNull(inflated.findViewById<View>(R.id.battery_status))
    }

    @Test
    fun batteryRenderer_horizontalModeKeepsEverythingOnOneRow() {
        val inflated = inflate(
            BatteryWidgetRenderer.render(
                context = context,
                configuration = BatteryWidgetConfiguration(layout = BatteryWidgetLayout.HORIZONTAL, showPlugSource = true),
                state = BatteryWidgetState(41, true, BatteryPlugSource.AC, false),
            ),
        )

        assertEquals("41%", inflated.findViewById<TextView>(R.id.battery_percent).text.toString())
        assertEquals(View.VISIBLE, inflated.findViewById<View>(R.id.battery_status).visibility)
        assertEquals(View.VISIBLE, inflated.findViewById<View>(R.id.battery_icon).visibility)
    }

    @Test
    fun calendarRenderer_usesLiveTextClockAndConfigurationVisibility() {
        val views = CalendarWidgetRenderer.render(
            context,
            CalendarWidgetConfiguration(showWeekday = false, showMonth = true),
        )
        val inflated = inflate(views)

        assertEquals(View.GONE, inflated.findViewById<TextClock>(R.id.calendar_weekday).visibility)
        assertEquals(View.VISIBLE, inflated.findViewById<TextClock>(R.id.calendar_month).visibility)
        assertNotNull(inflated.findViewById<TextClock>(R.id.calendar_month))
    }

    @Test
    fun weatherRenderer_isHonestWhenNoSnapshotIsAvailable() = runBlocking {
        val views = WeatherWidgetRenderer.render(context, WeatherWidgetConfiguration())
        val inflated = inflate(views)

        assertEquals("—", inflated.findViewById<TextView>(R.id.weather_temperature).text.toString())
        assertEquals("Weather unavailable", inflated.findViewById<TextView>(R.id.weather_condition).text.toString())
    }

    @Test
    fun weatherRenderer_displaysAvailableDataWithConfiguredVisibility() {
        val views = WeatherWidgetRenderer.renderSync(
            context,
            WeatherWidgetConfiguration(showLocation = false, showHighLow = true),
            WeatherResult.Available(
                temperature = "28°",
                condition = "Clear",
                locationName = "Dubai",
                highLow = "31° / 22°",
            ),
            options = null,
            appWidgetId = -1,
            productId = "weather",
        )
        val inflated = inflate(views)

        assertEquals("28°", inflated.findViewById<TextView>(R.id.weather_temperature).text.toString())
        assertEquals("Clear", inflated.findViewById<TextView>(R.id.weather_condition).text.toString())
        assertEquals("31° / 22°", inflated.findViewById<TextView>(R.id.weather_high_low).text.toString())
        assertEquals(View.GONE, inflated.findViewById<View>(R.id.weather_location).visibility)
    }

    private fun inflate(views: android.widget.RemoteViews): View {
        val root = FrameLayout(context)
        return views.apply(context, root)
    }
}
