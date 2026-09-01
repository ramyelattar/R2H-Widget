package com.r2h_widget.widget.clock

import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class ClockFormatterTest {
    private val instant = Instant.parse("2026-05-21T17:05:00Z")

    @Test
    fun formats12HourTimeWithLocaleAwareDateAndWeekday() {
        val display = ClockFormatter.format(
            instant = instant,
            configuration = DigitalClockConfiguration(use24HourFormat = false),
            zoneId = ZoneOffset.UTC,
            locale = Locale.US,
        )

        assertEquals("5:05 PM", display.time)
        assertEquals("21 May 2026", display.date)
        assertEquals("Thursday", display.weekday)
    }

    @Test
    fun formats24HourTime() {
        val display = ClockFormatter.format(
            instant = instant,
            configuration = DigitalClockConfiguration(use24HourFormat = true),
            zoneId = ZoneOffset.UTC,
            locale = Locale.US,
        )

        assertEquals("17:05", display.time)
    }

    @Test
    fun formatsCustomSeparatorAndSeconds() {
        val configuration = DigitalClockConfiguration(
            content = ClockContentConfiguration(
                use24HourFormat = true,
                leadingZero = true,
                separator = ClockTimeSeparator.DOT,
                showSeconds = true,
            ),
            date = ClockDateConfiguration(),
        )

        val display = ClockFormatter.format(instant, configuration, ZoneOffset.UTC, Locale.US)

        assertEquals("17.05.00", display.time)
    }

    @Test
    fun dateAndWeekdayPatternsRespectStudioChoices() {
        val configuration = ClockDateConfiguration(
            weekdayStyle = ClockWeekdayStyle.SHORT,
            monthStyle = ClockMonthStyle.FULL_NAME,
            yearStyle = ClockYearStyle.TWO_DIGIT,
            formatPreset = ClockDateFormatPreset.MONTH_DAY_YEAR,
        )

        assertEquals("MMMM d, yy", ClockFormatter.datePattern(configuration))
        assertEquals("EEE", ClockFormatter.weekdayPattern(configuration))
    }
}
