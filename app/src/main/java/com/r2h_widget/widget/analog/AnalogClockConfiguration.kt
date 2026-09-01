package com.r2h_widget.widget.analog

import com.r2h_widget.widget.clock.ClockDateAlignment
import com.r2h_widget.widget.clock.ClockDatePlacement
import com.r2h_widget.widget.clock.ClockDateFormatPreset
import com.r2h_widget.widget.clock.ClockFontFamily
import com.r2h_widget.widget.clock.ClockMonthStyle
import com.r2h_widget.widget.clock.ClockWeekdayStyle
import com.r2h_widget.widget.clock.ClockYearStyle
import com.r2h_widget.widget.clock.ClockDateConfiguration
import com.r2h_widget.widget.common.WidgetActionConfiguration
import com.r2h_widget.widget.common.WidgetBackgroundConfiguration
import com.r2h_widget.widget.common.WidgetEffectsConfiguration

/** How the tick marks around the dial are drawn. */
enum class AnalogMarkerStyle(val label: String) {
    DOTS("Dots"),
    LINES("Lines"),
}

/** Which hour positions show numerals. */
enum class AnalogNumberStyle(val label: String) {
    NONE("None"),
    QUARTER("12 · 3 · 6 · 9"),
    ALL("All hours"),
}

/** The small hub that ties the hands together. */
enum class AnalogCapStyle(val label: String) {
    DOT("Dot"),
    RING("Ring"),
}

/**
 * Typed configuration for the single Analog Clock product. Dial styles, markers,
 * hands, numerals, and the date readout are all studio controls on one product.
 */
data class AnalogClockConfiguration(
    val showHourMarkers: Boolean = true,
    val showMinuteMarkers: Boolean = true,
    val markerStyle: AnalogMarkerStyle = AnalogMarkerStyle.LINES,
    val markerColorHex: String = "#8E8A9E",
    val numberStyle: AnalogNumberStyle = AnalogNumberStyle.QUARTER,
    val numberColorHex: String = "#F4F2FA",
    val dialFillEnabled: Boolean = true,
    val dialColorHex: String = "#141024",
    val showHourHand: Boolean = true,
    val showMinuteHand: Boolean = true,
    val showSecondHand: Boolean = true,
    val hourHandColorHex: String = "#F4F2FA",
    val minuteHandColorHex: String = "#C9C4D8",
    val secondHandColorHex: String = "#38BDF8",
    val capStyle: AnalogCapStyle = AnalogCapStyle.DOT,
    val capColorHex: String = "#38BDF8",
    val date: ClockDateConfiguration = ClockDateConfiguration(
        enabled = false,
        placement = ClockDatePlacement.BOTTOM,
        weekdayStyle = ClockWeekdayStyle.HIDDEN,
        monthStyle = ClockMonthStyle.SHORT_NAME,
        yearStyle = ClockYearStyle.HIDDEN,
        formatPreset = ClockDateFormatPreset.DAY_MONTH,
    ),
    val dateFontFamily: ClockFontFamily = ClockFontFamily.SYSTEM,
    val dateColorHex: String = "#A7A4B5",
    val dateAlignment: ClockDateAlignment = ClockDateAlignment.CENTER,
    val contentScale: Float = 1f,
    val contentPaddingDp: Float = 14f,
    val background: WidgetBackgroundConfiguration = WidgetBackgroundConfiguration(),
    val effects: WidgetEffectsConfiguration = WidgetEffectsConfiguration(),
    val action: WidgetActionConfiguration = WidgetActionConfiguration(),
)
