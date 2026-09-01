package com.r2h_widget.widget.clock

import com.r2h_widget.R

/**
 * Bounded RemoteViews layout selection. Each base keeps its time TextClock
 * live, while date families are selected independently through child layouts.
 */
internal object DigitalClockRemoteViewsLayouts {
    fun baseFor(family: ClockFontFamily): Int = when (family) {
        ClockFontFamily.SYSTEM -> R.layout.widget_digital_clock
        ClockFontFamily.ROUNDED -> R.layout.widget_digital_clock_rounded
        ClockFontFamily.CONDENSED -> R.layout.widget_digital_clock_condensed
        ClockFontFamily.MONO -> R.layout.widget_digital_clock_mono_font
        ClockFontFamily.ELEGANT -> R.layout.widget_digital_clock_elegant
        ClockFontFamily.DIGITAL -> R.layout.widget_digital_clock_digital
    }

    fun dateFor(family: ClockFontFamily): Int = when (family) {
        ClockFontFamily.SYSTEM -> R.layout.widget_clock_date_system
        ClockFontFamily.ROUNDED -> R.layout.widget_clock_date_rounded
        ClockFontFamily.CONDENSED -> R.layout.widget_clock_date_condensed
        ClockFontFamily.MONO -> R.layout.widget_clock_date_mono
        ClockFontFamily.ELEGANT -> R.layout.widget_clock_date_elegant
        ClockFontFamily.DIGITAL -> R.layout.widget_clock_date_digital
    }
}
